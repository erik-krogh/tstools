package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.analysis.unionFind.UnionClass;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.analysis.unionFind.nodes.*;
import dk.webbies.tscreate.jsnapconvert.Snap;
import dk.webbies.tscreate.paser.AST.Identifier;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.webbies.tscreate.Util.cast;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class TypeConverter {
    private final Map<UnionNode, UnionClass> classes;
    private final Map<UnionClass, InterfaceType> cache = new HashMap<>();
    private Map<String, Snap.Value> globalValues;

    public TypeConverter(Map<UnionNode, UnionClass> classes, Map<String, Snap.Value> globalValues) {
        this.classes = classes;
        this.globalValues = globalValues;
    }

    public DeclarationType convert(UnionClass unionClass) {
        if (cache.containsKey(unionClass)) {
            return cache.get(unionClass);
        }

        return convertNoCache(unionClass);
    }

    private DeclarationType convertNoCache(UnionClass unionClass) {
        List<UnionNode> nodes = unionClass.getNodes();
        if (nodes.isEmpty()) {
            return PrimitiveDeclarationType.VOID;
        }
        if (nodes.stream().allMatch(node -> node instanceof NonVoidNode || (node instanceof PrimitiveUnionNode && (((PrimitiveUnionNode)node).getType() == PrimitiveDeclarationType.UNDEFINED || ((PrimitiveUnionNode)node).getType() == PrimitiveDeclarationType.ANY)))) {
            return PrimitiveDeclarationType.ANY;
        }

        CategorizedNodes sortedNodes = new CategorizedNodes(unionClass.getNodes());

        Set<PrimitiveDeclarationType> primitives = sortedNodes.getNodes(PrimitiveUnionNode.class).stream().map(PrimitiveUnionNode::getType).filter(type -> type != PrimitiveDeclarationType.UNDEFINED && type != PrimitiveDeclarationType.VOID).collect(Collectors.toSet());
        Set<AddNode> adds = sortedNodes.getNodes(AddNode.class);
        Set<FunctionNode> functions = sortedNodes.getNodes(FunctionNode.class);
        Set<UnionNodeObject> objects = sortedNodes.getNodes(UnionNodeObject.class);

        int numberOfNonEmptyLists = (primitives.isEmpty() ? 0 : 1) + (functions.isEmpty() ? 0 : 1);

        if (numberOfNonEmptyLists > 1) {
            return PrimitiveDeclarationType.ANY;
        }

        if (primitives.stream().anyMatch(primitive -> primitive == PrimitiveDeclarationType.ANY)) {
            return PrimitiveDeclarationType.ANY;
        }

        if (functions.isEmpty() && (objects.isEmpty() || objectPropsMatchPrimitivePrototypes(objects, primitives))) {
            if (primitives.isEmpty()) {
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

        InterfaceType interfaceType = new InterfaceType("interface" + System.identityHashCode(unionClass));
        cache.put(unionClass, interfaceType);

        if (!functions.isEmpty()) {
            interfaceType.function = createFunctionType(functions);
        }

        if (!objects.isEmpty()) {
            Set<String> names = objects.stream().map(UnionNodeObject::getTypeName).collect(Collectors.toSet());
            ObjectType objectType;
            if (names.isEmpty() || names.contains(null) || names.size() > 1) {
                // TODO: Some kind of generic object.
                objectType = null;
            } else {
                objectType = new NamedObjectType(names.iterator().next());

            }
            interfaceType.object = objectType;
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

    private boolean objectPropsMatchPrimitivePrototypes(Set<UnionNodeObject> objects, Set<PrimitiveDeclarationType> primitives) {
        return objects.stream().allMatch(obj -> obj.getObjectFields().keySet().stream().allMatch(key -> {
            return objectFieldMatchPrototypeOf(this.globalValues.get("Object"), key) || primitives.stream().anyMatch(primitive -> {
                switch (primitive) {
                    case NUMBER:
                        return objectFieldMatchPrototypeOf(this.globalValues.get("Number"), key);
                    case BOOLEAN:
                        return objectFieldMatchPrototypeOf(this.globalValues.get("Boolean"), key);
                    case STRING:
                        return objectFieldMatchPrototypeOf(this.globalValues.get("String"), key);
                    case VOID:
                    case UNDEFINED:
                        return false;
                    case ANY:
                        throw new RuntimeException("Not supposed to be any \"any's\" at this point.");
                    default:
                        throw new UnsupportedOperationException("Dont know this " + primitive + ", when checking if the object accesses match the primitive");
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
        UnionFindSolver solver = new UnionFindSolver();
        for (FunctionNode function : functions) {
            if (function.astFunction != null) {
                if (returnFunction.astFunction != null && returnFunction.astFunction != function.astFunction) {
//                    throw new RuntimeException("Have a function with multiple astFunctions, don't know how to handle that yet"); // TODO:
                }
                returnFunction.astFunction = function.astFunction;
            }
            solver.union(function, returnFunction);
        }

        if (returnFunction.astFunction == null) {
            throw new RuntimeException("I only think I am supposed to do types for functions declared by the user");
        }

        return returnFunction;
    }

    public FunctionType createFunctionType(FunctionNode function) {
        UnionClass returnNode = classes.get(function.returnNode);
        DeclarationType returnType;
        if (returnNode != null) {
            returnType = convert(returnNode);
        } else {
            returnType = PrimitiveDeclarationType.VOID;
        }

        List<DeclarationType> argumentTypes = function.arguments.stream().map(unionNode -> convert(classes.get(unionNode))).collect(Collectors.toList());
        ArrayList<FunctionType.Argument> arguments = new ArrayList<>();
        List<String> argIds = function.astFunction.getArguments().stream().map(Identifier::getName).collect(Collectors.toList());
        for (int i = 0; i < argumentTypes.size(); i++) {
            DeclarationType argType = argumentTypes.get(i);
            String name = argIds.size() > i ? argIds.get(i) : "arg" + i;
            arguments.add(new FunctionType.Argument(name, argType));
        }
        return new FunctionType(returnType, arguments);
    }

    public FunctionType createFunctionType(Collection<FunctionNode> functionNodes) {
        FunctionNode function = getFunctionRepresentative(functionNodes);
        return createFunctionType(function);
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
                if (nodeClazz.isAssignableFrom(clazz)) {
                    keys.add(nodeClazz);
                }
            }

            // TODO: This acc.addAll takes forever.
            return cast(clazz, keys.stream().map(this.nodes::get).reduce(new HashSet<>(), (acc, elem)-> {
                acc.addAll(elem);
                return acc;
            }));
        }
    }
}
