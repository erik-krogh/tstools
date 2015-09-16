package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.unionFind.UnionClass;
import dk.webbies.tscreate.analysis.unionFind.nodes.FunctionNode;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.analysis.unionFind.nodes.UnionNode;
import dk.webbies.tscreate.jsnapconvert.Snap;
import dk.webbies.tscreate.jsnapconvert.classes.LibraryClass;
import dk.webbies.tscreate.analysis.declarations.types.FunctionType;
import dk.webbies.tscreate.paser.AST.AstNode;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class TypeAnalysis {
    private final Snap.Obj librarySnap;
    private final HashMap<Snap.Obj, LibraryClass> libraryClasses;
    private Options options;
    private Map<String, Snap.Value> globalValues;

    public TypeAnalysis(Snap.Obj librarySnap, HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, Map<String, Snap.Value> globalValues) {
        this.librarySnap = librarySnap;
        this.libraryClasses = libraryClasses;
        this.options = options;
        this.globalValues = globalValues;
    }

    public Map<Snap.Obj, FunctionType> getFunctionTypes() {
        List<Snap.Obj> functions = getAllFunctionInstances(librarySnap, new HashSet<>());

        Map<Snap.Obj, FunctionNode> functionNodes = new HashMap<>();
        for (Snap.Obj function : functions) {
            functionNodes.put(function, new FunctionNode(function));
        }

        Map<UnionNode, UnionClass> classes = analyseFunctions(functionNodes);

        TypeConverter typeConverter = new TypeConverter(classes, globalValues);

        Map<Snap.Obj, FunctionType> result = new HashMap<>();
        for (Map.Entry<Snap.Obj, FunctionNode> entry : functionNodes.entrySet()) {
            result.put(entry.getKey(), typeConverter.createFunctionType(entry.getValue()));
        }

        return result;
    }

    private Map<UnionNode, UnionClass> analyseFunctions(Map<Snap.Obj, FunctionNode> functionNodes) {
        Set<Snap.Obj> functions = functionNodes.keySet();

        if (options.separateFunctions) {
            Map<UnionNode, UnionClass> result = new HashMap<>();

            for (Snap.Obj function : functions) {
                Map<ProgramPoint, UnionNode> nodes = new HashMap<>();
                UnionFindSolver solver = new UnionFindSolver();

                // This way, all the other functions will be "emptied" out, so that the result of them doesn't affect the analysis of this function.
                HashMap<Snap.Obj, FunctionNode> subFunctions = new HashMap<>();
                subFunctions.put(function, functionNodes.get(function)); // But the one we are analysing, should still be the right one.

                analyse(function, nodes, subFunctions, solver);

                solver.finish();

                result.putAll(solver.getUnionClasses());
            }

            return result;
        } else {
            Map<ProgramPoint, UnionNode> nodes = new HashMap<>();
            UnionFindSolver solver = new UnionFindSolver();
            for (Snap.Obj function : functions) {
                analyse(function, nodes, functionNodes, solver);
            }

            solver.finish();

            return solver.getUnionClasses();
        }
    }

    private void analyse(Snap.Obj closure, Map<ProgramPoint, UnionNode> nodes, Map<Snap.Obj, FunctionNode> functionNodes, UnionFindSolver solver) {
        Map<String, Snap.Value> values = new HashMap<>();
        for (Snap.Property property : closure.env.properties) {
            values.put(property.name, property.value);
        }

        new ResolveEnvironmentVisitor(closure, closure.function.astNode, solver, nodes, values, globalValues, functionNodes).visit(closure.function.astNode);

        FunctionNode functionNode = functionNodes.get(closure);
        closure.function.astNode.accept(new UnionConstraintVisitor(closure, solver, nodes, functionNode, functionNodes, libraryClasses, options, globalValues));
    }

    public static class ProgramPoint {
        private Snap.Obj function;
        private AstNode astNode;

        public ProgramPoint(Snap.Obj function, AstNode astNode) {
            if (function.function == null) {
                throw new RuntimeException("This should be a function");
            }
            this.function = function;
            this.astNode = astNode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProgramPoint that = (ProgramPoint) o;
            return Objects.equals(function, that.function) &&
                    Objects.equals(astNode, that.astNode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(function, astNode);
        }
    }

    private List<Snap.Obj> getAllFunctionInstances(Snap.Obj obj, HashSet<Snap.Obj> seen) {
        if (seen.contains(obj)) {
            return Collections.EMPTY_LIST;
        }
        seen.add(obj);

        ArrayList<Snap.Obj> result = new ArrayList<>();
        if (obj.function != null && obj.function.type.equals("user")) {
            result.add(obj);
        }
        if (obj.properties != null) {
            for (Snap.Property property : obj.properties) {
                if (property != null && property.value != null && property.value instanceof Snap.Obj) {
                    result.addAll(getAllFunctionInstances((Snap.Obj) property.value, seen));
                }
            }
        }
        if (obj.env != null) {
            result.addAll(getAllFunctionInstances(obj.env, seen));
        }

        return result;
    }

}
