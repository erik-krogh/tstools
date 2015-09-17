package dk.webbies.tscreate.analysis;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.unionFind.UnionClass;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.analysis.unionFind.nodes.FunctionNode;
import dk.webbies.tscreate.analysis.unionFind.nodes.HeapValueNode;
import dk.webbies.tscreate.analysis.unionFind.nodes.UnionNode;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.AstNode;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class TypeAnalysis {
    private final HashMap<Snap.Obj, LibraryClass> libraryClasses;
    private final Snap.Obj globalObject;
    private final TypeFactory typeFactory;
    private final Map<UnionNode, UnionClass> classes;
    private Options options;

    public TypeAnalysis(HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj globalObject) {
        this.libraryClasses = libraryClasses;
        this.options = options;
        this.globalObject = globalObject;
        Map<Snap.Obj, FunctionNode> functionNodes = TypeAnalysis.getFunctionNodes(globalObject);

        this.classes = analyseFunctions(functionNodes);

        this.typeFactory = new TypeFactory(classes, globalObject, libraryClasses, createHeapToUnionNodeMap());
    }

    public TypeFactory getTypeFactory() {
        return typeFactory;
    }

    private Map<Snap.Value, Collection<UnionNode>> createHeapToUnionNodeMap() {
        Multimap<Snap.Value, UnionNode> result = HashMultimap.create();

        Set<UnionNode> unionNodes = this.classes.keySet();
        for (UnionNode unionNode : unionNodes) {
            if (unionNode instanceof HeapValueNode) {
                HeapValueNode heapValue = (HeapValueNode) unionNode;
                result.put(heapValue.value, heapValue);
            }
        }

        return result.asMap();
    }

    private static Map<Snap.Obj, FunctionNode> getFunctionNodes(Snap.Obj globalObject) {
        List<Snap.Obj> functions = getAllFunctionInstances(globalObject, new HashSet<>());

        Map<Snap.Obj, FunctionNode> functionNodes = new HashMap<>();
        for (Snap.Obj function : functions) {
            functionNodes.put(function, new FunctionNode(function));
        }
        return functionNodes;
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

        HeapValueNode.Factory heapFactory = new HeapValueNode.Factory(globalObject, solver);
        new ResolveEnvironmentVisitor(closure, closure.function.astNode, solver, nodes, values, JSNAPUtil.createPropertyMap(this.globalObject), functionNodes, this.globalObject, heapFactory).visit(closure.function.astNode);

        FunctionNode functionNode = functionNodes.get(closure);
        closure.function.astNode.accept(new UnionConstraintVisitor(closure, solver, nodes, functionNode, functionNodes, libraryClasses, options, globalObject, heapFactory));
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

    private static List<Snap.Obj> getAllFunctionInstances(Snap.Obj obj, HashSet<Snap.Obj> seen) {
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
