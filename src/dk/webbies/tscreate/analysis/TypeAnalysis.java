package dk.webbies.tscreate.analysis;

import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.analysis.unionFind.FunctionNode;
import dk.webbies.tscreate.analysis.unionFind.HeapValueNode;
import dk.webbies.tscreate.analysis.unionFind.UnionNode;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.AstNode;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class TypeAnalysis {
    final HashMap<Snap.Obj, LibraryClass> libraryClasses;
    final Snap.Obj globalObject;
    final Map<Type, String> typeNames;
    final Options options;

    private final TypeFactory typeFactory;
    private final Map<Snap.Obj, FunctionNode> functionNodes;

    public TypeAnalysis(HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj globalObject, Map<Type, String> typeNames) {
        this.libraryClasses = libraryClasses;
        this.options = options;
        this.globalObject = globalObject;
        this.typeNames = typeNames;
        this.functionNodes = TypeAnalysis.getFunctionNodes(globalObject);
        this.typeFactory = new TypeFactory(globalObject, libraryClasses, options, typeNames);
    }

    public TypeFactory getTypeFactory() {
        return typeFactory;
    }

    private static Map<Snap.Obj, FunctionNode> getFunctionNodes(Snap.Obj globalObject) {
        List<Snap.Obj> functions = getAllFunctionInstances(globalObject, new HashSet<>());

        Map<Snap.Obj, FunctionNode> functionNodes = new HashMap<>();
        for (Snap.Obj function : functions) {
            functionNodes.put(function, FunctionNode.create(function));
        }
        return functionNodes;
    }

    public void analyseFunctions() {
        Set<Snap.Obj> functions = functionNodes.keySet();

        System.out.println("Analyzing " + functions.size() + " functions");

        if (options.separateFunctions) {
            int counter = 1;
            for (Snap.Obj functionClosure : functions) {
                System.out.println(counter++ + "/" + functions.size());
                UnionFindSolver solver = new UnionFindSolver();

                // This way, all the other functions will be "emptied" out, so that the result of them doesn't affect the analysis of this function.
                FunctionNode functionNode = functionNodes.get(functionClosure);
                HashMap<Snap.Obj, FunctionNode> subFunctions = new HashMap<>();
                subFunctions.put(functionClosure, functionNode); // But the one we are analysing, should still be the right one.

                HeapValueNode.Factory heapFactory = new HeapValueNode.Factory(globalObject, solver, libraryClasses, typeNames, this);

                // TODO: Very tmp, for testing underscore.
                if (functionClosure.function.id.equals("145") || functionClosure.function.id.equals("3") || true) {
                    if (functionClosure.function.id.equals("145")) {
                        System.out.println();
                    }
                    analyse(functionClosure, subFunctions, solver, functionNode, heapFactory);
                } else {
                    solver.add(functionNode);
                    solver.add(functionNode.returnNode);
                    solver.add(functionNode.thisNode);
                    functionNode.arguments.forEach(solver::add);
                }



                solver.finish();
                typeFactory.finishedFunctionNodes.add(functionNode);
                typeFactory.putResolvedFunctionType(functionClosure, typeFactory.getType(functionNode));
            }

            typeFactory.resolveClassTypes();

        } else {
            Map<ProgramPoint, UnionNode> nodes = new HashMap<>();
            UnionFindSolver solver = new UnionFindSolver();
            HeapValueNode.Factory heapFactory = new HeapValueNode.Factory(globalObject, solver, libraryClasses, typeNames, this);
            for (Snap.Obj function : functions) {
                analyse(function, functionNodes, solver, functionNodes.get(function), heapFactory);
            }

            solver.finish();

            for (FunctionNode functionNode : functionNodes.values()) {
                typeFactory.finishedFunctionNodes.add(functionNode);
            }

            for (Snap.Obj function : functions) {
                typeFactory.putResolvedFunctionType(function, typeFactory.getType(functionNodes.get(function)));
            }

            typeFactory.resolveClassTypes();

        }
    }

    public void analyse(Snap.Obj closure, Map<Snap.Obj, FunctionNode> functionNodes, UnionFindSolver solver, FunctionNode functionNode, HeapValueNode.Factory heapFactory) {
        if (closure.function.type.equals("unknown")) {
            return;
        }

        if (closure.function.type.equals("bind")) {
            if (!closure.function.arguments.isEmpty()) {
                solver.union(functionNode.thisNode, heapFactory.fromValue(closure.function.arguments.get(0)));
            }
            closure.env = closure.function.target.env;
            closure.function.astNode = closure.function.target.function.astNode;
        }

        Map<String, Snap.Value> values = new HashMap<>();
        for (Snap.Property property : closure.env.properties) { // TODO: Look at properties instead, to include getters and setters.
            values.put(property.name, property.value);
        }

        HashMap<ProgramPoint, UnionNode> programPoints = new HashMap<>();

        new ResolveEnvironmentVisitor(closure, closure.function.astNode, solver, programPoints, values, JSNAPUtil.createPropertyMap(this.globalObject), this.globalObject, heapFactory, libraryClasses).visit(closure.function.astNode);

        new UnionConstraintVisitor(closure, solver, programPoints, functionNode, functionNodes, heapFactory, this).visit(closure.function.astNode);
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
        if (obj.function != null && (obj.function.type.equals("user") || obj.function.type.equals("bind"))) {
            result.add(obj);
        }
        if (obj.properties != null) {
            for (Snap.Property property : obj.properties) {
                if (property.value != null && property.value instanceof Snap.Obj) {
                    result.addAll(getAllFunctionInstances((Snap.Obj) property.value, seen));
                }
            }
        }

        if (obj.prototype != null && obj.prototype.properties != null) {
            for (Snap.Property property : obj.prototype.properties) {
                if (property.value != null && property.value instanceof Snap.Obj) {
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
