package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.analysis.unionFind.UnionClass;
import dk.webbies.tscreate.analysis.unionFind.nodes.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.webbies.tscreate.Util.cast;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class TypeFactory {
    private final Map<UnionNode, UnionClass> classes;
    private final Map<UnionClass, DeclarationType> cache = new HashMap<>();
    private final Snap.Obj globalObject;
    private HashMap<Snap.Obj, LibraryClass> libraryClasses;
    private Map<Snap.Value, Collection<UnionNode>> heapToUnionNodeMap;
    private Map<Snap.Obj, FunctionNode> functionNodes;

    public TypeFactory(Map<UnionNode, UnionClass> classes, Snap.Obj globalObject, HashMap<Snap.Obj, LibraryClass> libraryClasses, Map<Snap.Value, Collection<UnionNode>> heapToUnionNodeMap, Map<Snap.Obj, FunctionNode> functionNodes) {
        this.classes = classes;
        this.globalObject = globalObject;
        this.libraryClasses = libraryClasses;
        this.heapToUnionNodeMap = heapToUnionNodeMap;
        this.functionNodes = functionNodes;
    }

    /**
     * This function is only to be used from outside TypeFactory, it bypasses the cache, so if it is every used to get some subtype, then it can create an stackoverflow.
     *
     * @param value The heap value that
     * @return the type.
     */
    public DeclarationType getType(Snap.Value value) {
        if (value instanceof Snap.Obj && functionNodes.containsKey(value)) {
            return getType(functionNodes.get(value));
        }
        Collection<UnionNode> classRepresentatives = heapToUnionNodeMap.get(value);
        if (classRepresentatives == null || classRepresentatives.size() == 0) {
            throw new RuntimeException();
        }
        if (classRepresentatives.size() == 1) {
            UnionNode unionNode = classRepresentatives.iterator().next();
            return getType(classes.get(unionNode));
        }
        //noinspection Convert2MethodRef
        List<UnionNode> nodes = classRepresentatives.stream().map(this::getUnionClassNodes).distinct().map(list -> new ArrayList<>(list)).reduce((acc, elem) -> {
            acc.addAll(elem);
            return acc;
        }).get();
        return getTypeNoCache(nodes);
    }

    private List<UnionNode> getUnionClassNodes(UnionNode unionNode) {
        if (classes.containsKey(unionNode)) {
            return classes.get(unionNode).getNodes();
        } else {
            throw new RuntimeException();
        }
    }

    DeclarationType getType(UnionNode node) {
        return getType(classes.get(node));
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
        result = getTypeNoCache(unionClass.getNodes());
        unresolvedType.setResolvedType(result);
        return result;
    }

    private DeclarationType getTypeNoCache(List<UnionNode> nodes) {
        if (nodes.isEmpty()) {
            return PrimitiveDeclarationType.VOID;
        }
        if (nodes.stream().allMatch(node -> node instanceof NonVoidNode || (node instanceof PrimitiveUnionNode && (((PrimitiveUnionNode) node).getType() == PrimitiveDeclarationType.UNDEFINED || ((PrimitiveUnionNode) node).getType() == PrimitiveDeclarationType.ANY)))) {
            return PrimitiveDeclarationType.ANY;
        }

        // Removing all the any's. TODO: Is this a good idea?
        nodes = nodes.stream().filter(node -> !(node instanceof PrimitiveUnionNode && ((PrimitiveUnionNode) node).getType() == PrimitiveDeclarationType.ANY)).collect(Collectors.toList());

        CategorizedNodes sortedNodes = new CategorizedNodes(nodes);

        Set<PrimitiveDeclarationType> primitives = sortedNodes.getNodes(PrimitiveUnionNode.class).stream().map(PrimitiveUnionNode::getType).filter(type -> type != PrimitiveDeclarationType.UNDEFINED && type != PrimitiveDeclarationType.VOID).collect(Collectors.toSet());
        Set<AddNode> adds = sortedNodes.getNodes(AddNode.class);
        Set<FunctionNode> functions = sortedNodes.getNodes(FunctionNode.class);
        Set<ObjectUnionNode> objects = sortedNodes.getNodes(ObjectUnionNode.class);


        DeclarationType primitiveType = null;
        if (functions.isEmpty() && (objects.isEmpty() || objectPropsMatchPrimitivePrototypes(sortedNodes, primitives))) {
            primitiveType = getPrimitiveType(primitives, adds);
        }

        FunctionType functionType = null;
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
            classConstructorType = createClassType(classDeclarations.get(0));
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
            throw new RuntimeException("Does not happen");
        }
        if (resultingTypes.size() == 1) {
            DeclarationType result = resultingTypes.get(0);
            if (result instanceof UnnamedObjectType) {
                InterfaceType interfaceType = new InterfaceType("interfaceObj" + System.identityHashCode(nodes));
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

    private DeclarationType getObjectInstanceType(CategorizedNodes sortedNodes, Set<ObjectUnionNode> objects) {
        List<String> typeNames = objects.stream().filter(obj -> obj.getTypeName() != null).map(ObjectUnionNode::getTypeName).collect(Collectors.toList());

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
                .map(this::createClassType)
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

    private DeclarationType getPrimitiveType(Set<PrimitiveDeclarationType> primitives, Set<AddNode> adds) {
        if (primitives.isEmpty() && adds.isEmpty()) {
            return null;
        }
        DeclarationType addType = null;
        if (!adds.isEmpty()) {
            addType = getAddType(adds);
        }
        if (primitives.isEmpty() && addType != null) {
            return addType;
        }

        if (primitives.size() != 1) {
            return new UnionDeclarationType(primitives);
        }
        return primitives.iterator().next();
    }

    private Map<LibraryClass, DeclarationType> libraryClassCache = new HashMap<>();

    private DeclarationType createClassType(LibraryClass libraryClass) {
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
        if (libraryClassCache.containsKey(libraryClass)) {
            return libraryClassCache.get(libraryClass);
        }
        UnresolvedDeclarationType unresolvedType = new UnresolvedDeclarationType();
        libraryClassCache.put(libraryClass, unresolvedType);
        DeclarationType result;

        Snap.Property constructorProp = libraryClass.prototype.getProperty("constructor");
        if (constructorProp == null) {
            return null;
        }
        Snap.Obj constructor = (Snap.Obj) constructorProp.value;
        switch (constructor.function.type) {
            case "native":
                if (constructor.function.id.length() > 0) {
                    // TODO; Nope not right, it is the constructor, not an instance of it.
                    return null;
//                    throw new UnsupportedOperationException();
                } else if (true) { // <- prevents unreachable statement for the break.
                    throw new RuntimeException();
                }
                break;
            case "user":
                // TODO: Remember the static fields, also create some method for removing fields defined in the prototype.

                // TODO: The constructorNode might never have been seen by the unionFindSolver.
                // Bypassing the cache.
                List<FunctionNode> constructorNodes = classes.get(libraryClass.constructorNode).getNodes().stream().filter(node -> node instanceof FunctionNode).map(node -> (FunctionNode) node).collect(Collectors.toList());
                FunctionType constructorType = createFunctionType(constructorNodes); // TODO: Find where to many functionNodes gets unioned together.

                Map<String, DeclarationType> prototypeProperties = new HashMap<>();
                // TODO: Test that it actually has the thisNode when it should.
                if (classes.containsKey(libraryClass.thisNode)) {
                    // Bypassing the cache
                    List<UnionNode> nodes = classes.get(libraryClass.thisNode).getNodes();
                    prototypeProperties.putAll(getObjectProperties(new CategorizedNodes(nodes).getNodes(ObjectUnionNode.class)));
                }

                // I assume the prototype is correct, so i just overwrite whatever was before.
                libraryClass.prototype.getPropertyValueMap().forEach((name, value) -> {
                    if (name.equals("constructor")) {
                        return;
                    }
                    prototypeProperties.put(name, getType(value));
                });

                Map<String, DeclarationType> staticFields = getObjectProperties(classes.get(libraryClass.constructorNode).getNodes().stream().filter(node -> node instanceof ObjectUnionNode).map(node -> (ObjectUnionNode) node).collect(Collectors.toSet()));
                for (String name : Arrays.asList("prototype", "caller", "length", "name", "arguments")) {
                    staticFields.remove(name);
                }

//                prototypeProperties.keySet().forEach(staticFields::remove); // TODO: Something clever here, because with prototype, everything just gets removed.


                ClassType classType = new ClassType(constructorType, prototypeProperties, libraryClass.getName(), staticFields);
                libraryClassCache.put(libraryClass, classType);
                classType.setSuperClass(createClassType(libraryClass.superClass));
                result = classType;
                break;
            default:
                throw new UnsupportedOperationException();
        }

        unresolvedType.setResolvedType(result);
        return result;
    }

    private Map<String, DeclarationType> getObjectProperties(Set<ObjectUnionNode> objects) {
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

    public FunctionType createFunctionType(FunctionNode function, List<String> argumentNames) {
        if (function.closure != null && function.closure.function.type.equals("user")) {
            FunctionNode fromFunctionNodes = functionNodes.get(function.closure);
            if (fromFunctionNodes != function) {
                return createFunctionType(fromFunctionNodes, argumentNames);
            }
        }
        UnionClass returnNode = classes.get(function.returnNode);
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

    private DeclarationType createFunctionType(FunctionNode functionNode) {
        return createFunctionType(Arrays.asList(functionNode));
    }

    public FunctionType createFunctionType(Collection<FunctionNode> functionNodes) {
        FunctionNode function = getFunctionRepresentative(functionNodes);
        return createFunctionType(function, getArgumentNames(functionNodes));
    }

    private List<String> getArgumentNames(Collection<FunctionNode> functionNodes) {
        return Collections.max(functionNodes, (o1, o2) -> o1.getArgumentNames().size() - o2.getArgumentNames().size()).getArgumentNames();
    }

    private DeclarationType getAddType(Collection<AddNode> adds) {
        Set<PrimitiveDeclarationType> lhsTypes = new HashSet<>();
        Set<PrimitiveDeclarationType> rhsTypes = new HashSet<>();
        for (AddNode add : adds) {
            lhsTypes.addAll(getPrimitives(classes.get(add.getLhs()).getNodes()));
            rhsTypes.addAll(getPrimitives(classes.get(add.getRhs()).getNodes()));
        }

        Set<PrimitiveDeclarationType> both = Stream.concat(lhsTypes.stream(), rhsTypes.stream()).collect(Collectors.toSet());
        if (both.size() == 1) {
            return both.iterator().next();
        }

        boolean hasNumber = both.contains(PrimitiveDeclarationType.NUMBER);
        boolean hasString = both.contains(PrimitiveDeclarationType.STRING);

        if (lhsTypes.size() == 1 && rhsTypes.size() == 1) {
            if (hasNumber && hasString) {
                return PrimitiveDeclarationType.STRING;
            }
            PrimitiveDeclarationType lhs = lhsTypes.iterator().next();
            PrimitiveDeclarationType rhs = rhsTypes.iterator().next();
            if (lhs == rhs) {
                return lhs;
            } else {
                return new UnionDeclarationType(lhs, rhs);
            }
        }

        if (hasString && hasNumber) {
            return new UnionDeclarationType(PrimitiveDeclarationType.STRING, PrimitiveDeclarationType.NUMBER);
        }
        if (hasNumber) {
            return PrimitiveDeclarationType.NUMBER;
        }
        return PrimitiveDeclarationType.STRING;
    }

    private Set<PrimitiveDeclarationType> getPrimitives(Collection<UnionNode> nodes) {
        List<PrimitiveUnionNode> primitives = cast(PrimitiveUnionNode.class, nodes.stream().filter(node -> node instanceof PrimitiveUnionNode).collect(Collectors.toList()));
        return primitives.stream().map(PrimitiveUnionNode::getType).collect(Collectors.toSet());
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

            return cast(clazz, keys.stream().map(this.nodes::get).reduce(new HashSet<>(), (acc, elem) -> {
                acc.addAll(elem);
                return acc;
            }));
        }
    }
}
