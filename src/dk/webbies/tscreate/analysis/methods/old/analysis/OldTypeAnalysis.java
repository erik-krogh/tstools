package dk.webbies.tscreate.analysis.methods.old.analysis;

import dk.au.cs.casa.typescript.types.Signature;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.HeapValueFactory;
import dk.webbies.tscreate.analysis.NativeTypeFactory;
import dk.webbies.tscreate.analysis.TypeFactory;
import dk.webbies.tscreate.analysis.methods.mixed.MixedTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.old.analysis.unionFind.HeapValueNode;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.AstNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
@SuppressWarnings("Duplicates")
public class OldTypeAnalysis implements dk.webbies.tscreate.analysis.TypeAnalysis {
    final HashMap<Snap.Obj, LibraryClass> libraryClasses;
    final Snap.Obj globalObject;
    final Options options;

    private final TypeFactory typeFactory;
    private final Map<Snap.Obj, FunctionNode> functionNodes;
    private final UnionFindSolver solver;
    private final HeapValueNode.Factory heapFactory;
    private final List<Snap.Obj> nativeFunctions;
    private final NativeTypeFactory nativeTypeFactory;
    private DeclarationParser.NativeClassesMap nativeClasses;

    public OldTypeAnalysis(HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj globalObject, DeclarationParser.NativeClassesMap nativeClasses) {
        this.nativeClasses = nativeClasses;
        this.libraryClasses = libraryClasses;
        this.options = options;
        this.globalObject = globalObject;
        this.solver = new UnionFindSolver(this);
        this.heapFactory = new HeapValueNode.Factory(globalObject, solver, libraryClasses, this);
        this.functionNodes = OldTypeAnalysis.getFunctionNodes(globalObject, solver);
        this.nativeFunctions = MixedTypeAnalysis.getAllFunctionInstances(globalObject).stream().filter(closure -> !(closure.function.type.equals("user") || closure.function.type.equals("bind"))).filter(closure -> !closure.function.callSignatures.isEmpty()).collect(Collectors.toList());
        this.typeFactory = new TypeFactory(globalObject, libraryClasses, options, nativeClasses, this);
        this.nativeTypeFactory = new NativeTypeFactory(heapFactory.getPrimitivesFactory(), solver, nativeClasses);
    }

    public TypeFactory getTypeFactory() {
        return typeFactory;
    }

    @Override
    public Options getOptions() {
        return options;
    }

    @Override
    public Map<Snap.Obj, LibraryClass> getLibraryClasses() {
        return libraryClasses;
    }

    private static Map<Snap.Obj, FunctionNode> getFunctionNodes(Snap.Obj globalObject, UnionFindSolver solver) {
        List<Snap.Obj> functions = getAllFunctionInstances(globalObject, new HashSet<>());

        Map<Snap.Obj, FunctionNode> functionNodes = new HashMap<>();
        for (Snap.Obj function : functions) {
            functionNodes.put(function, FunctionNode.create(function, solver));
        }
        return functionNodes;
    }

    public void analyseFunctions() {
        System.out.println("Resolving native functions");

        for (Snap.Obj closure : this.nativeFunctions) {
            UnionNode node = new EmptyNode(solver);
            for (Signature signature : closure.function.callSignatures) {
                FunctionNode functionNode = nativeTypeFactory.fromSignature(signature);
                solver.union(node, new IncludeNode(solver, functionNode));
            }
            solver.finish();

            List<UnionFeature> features = UnionFeature.getReachable(node.getFeature());
            typeFactory.registerFunction(closure, features);
        }

        Set<Snap.Obj> functions = functionNodes.keySet();

        System.out.println("Analyzing " + functions.size() + " functions");

        // Same as "separate methods".
        if (options.staticMethod == Options.StaticAnalysisMethod.OLD_UNIFICATION_CONTEXT_SENSITIVE) {
            int counter = 0;
            for (Snap.Obj functionClosure : functions) {
//                System.out.println(++counter + "/" + functions.size());
                UnionFindSolver solver = new UnionFindSolver(this);

                // This way, all the other functions will be "emptied" out, so that the result of them doesn't affect the analysis of this function.
                FunctionNode functionNode = functionNodes.get(functionClosure);
                HashMap<Snap.Obj, FunctionNode> subFunctions = new HashMap<>();
                subFunctions.put(functionClosure, functionNode); // But the one we are analysing, should still be the right one.

                HeapValueNode.Factory heapFactory = new HeapValueNode.Factory(globalObject, solver, libraryClasses, this);

                analyse(functionClosure, subFunctions, solver, functionNode, heapFactory, new HashSet<>());

                UnionFeature feature = functionNode.getFeature();
                assert UnionFeature.getReachable(functionNode.getFeature()).size() == 1;
                typeFactory.registerFunction(functionClosure, Collections.singletonList(feature));
                typeFactory.currentClosure = functionClosure;
                typeFactory.putResolvedFunctionType(functionClosure, typeFactory.getTypeNoCache(feature));
                typeFactory.currentClosure = null;
            }

            typeFactory.resolveClassTypes();

        } else {
            int counter = 0;

            HeapValueNode.Factory heapFactory = new HeapValueNode.Factory(globalObject, solver, libraryClasses, this);
            for (Snap.Obj function : functions) {
                System.out.println(++counter + "/" + functions.size());
                analyse(function, functionNodes, solver, functionNodes.get(function), heapFactory, null);
            }

            solver.finish();

            counter = 0;

            for (Map.Entry<Snap.Obj, FunctionNode> entry : functionNodes.entrySet()) {
//                System.out.println("Inference: " + ++counter + "/" + functionNodes.size());
                Snap.Obj closure = entry.getKey();
                FunctionNode functionNode = entry.getValue();

                typeFactory.registerFunction(closure, Collections.singletonList(functionNode.getFeature()));
                typeFactory.currentClosure = closure;

                // This is a very ugly hack to make it actually infer a type-function, no-matter what garbage gets unified with the function.
                UnionFeature feature = new UnionClass(solver, FunctionNode.create(entry.getKey(), solver)).getFeature();
                feature.getFunctionFeature().takeIn(functionNode.getFeature().getFunctionFeature());

                typeFactory.putResolvedFunctionType(closure, typeFactory.getTypeNoCache(feature));
                typeFactory.currentClosure = null;
            }

            typeFactory.resolveClassTypes();

        }
    }

    public void analyse(Snap.Obj closure, Map<Snap.Obj, FunctionNode> functionNodes, UnionFindSolver solver, FunctionNode functionNode, HeapValueFactory heapFactory, Set<Snap.Obj> hasAnalysed) {
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
        for (Snap.Property property : closure.env.properties) {
            values.put(property.name, property.value);
        }

        HashMap<ProgramPoint, UnionNode> programPoints = new HashMap<>();

        new ResolveEnvironmentVisitor(closure, closure.function.astNode, solver, programPoints, values, JSNAPUtil.createPropertyMap(this.globalObject), this.globalObject, heapFactory, libraryClasses).visit(closure.function.astNode);

        new UnionConstraintVisitor(closure, solver, functionNode, functionNodes, heapFactory, this, programPoints, nativeTypeFactory, hasAnalysed).visit(closure.function.astNode);
    }

    public DeclarationParser.NativeClassesMap getNativeClasses() {
        return nativeClasses;
    }

    @Override
    public FunctionNode getFunctionNode(Snap.Obj closure) {
        return this.functionNodes.get(closure);
    }

    @Override
    public HeapValueFactory getHeapFactory() {
        return heapFactory;
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
