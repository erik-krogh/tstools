package dk.webbies.tscreate.analysis.methods.mixed;

import dk.au.cs.casa.typescript.types.Signature;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.*;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.JSNAPUtil.RecordedCall;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
@SuppressWarnings("Duplicates")
public class MixedTypeAnalysis implements TypeAnalysis {
    public final Map<Snap.Obj, LibraryClass> libraryClasses;
    final Snap.Obj globalObject;
    public HeapValueFactory heapFactory;
    public final UnionFindSolver solver;

    public final Options options;
    public final NativeClassesMap nativeClasses;

    public TypeFactory typeFactory;
    private final Map<Snap.Obj, LibraryClass> prototypeFunctions;
    protected NativeTypeFactory nativeTypeFactory;
    public final Map<Snap.Obj, FunctionNode> functionNodes;
    protected final List<Snap.Obj> nativeFunctions;
    public boolean analysisFinished = false;
    private final boolean upperBoundMethod;
    protected Map<AstNode, Set<Snap.Obj>> callsites;

    public MixedTypeAnalysis(Map<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj globalObject, NativeClassesMap nativeClasses, boolean upperBoundMethod, Map<AstNode, Set<Snap.Obj>> callsites) {
        this.upperBoundMethod = upperBoundMethod;
        this.callsites = callsites;
        this.solver = new UnionFindSolver(this);
        this.libraryClasses = libraryClasses;
        this.options = options;
        this.globalObject = globalObject;
        this.nativeClasses = nativeClasses;
        this.prototypeFunctions = createPrototypeFunctionMap(libraryClasses);
        List<Snap.Obj> functions = getAllFunctionInstances(globalObject);
        this.functionNodes = functions.stream().filter(closure -> closure.function.type.equals("user") || closure.function.type.equals("bind")).collect(Collectors.toMap(Function.identity(), obj -> FunctionNode.create(obj, solver)));
        this.nativeFunctions = functions.stream().filter(closure -> !(closure.function.type.equals("user") || closure.function.type.equals("bind"))).filter(closure -> !closure.function.callSignatures.isEmpty()).collect(Collectors.toList());

        this.heapFactory = new SubsetHeapValueFactory(globalObject, solver, this);
        this.nativeTypeFactory = new NativeTypeFactory(heapFactory.getPrimitivesFactory(), solver, nativeClasses);
        this.typeFactory = new TypeFactory(globalObject, libraryClasses, options, nativeClasses, this);
    }

