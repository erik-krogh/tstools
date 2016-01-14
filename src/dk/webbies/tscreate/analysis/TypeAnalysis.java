package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.Identifier;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class TypeAnalysis {
    final HashMap<Snap.Obj, LibraryClass> libraryClasses;
    final Snap.Obj globalObject;
    final HeapValueFactory heapFactory;
    final UnionFindSolver solver;

    public final Options options;
    public final NativeClassesMap nativeClasses;

    private final TypeFactory typeFactory;
    private final Map<Snap.Obj, LibraryClass> prototypeFunctions;
    private final NativeTypeFactory nativeTypeFactory;
    final Map<Snap.Obj, FunctionNode> functionNodes;

    public TypeAnalysis(HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj globalObject, NativeClassesMap nativeClasses) {
        this.solver = new UnionFindSolver();
        this.libraryClasses = libraryClasses;
        this.options = options;
        this.globalObject = globalObject;
        this.nativeClasses = nativeClasses;
        this.prototypeFunctions = createPrototypeFunctionMap(libraryClasses);
        this.nativeTypeFactory = new NativeTypeFactory(new PrimitiveNode.Factory(solver, globalObject), solver, nativeClasses);
        List<Snap.Obj> functions = getAllFunctionInstances(globalObject);
        this.functionNodes = functions.stream().collect(Collectors.toMap(Function.identity(), obj -> FunctionNode.create(obj, solver)));
        this.typeFactory = new TypeFactory(globalObject, libraryClasses, options, nativeClasses, this, nativeTypeFactory);
        this.heapFactory = new HeapValueFactory(globalObject, solver, libraryClasses, this);
    }

    public TypeFactory getTypeFactory() {
        return typeFactory;
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

        System.out.println("Analyzing " + functionNodes.size() + " functions");

        int counter = 0;
        for (Snap.Obj closure : functionNodes.keySet()) {
//            System.out.println(++counter + "/" + functionNodes.size());

            FunctionNode functionNode = functionNodes.get(closure);

            analyse(closure, functionNodes, solver, functionNode, heapFactory, new HashSet<>());

            solver.finish();
        }

        for (Map.Entry<Snap.Obj, FunctionNode> entry : functionNodes.entrySet()) {
            Snap.Obj closure = entry.getKey();
            FunctionNode functionNode = entry.getValue();

            UnionFeature feature = functionNode.getFeature();
            assert UnionFeature.getReachable(functionNode.getFeature()).size() == 1;
            typeFactory.registerFunction(closure, Arrays.asList(feature.getFunctionFeature()));
            typeFactory.currentClosure = closure;
            typeFactory.putResolvedFunctionType(closure, typeFactory.getTypeNoCache(feature));
            typeFactory.currentClosure = null;
        }

        typeFactory.resolveClassTypes();

    }

    public void analyse(Snap.Obj closure, Map<Snap.Obj, FunctionNode> functionNodes, UnionFindSolver solver, FunctionNode functionNode, HeapValueFactory heapFactory, Set<Snap.Obj> analyzedFunctions) {
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
        // FIXME: Getters and setters, something gotta be done.
        if (closure.env.properties != null) {
            for (Snap.Property property : closure.env.properties) {
                values.put(property.name, property);
            }
        }

        if (prototypeFunctions.containsKey(closure)) {
            LibraryClass libraryClass = prototypeFunctions.get(closure);
            solver.union(functionNode.thisNode, new HasPrototypeNode(solver, libraryClass.prototype));
            solver.union(functionNode.thisNode, libraryClass.getThisNodeFromInstances(heapFactory));
            if (options.classOptions.unionThisFromPrototypeMethods) {
                solver.union(functionNode.thisNode, libraryClass.getNewThisNode(solver));
            }
        }

        if (closure.getProperty("prototype") != null) {
            Snap.Obj prototype = (Snap.Obj) closure.getProperty("prototype").value;
            if (libraryClasses.containsKey(prototype)) {
                LibraryClass libraryClass = libraryClasses.get(prototype);
                solver.union(functionNode.thisNode, libraryClass.getThisNodeFromInstances(heapFactory));
                solver.union(functionNode.thisNode, new HasPrototypeNode(solver, libraryClass.prototype));

                if (options.classOptions.unionThisFromConstructor) {
                    solver.union(libraryClass.getNewThisNode(solver), functionNode.thisNode);
                }
            }
        }

        Map<Identifier, UnionNode> identifierMap = new HashMap<>();

        new ResolveEnvironmentVisitor(closure, closure.function.astNode, solver, identifierMap, values, JSNAPUtil.createPropertyMap(this.globalObject), this.globalObject, heapFactory, libraryClasses, options).visit(closure.function.astNode);

        new UnionConstraintVisitor(closure, solver, identifierMap, functionNode, functionNodes, heapFactory, this, analyzedFunctions, this.nativeTypeFactory).visit(closure.function.astNode);
    }

    private static List<Snap.Obj> getAllFunctionInstances(Snap.Obj root) {
        return JSNAPUtil.getAllObjects(root).stream().filter(obj -> obj.function != null && (obj.function.type.equals("bind") || obj.function.type.equals("user"))).collect(Collectors.toList());
    }
}
