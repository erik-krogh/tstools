package dk.webbies.tscreate.analysis;

import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.Util;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;

import java.util.*;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.Util.cast;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class TypeFactory {
    private final Map<UnionClass, DeclarationType> cache = new HashMap<>();
    private final Snap.Obj globalObject;
    private HashMap<Snap.Obj, LibraryClass> libraryClasses;
    private Options options;
    private Map<Type, String> typeNames;
    public final Set<FunctionNode> finishedFunctionNodes = new HashSet<>();

    public TypeFactory(Snap.Obj globalObject, HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, Map<Type, String> typeNames) {
        this.globalObject = globalObject;
        this.libraryClasses = libraryClasses;
        this.options = options;
        this.typeNames = typeNames;
    }


    private final Map<Snap.Obj, UnresolvedDeclarationType> functionTypes = new HashMap<>();

    public DeclarationType getFunctionType(Snap.Obj closure) {
        if (functionTypes.containsKey(closure)) {
            return functionTypes.get(closure);
        } else {
            UnresolvedDeclarationType result = new UnresolvedDeclarationType();
            functionTypes.put(closure, result);
            return result;
        }
    }

    public void putResolvedFunctionType(Snap.Obj closure, DeclarationType type) {
        if (functionTypes.containsKey(closure)) {
            UnresolvedDeclarationType unresolved = functionTypes.get(closure);
            unresolved.setResolvedType(type);
        } else {
            functionTypes.put(closure, new UnresolvedDeclarationType(type));
        }
    }


    DeclarationType getType(UnionNode node) {
        return getType(node.getUnionClass());
    }

    private DeclarationType getType(UnionClass unionClass) {
        if (unionClass == null) {
            throw new NullPointerException();
        }
        DeclarationType result = cache.get(unionClass);
        if (result != null) {
            return result;
        }
        UnresolvedDeclarationType unresolvedType = new UnresolvedDeclarationType();
        cache.put(unionClass, unresolvedType);
        result = getTypeNoCache(new HashSet<>(unionClass.getNodes()));
        unresolvedType.setResolvedType(result);
        return result;
    }

    private DeclarationType getTypeNoCache(Set<UnionNode> nodes) {
        // First unpacking the GreatestCommonOfUnionNode
        nodes = getUnfoldedNodes(nodes);

        if (nodes.isEmpty()) {
            return PrimitiveDeclarationType.VOID;
        }
        if (nodes.stream().allMatch(node -> node instanceof NonVoidNode || (node instanceof PrimitiveUnionNode && (((PrimitiveUnionNode) node).getType() == PrimitiveDeclarationType.UNDEFINED || ((PrimitiveUnionNode) node).getType() == PrimitiveDeclarationType.ANY)))) {
            return PrimitiveDeclarationType.ANY;
        }

        // Removing all the any's. TODO: Is this a good idea?
        nodes = nodes.stream().filter(node -> !(node instanceof PrimitiveUnionNode && ((PrimitiveUnionNode) node).getType() == PrimitiveDeclarationType.ANY)).collect(Collectors.toSet());

        CategorizedNodes sortedNodes = new CategorizedNodes(nodes);

        Set<PrimitiveDeclarationType> primitives = sortedNodes.getNodes(PrimitiveUnionNode.class).stream().map(PrimitiveUnionNode::getType).filter(type -> type != PrimitiveDeclarationType.UNDEFINED && type != PrimitiveDeclarationType.VOID).collect(Collectors.toSet());
        Set<FunctionNode> functions = sortedNodes.getNodes(FunctionNode.class);
        Set<ObjectUnionNode> objects = sortedNodes.getNodes(ObjectUnionNode.class);


        DeclarationType primitiveType = null;
        if (functions.isEmpty() && (objects.isEmpty() || objectPropsMatchPrimitivePrototypes(sortedNodes, primitives))) {
            primitiveType = getPrimitiveType(primitives);
        }

        DeclarationType functionType = null;
        if (!functions.isEmpty()) {
            functionType = createFunctionType(functions);
        }

        DeclarationType classConstructorType = null;
        DeclarationType objectInstanceType = getObjectInstanceType(sortedNodes, objects);

        List<LibraryClass> classDeclarations = sortedNodes.getNodes(HeapValueNode.class).stream()
                .map(heap -> heap.value)
                .filter(obj -> obj instanceof Snap.Obj)
                .map(obj -> (Snap.Obj) obj)
                .filter(obj -> obj.function != null && obj.getProperty("prototype") != null)
                .map(obj -> obj.getProperty("prototype").value)
                .distinct()
                .map(this.libraryClasses::get)
                .collect(Collectors.toList());

        if (classDeclarations.size() == 1) {
            classConstructorType = getClassType(classDeclarations.get(0));
        }

        if (objectInstanceType == null && classConstructorType == null && !objects.isEmpty()) {
            objectInstanceType = new UnnamedObjectType(getObjectProperties(objects));
        }

        // First priority.
        if (classConstructorType != null) {
            return classConstructorType;
        }

        List<DeclarationType> resultingTypes = Arrays.asList(primitiveType, functionType, objectInstanceType).stream().filter(type -> type != null).collect(Collectors.toList());
        if (resultingTypes.size() == 0) {
            getObjectInstanceType(sortedNodes, objects);
            throw new RuntimeException("Does not happen");
        }
        if (resultingTypes.size() == 1) {
            DeclarationType result = resultingTypes.get(0);
            if (result instanceof UnnamedObjectType) {
                InterfaceType interfaceType = new InterfaceType("interfaceObj" + System.identityHashCode(nodes)); // TODO: That name is ugly-sauce
                interfaceType.object = result;
                return interfaceType;
            }
            return result;
        }

        if (objectInstanceType != null && objectInstanceType == PrimitiveDeclarationType.ANY) {
            if (functionType != null) {
                return functionType;
            }
            if (primitiveType != null) {
                return primitiveType;
            }
            return PrimitiveDeclarationType.ANY;
        }
        // TODO: Remove the stuff from object that doesn't make sense (look at prototype).
        if (primitiveType != null) {
            if (functionType == null) {
                return primitiveType;
            }
            return new UnionDeclarationType(primitiveType, functionType);
        } else {
            return functionType;
        }
    }

    // TODO: Call this all the time with getUnionClass?
    private static Set<UnionNode> getUnfoldedNodes(Collection<UnionNode> nodes) {
        Set<GreatestCommonOfUnionNode> greatestCommons = nodes.stream().filter(node -> node instanceof GreatestCommonOfUnionNode).map(node -> (GreatestCommonOfUnionNode) node).collect(Collectors.toSet());

        Set<UnionNode> newNodeSet = new HashSet<>();
        newNodeSet.addAll(nodes);

        List<GreatestCommonOfUnionNode> toAdd = new ArrayList<>(greatestCommons);

        while (!toAdd.isEmpty()) {
            Set<UnionClass> classesToAdd = new HashSet<>();
            for (GreatestCommonOfUnionNode greatestCommon : toAdd) {
                for (UnionNode node : greatestCommon.getNodes()) {
                    if (!newNodeSet.contains(node)) {
                        if (node.getUnionClass() == null) {
                            throw new NullPointerException();
                        }
                        classesToAdd.add(node.getUnionClass());
                    }
                }
            }
            toAdd.clear();

            newNodeSet.removeAll(greatestCommons);
            for (UnionClass unionClass : classesToAdd) {
                for (UnionNode unionNode : unionClass.getNodes()) {
                    if (unionNode instanceof GreatestCommonOfUnionNode) {
                        if (greatestCommons.contains(unionNode)) {
                            // Do nothing, we already have it.
                        } else {
                            toAdd.add((GreatestCommonOfUnionNode) unionNode);
                            greatestCommons.add((GreatestCommonOfUnionNode) unionNode);
                        }

                    } else {
                        newNodeSet.add(unionNode);
                    }
                }
            }
        }

        return newNodeSet;
    }

    private DeclarationType getObjectInstanceType(CategorizedNodes sortedNodes, Set<ObjectUnionNode> objects) {
        List<String> typeNames = objects.stream().filter(obj -> obj.getTypeName() != null).map(ObjectUnionNode::getTypeName).distinct().collect(Collectors.toList());

        List<DeclarationType> result = new ArrayList<>();

        if (!typeNames.isEmpty()) {
            if (typeNames.size() > 2) {
                result.add(PrimitiveDeclarationType.ANY);
            } else if (typeNames.size() > 0) {
                result.addAll(typeNames.stream().map(NamedObjectType::new).collect(Collectors.toList()));
            }
        }

        sortedNodes.getNodes(HasPrototypeUnionNode.class).stream()
                .map(HasPrototypeUnionNode::getPrototype)
                .distinct()
                .map(this.libraryClasses::get)
                .filter(clazz -> clazz != null)
                .filter(clazz -> !clazz.isNativeClass())
                .map(this::getClassType)
                .filter(classType -> classType != null)
                .forEach(classType -> result.add(new ClassInstanceType(classType)));

        if (result.size() == 0) {
            return null;
        } else if (result.size() == 1) {
            return result.get(0);
        } else if (result.size() < 4) {
            return new UnionDeclarationType(result);
        } else {
            return null;
        }
    }

    private DeclarationType getPrimitiveType(Set<PrimitiveDeclarationType> primitives) {
        if (primitives.isEmpty()) {
            return null;
        }
        if (primitives.size() != 1) {
            return new UnionDeclarationType(primitives);
        }
        return primitives.iterator().next();
    }

    private Map<LibraryClass, UnresolvedDeclarationType> libraryClassCache = new HashMap<>();

    private DeclarationType getClassType(LibraryClass libraryClass) {
        if (libraryClass == null) {
            return null;
        }
        char firstChar = libraryClass.getName().charAt(0);
        if (firstChar == "_".charAt(0)) {
            libraryClass.isUsedAsClass = true; // TODO:
        }
        if (!libraryClass.isUsedAsClass) { // TODO: Nope.
            return null;
        }
        Snap.Property constructorProp = libraryClass.prototype.getProperty("constructor");
        if (constructorProp == null) {
            return null;
        }

        // Handling the native case here, because there we never end up in a recursive type.
        Snap.Obj constructor = (Snap.Obj) libraryClass.prototype.getProperty("constructor").value;
        switch (constructor.function.type) {
            case "native":
                if (constructor.function.id.length() > 0) {
                    // TODO; Nope not right, it is the constructor, not an instance of it.
                    return null;
//                    throw new UnsupportedOperationException();
                } else {
                    throw new RuntimeException();
                }
        }

        if (libraryClassCache.containsKey(libraryClass)) {
            return libraryClassCache.get(libraryClass);
        }
        UnresolvedDeclarationType unresolvedType = new UnresolvedDeclarationType();
        libraryClassCache.put(libraryClass, unresolvedType);

        return unresolvedType;
    }

    public void resolveClassTypes() {
        libraryClassCache.forEach((libraryClass, unresolvedType) -> {
            unresolvedType.setResolvedType(createClassType(libraryClass));
        });
    }

    // This is only to be used from within resolveClassTypes.
    private DeclarationType createClassType(LibraryClass libraryClass) {
        Snap.Obj constructor = (Snap.Obj) libraryClass.prototype.getProperty("constructor").value;
        switch (constructor.function.type) {
            case "user":
                // TODO: Remember the static fields, also create some method for removing fields defined in the prototype.

                // TODO: The constructorNode might never have been seen by the unionFindSolver.
                // Bypassing the cache.
                Collection<UnionNode> unfilteredConstructorNodes = TypeFactory.getUnfoldedNodes(libraryClass.constructorNodes.stream().map(node -> new ArrayList<>(node.getUnionClass().getNodes())).reduce(new ArrayList<>(), Util::reduceList));
                List<FunctionNode> constructorNodes = unfilteredConstructorNodes.stream().filter(node -> node instanceof FunctionNode).map(node -> (FunctionNode) node).collect(Collectors.toList());
                DeclarationType constructorType = createFunctionType(constructorNodes);

                Map<String, DeclarationType> prototypeProperties = new HashMap<>();

                // Bypassing the cache
                Collection<UnionNode> thisNodes = libraryClass.thisNodes.stream().map(UnionNode::getUnionClass).map(unionClass -> new ArrayList<>(unionClass.getNodes())).reduce(new ArrayList<>(), Util::reduceList);
                thisNodes = TypeFactory.getUnfoldedNodes(thisNodes);
                for (FunctionNode node : constructorNodes) {
                    thisNodes.addAll(TypeFactory.getUnfoldedNodes(node.thisNode.getUnionClass().getNodes()));
                }

                prototypeProperties.putAll(getObjectProperties(new CategorizedNodes(thisNodes).getNodes(ObjectUnionNode.class)));

                // I assume the prototype is correct, so i just overwrite whatever was before.
                libraryClass.prototype.getPropertyMap().forEach((name, prop) -> {
                    if (name.equals("constructor")) {
                        return;
                    }
                    prototypeProperties.put(name, getHeapPropType(prop));
                });

                Map<String, DeclarationType> staticFields = getObjectProperties(unfilteredConstructorNodes.stream().filter(node -> node instanceof ObjectUnionNode).map(node -> (ObjectUnionNode) node).distinct().collect(Collectors.toList()));
                for (String name : Arrays.asList("prototype", "caller", "length", "name", "arguments")) {
                    staticFields.remove(name);
                }

//                prototypeProperties.keySet().forEach(staticFields::remove); // TODO: Something clever here, because with in underscore.js, everything just gets removed.


                ClassType classType = new ClassType(constructorType, prototypeProperties, libraryClass.getName(), staticFields);
                classType.setSuperClass(getClassType(libraryClass.superClass));
                return classType;
            default: // Case "native" should already be handled here.
                throw new UnsupportedOperationException();
        }
    }

    public DeclarationType getHeapPropType(Snap.Property prop) {
        if (prop.value != null) {
            return getHeapValueType(prop.value);
        } else {
            TypeAnalysis typeAnalysis = new TypeAnalysis(libraryClasses, options, globalObject, typeNames);
            UnionFindSolver solver = new UnionFindSolver();
            HeapValueNode.Factory heapFactory = new HeapValueNode.Factory(globalObject, solver, libraryClasses, typeNames, typeAnalysis);
            UnionNode unionNode = heapFactory.fromProperty(prop);
            solver.finish();
            return getType(unionNode);
        }
    }

    public DeclarationType getHeapValueType(Snap.Value value) {
        if (value instanceof Snap.BooleanConstant) {
            return PrimitiveDeclarationType.BOOLEAN;
        } else if (value instanceof Snap.NumberConstant) {
            return PrimitiveDeclarationType.NUMBER;
        } else if (value instanceof Snap.StringConstant) {
            return PrimitiveDeclarationType.STRING;
        } else if (value instanceof Snap.UndefinedConstant) {
            return PrimitiveDeclarationType.UNDEFINED;
        } else if (value instanceof Snap.NullConstant) {
            return PrimitiveDeclarationType.ANY;
        } else if ((value instanceof Snap.Obj)) {
            if (((Snap.Obj) value).function != null) {
                return getFunctionType((Snap.Obj) value);
            } else {
                return getModuleType((Snap.Obj) value);
            }
        } else {
            throw new RuntimeException("I don't know what to do with a " + value);
        }

    }

    private ModuleType getModuleType(Snap.Obj value) {
        HashMap<String, DeclarationType> declarations = new HashMap<>();
        for (Map.Entry<String, Snap.Property> entry : value.getPropertyMap().entrySet()) {
            declarations.put(entry.getKey(), getHeapPropType(entry.getValue()));
        }

        return new ModuleType(declarations);
    }

    private Map<String, DeclarationType> getObjectProperties(Collection<ObjectUnionNode> objects) {
        // The objects come from the same UnionClass, so a given field is unioned together across all the objects. So we just pick representatives.
        Map<String, UnionNode> nodes = new HashMap<>();
        objects.forEach(obj -> obj.getObjectFields().forEach(nodes::put));

        Map<String, DeclarationType> result = new HashMap<>();
        nodes.forEach((name, node) -> {
            result.put(name, getType(node));
        });

        return result;
    }

    private boolean objectPropsMatchPrimitivePrototypes(CategorizedNodes sortedNodes, Set<PrimitiveDeclarationType> primitives) {
        Set<HeapValueNode> heapValues = sortedNodes.getNodes(HeapValueNode.class);
        if (!heapValues.isEmpty()) {
            return false;
        }
        Set<ObjectUnionNode> objects = sortedNodes.getNodes(ObjectUnionNode.class);
        return objects.stream().allMatch(obj -> obj.getObjectFields().keySet().stream().allMatch(key -> {
            return objectFieldMatchPrototypeOf(this.globalObject.getProperty("Object").value, key) || primitives.stream().anyMatch(primitive -> {
                switch (primitive) {
                    case NUMBER:
                        return objectFieldMatchPrototypeOf(this.globalObject.getProperty("Number").value, key);
                    case BOOLEAN:
                        return objectFieldMatchPrototypeOf(this.globalObject.getProperty("Boolean").value, key);
                    case STRING:
                        return objectFieldMatchPrototypeOf(this.globalObject.getProperty("String").value, key);
                    case VOID:
                    case UNDEFINED:
                        return false;
                    case ANY:
                        throw new RuntimeException("Not supposed to be any \"any's\" at this point.");
                    default:
                        throw new UnsupportedOperationException("Don't know this " + primitive + ", when checking if the object accesses match the primitive");
                }
            });
        }));
    }

    private boolean objectFieldMatchPrototypeOf(Snap.Value value, String key) {
        if (!(value instanceof Snap.Obj)) {
            throw new RuntimeException();
        }
        Snap.Obj prototype = (Snap.Obj) ((Snap.Obj) value).getProperty("prototype").value;
        return prototype.getProperty(key) == null;
    }

    private FunctionNode getFunctionRepresentative(Collection<FunctionNode> functions) {
        FunctionNode returnFunction = functions.iterator().next();
        for (FunctionNode function : functions) {
            if (function.arguments.size() > returnFunction.arguments.size()) {
                returnFunction = function;
            }
        }

        // TODO: There can than 1 closure. (example: "return foo || bar", where foo and bar are functions on the heap).
        Optional<Snap.Obj> functionClosure = functions.stream().filter(func -> func.closure != null).map(func -> func.closure).findAny();
        if (functionClosure.isPresent()) {
            returnFunction.closure = functionClosure.get();
        } else {
            returnFunction.closure = null;
        }

        return returnFunction;
    }

    public DeclarationType createFunctionType(FunctionNode function, List<String> argumentNames, Collection<FunctionNode> functionNodes) {
        if (function.closure != null && (function.closure.function.type.equals("user") || function.closure.function.type.equals("bind"))) {
            boolean inFinishedNodes = functionNodes.stream().anyMatch(finishedFunctionNodes::contains);
            if (!inFinishedNodes) {
                return getFunctionType(function.closure);
            }
        }

        UnionClass returnNode = function.returnNode.getUnionClass();
        DeclarationType returnType;
        if (returnNode != null) {
            returnType = getType(returnNode);
        } else {
            returnType = PrimitiveDeclarationType.VOID;
        }

        List<DeclarationType> argumentTypes = function.arguments.stream().map((node) -> {
            try {
                return getType(node);
            } catch (Exception e) {
                return PrimitiveDeclarationType.ANY; // TODO: Nope.
            }
        }).collect(Collectors.toList());
        ArrayList<FunctionType.Argument> arguments = new ArrayList<>();
        for (int i = 0; i < argumentTypes.size(); i++) {
            DeclarationType argType = argumentTypes.get(i);
            String name = argumentNames.size() > i ? argumentNames.get(i) : "arg" + i;
            arguments.add(new FunctionType.Argument(name, argType));
        }
        return new FunctionType(returnType, arguments);
    }

    public DeclarationType createFunctionType(Collection<FunctionNode> functionNodes) {
        FunctionNode function = getFunctionRepresentative(functionNodes);
        return createFunctionType(function, getArgumentNames(functionNodes), functionNodes);
    }

    private List<String> getArgumentNames(Collection<FunctionNode> functionNodes) {
        return Collections.max(functionNodes, (o1, o2) -> o1.getArgumentNames().size() - o2.getArgumentNames().size()).getArgumentNames();
    }
    private static class CategorizedNodes {
        HashMap<Class<?>, Set<UnionNode>> nodes = new HashMap<>();

        public CategorizedNodes(Collection<UnionNode> nodes) {
            HashMap<Class<?>, Set<UnionNode>> categorizesNodes = new HashMap<>();
            for (UnionNode node : nodes) {
                if (categorizesNodes.containsKey(node.getClass())) {
                    categorizesNodes.get(node.getClass()).add(node);
                } else {
                    categorizesNodes.put(node.getClass(), new HashSet<>(Arrays.asList(node)));
                }
            }

            this.nodes = categorizesNodes;
        }

        public <T extends UnionNode> Set<T> getNodes(Class<T> clazz) {
            List<Class<?>> keys = new ArrayList<>();
            for (Class<?> nodeClazz : nodes.keySet()) {
                if (clazz.isAssignableFrom(nodeClazz)) {
                    keys.add(nodeClazz);
                }
            }

            return cast(clazz, keys.stream().map(this.nodes::get).reduce(new HashSet<>(), Util::reduceSet));
        }
    }
}
