package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.analysis.declarations.Declaration;
import dk.webbies.tscreate.analysis.declarations.DeclarationBlock;
import dk.webbies.tscreate.analysis.declarations.VariableDeclaration;
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

    public TypeFactory(Map<UnionNode, UnionClass> classes, Snap.Obj globalObject, HashMap<Snap.Obj, LibraryClass> libraryClasses, Map<Snap.Value, Collection<UnionNode>> heapToUnionNodeMap) {
        this.classes = classes;
        this.globalObject = globalObject;
        this.libraryClasses = libraryClasses;
        this.heapToUnionNodeMap = heapToUnionNodeMap;
    }

    /**
     * This function is only to be used from outside TypeFactory, it bypasses the cache, so if it is every used to get some subtype, then it can create an stackoverflow.
     *
     * @param value The heap value that
     * @return the type.
     */
    public DeclarationType getType(Snap.Value value) {
        Collection<UnionNode> classRepresentatives = heapToUnionNodeMap.get(value);
        if (classRepresentatives.size() == 0) {
            throw new RuntimeException();
        }
        if (classRepresentatives.size() == 1) {
            UnionNode unionNode = classRepresentatives.iterator().next();
            return getType(classes.get(unionNode));
        }
        //noinspection Convert2MethodRef
        List<UnionNode> nodes = classRepresentatives.stream().map(this::getUnionClassNodes).map(list -> new ArrayList<>(list)).reduce((acc, elem) -> {
            acc.addAll(elem);
            return acc;
        }).get();
        return getTypeNoCache(nodes);
    }

    private List<UnionNode> getUnionClassNodes(UnionNode unionNode) {
        if (classes.containsKey(unionNode)) {
            return classes.get(unionNode).getNodes();
        } else if (unionNode instanceof FunctionNode) {
            return Arrays.asList(unionNode);
        } else {
            throw new RuntimeException();
        }
    }

    DeclarationType getType(UnionNode node) {
        return getType(classes.get(node));
    }

    private DeclarationType getType(UnionClass unionClass) {
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

    // TODO: Make sure the cache is populated early.
    private DeclarationType getTypeNoCache(List<UnionNode> nodes) {
        if (nodes.isEmpty()) {
            return PrimitiveDeclarationType.VOID;
        }
        if (nodes.stream().allMatch(node -> node instanceof NonVoidNode || (node instanceof PrimitiveUnionNode && (((PrimitiveUnionNode) node).getType() == PrimitiveDeclarationType.UNDEFINED || ((PrimitiveUnionNode) node).getType() == PrimitiveDeclarationType.ANY)))) {
            return PrimitiveDeclarationType.ANY;
        }

        CategorizedNodes sortedNodes = new CategorizedNodes(nodes);

        Set<PrimitiveDeclarationType> primitives = sortedNodes.getNodes(PrimitiveUnionNode.class).stream().map(PrimitiveUnionNode::getType).filter(type -> type != PrimitiveDeclarationType.UNDEFINED && type != PrimitiveDeclarationType.VOID).collect(Collectors.toSet());
        Set<AddNode> adds = sortedNodes.getNodes(AddNode.class);
        Set<FunctionNode> functions = sortedNodes.getNodes(FunctionNode.class);
        Set<ObjectUnionNode> objects = sortedNodes.getNodes(ObjectUnionNode.class);

        int numberOfNonEmptyLists = (primitives.isEmpty() ? 0 : 1) + (functions.isEmpty() ? 0 : 1);

        if (numberOfNonEmptyLists > 1) {
            return PrimitiveDeclarationType.ANY;
        }

        if (primitives.stream().anyMatch(primitive -> primitive == PrimitiveDeclarationType.ANY)) {
            return PrimitiveDeclarationType.ANY;
        }

        if (functions.isEmpty() && (objects.isEmpty() || objectPropsMatchPrimitivePrototypes(sortedNodes, primitives))) {
            if (primitives.isEmpty() && adds.isEmpty()) {
                return PrimitiveDeclarationType.ANY;
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

        InterfaceType interfaceType = new InterfaceType("interface" + System.identityHashCode(nodes));

        if (!functions.isEmpty()) {
            interfaceType.function = createFunctionType(functions);
        }

        if (!objects.isEmpty()) {
            List<String> typeNames = objects.stream().filter(obj -> obj.getTypeName() != null).map(ObjectUnionNode::getTypeName).collect(Collectors.toList());

            if (!typeNames.isEmpty()) {
                if (typeNames.size() > 2) {
                    return PrimitiveDeclarationType.ANY;
                } else if (typeNames.size() > 1) {
                    return new UnionDeclarationType(typeNames.stream().map(NamedObjectType::new).collect(Collectors.toList()));
                } else {
                    return new NamedObjectType(typeNames.get(0));
                }
            }


            // TODO: Use that it is an instance of classes.
            Set<LibraryClass> classInstances = sortedNodes.getNodes(HasPrototypeUnionNode.class).stream()
                    .map(HasPrototypeUnionNode::getPrototype)
                    .map(this.libraryClasses::get)
                    .filter(clazz -> clazz != null)
                    .filter(clazz -> !clazz.isPrimitiveClass())
                    .collect(Collectors.toSet());

            List<LibraryClass> classDeclarations = sortedNodes.getNodes(HeapValueNode.class).stream()
                    .map(heap -> heap.value)
                    .filter(obj -> obj instanceof Snap.Obj)
                    .map(obj -> (Snap.Obj) obj)
                    .filter(obj -> obj.function != null)
                    .map(obj -> obj.getProperty("prototype").value)
                    .distinct()
                    .map(this.libraryClasses::get)
                    .collect(Collectors.toList());

            if (classDeclarations.size() > 2) {
                return PrimitiveDeclarationType.ANY;
            } else if (classDeclarations.size() > 1) {
                List<DeclarationType> classes = classDeclarations.stream().map(this::createClassType).collect(Collectors.toList());
                return new UnionDeclarationType(classes);
            } else if (classDeclarations.size() == 1){
                return createClassType(classDeclarations.get(0));
            }

            interfaceType.object = new UnnamedObjectType(new DeclarationBlock(getObjectProperties(objects)));
        }

        if (functions.isEmpty()) {
            if (interfaceType.object == null) {
                return PrimitiveDeclarationType.ANY;
            }
            return interfaceType.object;
        }

        if (objects.isEmpty()) {
            if (interfaceType.function == null) {
                return PrimitiveDeclarationType.ANY;
            }
            return interfaceType.function;
        }


        return interfaceType;
    }

    private Map<LibraryClass, DeclarationType> libraryClassCache = new HashMap<>();
    private DeclarationType createClassType(LibraryClass libraryClass) {
        if (libraryClass == null) {
            return null;
        }
        if (libraryClassCache.containsKey(libraryClass)) {
            return libraryClassCache.get(libraryClass);
        }
        DeclarationType result;

        Snap.Obj constructor = (Snap.Obj) libraryClass.prototype.getProperty("constructor").value;
        switch (constructor.function.type) {
            case "native":
                if (constructor.function.id.length() > 0) {
                    result = new NamedObjectType(constructor.function.id);
                } else {
                    throw new RuntimeException();
                }
                break;
            case "user":
                DeclarationType constructorType = getType(libraryClass.functionNode); // TODO: Make a createFunction, createPrimitive, createObject... And use createFunction here.

                DeclarationType propertiesType = getType(libraryClass.thisNode);  // TODO: Make a createFunction, createPrimitive, createObject... And use createObject here.
                ClassType classType = new ClassType(constructorType, propertiesType, libraryClass.getName());
                libraryClassCache.put(libraryClass, classType);
                classType.setSuperClass(createClassType(libraryClass.superClass));
                result = classType;
                break;
            default:
                throw new UnsupportedOperationException();
        }

        libraryClassCache.put(libraryClass, result);
        return result;
    }

    private ArrayList<Declaration> getObjectProperties(Set<ObjectUnionNode> objects) {
        // The objects come from the same UnionClass, so a given field is unioned together across all the objects. So we just pick representatives.
        Map<String, UnionNode> nodes = new HashMap<>();
        objects.forEach(obj -> obj.getObjectFields().forEach(nodes::put));

        ArrayList<Declaration> result = new ArrayList<>();
        nodes.forEach((name, node) -> {
            result.add(new VariableDeclaration(name, getType(node)));
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

        return returnFunction;
    }

    public FunctionType createFunctionType(FunctionNode function, List<String> argumentNames) {
        UnionClass returnNode = classes.get(function.returnNode);
        DeclarationType returnType;
        if (returnNode != null) {
            returnType = getType(returnNode);
        } else {
            returnType = PrimitiveDeclarationType.VOID;
        }

        List<DeclarationType> argumentTypes = function.arguments.stream().map(this::getType).collect(Collectors.toList());
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
