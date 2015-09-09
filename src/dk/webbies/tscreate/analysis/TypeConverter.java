package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.FunctionType;
import dk.webbies.tscreate.analysis.declarations.types.InterfaceType;
import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.analysis.unionFind.UnionClass;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.analysis.unionFind.nodes.*;
import dk.webbies.tscreate.paser.AST.Identifier;

import java.util.*;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.Util.cast;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class TypeConverter {
    private final Map<UnionNode, UnionClass> classes;
    private final Map<UnionClass, InterfaceType> cache = new HashMap<>();

    public TypeConverter(Map<UnionNode, UnionClass> classes) {
        this.classes = classes;
    }

    public DeclarationType convert(UnionClass unionClass) {
        if (cache.containsKey(unionClass)) {
            return cache.get(unionClass);
        }

        return convertTypeNoCache(unionClass);
    }

    private DeclarationType convertTypeNoCache(UnionClass unionClass) {
        List<UnionNode> nodes = unionClass.getNodes();

        // Cannot use null here, make them into that it cannot be void.
        if (nodes.isEmpty()) {
            return PrimitiveDeclarationType.VOID;
        }
        if (nodes.stream().allMatch(node -> node instanceof NonVoidNode)) {
            return PrimitiveDeclarationType.ANY;
        }
        nodes = nodes.stream().filter(node -> !(node instanceof NonVoidNode)).collect(Collectors.toList());

        List<PrimitiveUnionNode> primitives = new ArrayList<>();
        List<FunctionNode> functions = new ArrayList<>();
        List<AddNode> adds = new ArrayList<>();
        List<UnionNodeObject> objects = new ArrayList<>();
        for (UnionNode node : nodes) {
            if (node instanceof EmptyUnionNode) {
                continue;
            }
            if (node instanceof PrimitiveUnionNode) {
                primitives.add((PrimitiveUnionNode) node);
            } else if (node instanceof FunctionNode) {
                functions.add((FunctionNode) node);
            } else if (node instanceof AddNode) {
                adds.add((AddNode) node);
            } else if (node instanceof UnionNodeObject) {
                objects.add((UnionNodeObject) node);
            }
            else {
                throw new UnsupportedOperationException("Does not yet support this union type: " + node.getClass());
            }
        }

        int numberOfNonEmptyLists = (primitives.isEmpty() ? 0 : 1) + (functions.isEmpty() ? 0 : 1);

        if (numberOfNonEmptyLists > 1) {
            return PrimitiveDeclarationType.ANY;
        }

        if (functions.isEmpty() && objects.isEmpty()) {
            DeclarationType addType = null;
            if (!adds.isEmpty()) {
                addType = getAddType(adds);
            }
            if (primitives.isEmpty()) {
                return addType;
            }

            Set<PrimitiveDeclarationType> types = primitives.stream().map(PrimitiveUnionNode::getType).collect(Collectors.toSet());
            if (types.size() != 1) {
                return new UnionDeclarationType(types);
            }
            return types.iterator().next();
        }

        InterfaceType interfaceType = new InterfaceType();
        cache.put(unionClass, interfaceType);

        if (!functions.isEmpty()) {
            interfaceType.function = createFunctionType(functions);
        }

        return interfaceType; // TODO: Also add object stuff.
    }

    private FunctionNode getFunctionRepresentative(List<FunctionNode> functions) {
        FunctionNode returnFunction = functions.get(0);
        for (FunctionNode function : functions) {
            if (function.arguments.size() > returnFunction.arguments.size()) {
                returnFunction = function;
            }
        }
        UnionFindSolver solver = new UnionFindSolver();
        for (FunctionNode function : functions) {
            if (function.astFunction != null) {
                if (returnFunction.astFunction != null && returnFunction.astFunction != function.astFunction) {
                    throw new RuntimeException("Have a function with multiple astFunctions, don't know how to handle that yet");
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
        ArrayList<FunctionType.Argument> declarations = new ArrayList<>();
        List<String> argIds = function.astFunction.getArguments().stream().map(Identifier::getName).collect(Collectors.toList());
        for (int i = 0; i < argumentTypes.size(); i++) {
            DeclarationType argType = argumentTypes.get(i);
            String name = argIds.get(i);
            declarations.add(new FunctionType.Argument(name, argType));
        }
        return new FunctionType(returnType, declarations);
    }

    public FunctionType createFunctionType(List<FunctionNode> functionNodes) {
        FunctionNode function = getFunctionRepresentative(functionNodes);
        return createFunctionType(function);
    }

    private DeclarationType getAddType(List<AddNode> adds) {
        Set<PrimitiveDeclarationType> primitiveTypes = new HashSet<>();
        for (AddNode add : adds) {
            primitiveTypes.addAll(getPrimitives(classes.get(add.getLhs()).getNodes()));
            primitiveTypes.addAll(getPrimitives(classes.get(add.getRhs()).getNodes()));
        }

        boolean hasNumber = primitiveTypes.contains(PrimitiveDeclarationType.NUMBER);
        boolean hasString = primitiveTypes.contains(PrimitiveDeclarationType.STRING);
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
}
