package dk.webbies.tscreate.analysis;

import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.unionFind.*;
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
    public final Options options;

    private final TypeFactory typeFactory;
    private final Map<Snap.Obj, LibraryClass> prototypeFunctions;
    public final Map<Type, String> typeNames;

    public TypeAnalysis(HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj globalObject, TypeFactory typeFactory, Map<Type, String> typeNames) {
        this.libraryClasses = libraryClasses;
        this.options = options;
        this.globalObject = globalObject;
        this.typeNames = typeNames;
        this.typeFactory = typeFactory;
        this.prototypeFunctions = createPrototypeFunctionMap(libraryClasses);
    }

    private static Map<Snap.Obj, LibraryClass> createPrototypeFunctionMap(HashMap<Snap.Obj, LibraryClass> libraryClasses) {
        HashMap<Snap.Obj, LibraryClass> result = new HashMap<>();
        for (LibraryClass libraryClass : libraryClasses.values()) {
            for (Snap.Property prop : libraryClass.prototype.getPropertyMap().values()) {
                if (!prop.name.equals("constructor") && prop.value instanceof Snap.Obj && ((Snap.Obj) prop.value).function != null) {
                    result.put((Snap.Obj) prop.value, libraryClass);
                }
            }
        }

        return result;
    }

    public void analyseFunctions() {

        List<Snap.Obj> functions = getAllFunctionInstances(globalObject, new HashSet<>());

        System.out.println("Analyzing " + functions.size() + " functions");

        int counter = 1;
        for (Snap.Obj functionClosure : functions) {
            System.out.println(counter++ + "/" + functions.size());
            UnionFindSolver solver = new UnionFindSolver();

            FunctionNode functionNode = FunctionNode.create(functionClosure, solver);

            HeapValueNode.Factory heapFactory = new HeapValueNode.Factory(globalObject, solver, libraryClasses, this, new HashSet<>());

            Map<Snap.Obj, FunctionNode> subFunctionNodes = new HashMap<>();
            subFunctionNodes.put(functionClosure, FunctionNode.create(functionClosure, solver));

            analyse(functionClosure, subFunctionNodes, solver, functionNode, heapFactory, new HashSet<>());

            solver.finish();

            typeFactory.finishedFunctionClosures.add(functionClosure);
            UnionFeature feature = functionNode.getFeature();
            if (feature.heapValues == null) {
                feature.heapValues = new HashSet<>();
            }
            feature.heapValues.add(functionClosure);
            typeFactory.putResolvedFunctionType(functionClosure, typeFactory.getTypeNoCache(feature));
        }

        typeFactory.resolveClassTypes();

    }

    public void analyse(Snap.Obj closure, Map<Snap.Obj, FunctionNode> functionNodes, UnionFindSolver solver, FunctionNode functionNode, HeapValueNode.Factory heapFactory, Set<Snap.Obj> analyzedFunctions) {
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

        Map<String, Snap.Property> values = new HashMap<>();
        for (Snap.Property property : closure.env.properties) {
            values.put(property.name, property);
        }

        if (prototypeFunctions.containsKey(closure)) {
            LibraryClass libraryClass = prototypeFunctions.get(closure);
            solver.union(functionNode.thisNode, HasPrototypeUnionNode.create(libraryClass.prototype, solver));
        }

        HashMap<ProgramPoint, UnionNode> programPoints = new HashMap<>();

        new ResolveEnvironmentVisitor(closure, closure.function.astNode, solver, programPoints, values, JSNAPUtil.createPropertyMap(this.globalObject), this.globalObject, heapFactory, libraryClasses).visit(closure.function.astNode);

        new UnionConstraintVisitor(closure, solver, programPoints, functionNode, functionNodes, heapFactory, this, analyzedFunctions).visit(closure.function.astNode);
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