    @Override
    public NativeClassesMap getNativeClasses() {
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

    @Override
    public UnionFindSolver getSolver() {
        return solver;
    }

    @Override
    public Snap.Obj getGlobalObject() {
        return globalObject;
    }

    @Override
    public Map<Snap.Obj, LibraryClass> getLibraryClasses() {
        return libraryClasses;
    }

    @Override
    public TypeFactory getTypeFactory() {
        return typeFactory;
    }

    @Override
    public Options getOptions() {
        return options;
    }

    private static Map<Snap.Obj, LibraryClass> createPrototypeFunctionMap(Map<Snap.Obj, LibraryClass> libraryClasses) {
        Map<Snap.Obj, LibraryClass> result = new HashMap<>();
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
        System.out.println("Analyzing " + functionNodes.size() + " functions");

        int counter = 0;
        for (Snap.Obj closure : functionNodes.keySet()) {
            System.out.println("Analysis: " + ++counter + "/" + functionNodes.size());
            FunctionNode functionNode = functionNodes.get(closure);

            if (options.skipStaticAnalysisWhenPossible) {
                if (canGetEverythingFromRecordedCalls(closure)) {
                    addCallsToFunction(closure, solver, functionNode, heapFactory);
                } else {
                    analyse(closure, functionNodes, solver, functionNode, heapFactory);
                }
            } else {
                analyse(closure, functionNodes, solver, functionNode, heapFactory);
            }

            solver.finish();
        }

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

        System.out.println("Collapsing cycles");

        solver.collapseCycles();

        this.analysisFinished = true;

        counter = 0;
        for (Map.Entry<Snap.Obj, FunctionNode> entry : functionNodes.entrySet()) {
            System.out.println("Inference: " + ++counter + "/" + functionNodes.size());
            Snap.Obj closure = entry.getKey();
            FunctionNode functionNode = entry.getValue();

            UnionFeature feature = functionNode.getFeature();
            assert UnionFeature.getReachable(functionNode.getFeature()).size() == 1;
            typeFactory.registerFunction(closure, Collections.singletonList(feature));
            typeFactory.currentClosure = closure;
            typeFactory.putResolvedFunctionType(closure, typeFactory.getTypeNoCache(feature));
            typeFactory.currentClosure = null;
        }

        System.out.println("Resolving class types");

        typeFactory.resolveClassTypes();

    }

    protected boolean canGetEverythingFromRecordedCalls(Snap.Obj closure) {
        if (closure.recordedCalls == null) {
            return false;
        }
        int argsSize = closure.function.astNode.getArguments().size();
        List<Integer> notSeenArgs = new ArrayList<>();
        for (int i = 0; i < argsSize; i++) {
            notSeenArgs.add(i);
        }
        List<RecordedCall> calls = JSNAPUtil.getCalls(closure.recordedCalls);
        for (RecordedCall call : calls) {
            Iterator<Integer> iter = notSeenArgs.iterator();
            while (iter.hasNext()) {
                Integer notSeen = iter.next();
                if (call.arguments.size() <= notSeen) {
                    continue;
                }
                Snap.Value argValue = call.arguments.get(notSeen);
                if (!(argValue instanceof Snap.UndefinedConstant) && !(argValue instanceof Snap.NullConstant)) {
                    iter.remove();
                }
            }

            if (notSeenArgs.isEmpty()) {
                break;
            }
        }
        if (!notSeenArgs.isEmpty()) {
            return false;
        }

        if (returnsSomething(closure.function.astNode)) {
            for (RecordedCall call : calls) {
                if (!(call.callReturn instanceof Snap.UndefinedConstant) && !(call.callReturn instanceof Snap.NullConstant)) {
                    return true;
                }
            }
        } else {
            return true;
        }

        return false;
    }

    private boolean returnsSomething(FunctionExpression function) {
        AtomicBoolean result = new AtomicBoolean(false);
        new NodeTransverse<Void>(){
            @Override
            public Void visit(FunctionExpression function) {
                return null; // Not visiting recursively.
            }

            @Override
            public Void visit(Return aReturn) {
                if (!(aReturn.getExpression() instanceof UnaryExpression) || ((UnaryExpression) aReturn.getExpression()).getOperator() != Operator.VOID) {
                    // Here it is not just a "return;" statement.
                    result.set(true);
                }
                return NodeTransverse.super.visit(aReturn);
            }
        }.visit(function);

        return result.get();
    }

    public void analyse(Snap.Obj closure, Map<Snap.Obj, FunctionNode> functionNodes, UnionFindSolver solver, FunctionNode functionNode, HeapValueFactory heapFactory) {
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
        Snap.Obj env = closure.env;
        while (env != null) {
            if (env.properties != null) {
                for (Snap.Property property : env.properties) {
                    if (values.containsKey(property.name)) {
                        continue;
                    }
                    values.put(property.name, property);
                }
            }
            env = env.env;
        }

        if (prototypeFunctions.containsKey(closure)) {
            LibraryClass libraryClass = prototypeFunctions.get(closure);
            if (options.classOptions.useInstancesForThis) {
                solver.union(functionNode.thisNode, new HasPrototypeNode(solver, libraryClass.prototype));
                if (!"FunctionConstructor".equals(nativeClasses.nameFromPrototype(libraryClass.prototype))) {
                    solver.union(functionNode.thisNode, new IncludeNode(solver, libraryClass.getThisNodeFromInstances(heapFactory)));
                }
            }
            if (options.classOptions.unionThisFromPrototypeMethods) {
                solver.union(functionNode.thisNode, libraryClass.getNewThisNode(solver));
            }
        }

        if (closure.getProperty("prototype") != null && closure.getProperty("prototype").value instanceof Snap.Obj) {
            Snap.Obj prototype = (Snap.Obj) closure.getProperty("prototype").value;
            if (libraryClasses.containsKey(prototype)) {
                LibraryClass libraryClass = libraryClasses.get(prototype);
                if (options.classOptions.useInstancesForThis) {
                    solver.union(functionNode.thisNode, new IncludeNode(solver, libraryClass.getThisNodeFromInstances(heapFactory)));
                    solver.union(functionNode.thisNode, new HasPrototypeNode(solver, libraryClass.prototype));
                }

                if (options.classOptions.unionThisFromConstructor) {
                    solver.union(libraryClass.getNewThisNode(solver), functionNode.thisNode);
                }
            }
        }

        if (closure.recordedCalls != null) {
            addCallsToFunction(closure, solver, functionNode, heapFactory);
        }

        Map<Identifier, UnionNode> identifierMap = new HashMap<>();

        new ResolveEnvironmentVisitor(closure, closure.function.astNode, solver, identifierMap, values, JSNAPUtil.createPropertyMap(this.globalObject), this.globalObject, heapFactory, libraryClasses, options).visit(closure.function.astNode);

        applyConstraints(closure, functionNodes, solver, functionNode, heapFactory, identifierMap);
    }

    public void applyConstraints(Snap.Obj closure, Map<Snap.Obj, FunctionNode> functionNodes, UnionFindSolver solver, FunctionNode functionNode, HeapValueFactory heapFactory, Map<Identifier, UnionNode> identifierMap) {
        new MixedConstraintVisitor(closure, solver, identifierMap, functionNode, functionNodes, heapFactory, this, this.nativeTypeFactory, this.upperBoundMethod, callsites).visit(closure.function.astNode);
    }

    protected void addCallsToFunction(Snap.Obj closure, UnionFindSolver solver, FunctionNode functionNode, HeapValueFactory heapFactory) {
        List<RecordedCall> calls = JSNAPUtil.getCalls(closure.recordedCalls);
        if (calls.size() > options.maxObjects) {
            calls = calls.subList(0, options.maxObjects);
        }
        for (RecordedCall call : calls) {
            FunctionNode newFunc = FunctionNode.create(call.arguments.size(), solver);
            solver.union(newFunc.returnNode, heapFactory.fromValue(call.callReturn));

            for (int i = 0; i < call.arguments.size(); i++) {
                solver.union(newFunc.arguments.get(i), heapFactory.fromValue(call.arguments.get(i)));
            }
            solver.union(functionNode, newFunc);
        }
    }

    public static List<Snap.Obj> getAllFunctionInstances(Snap.Obj root) {
        return JSNAPUtil.getAllObjects(root).stream().filter(obj -> obj.function != null).collect(Collectors.toList());
    }
}
