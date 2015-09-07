package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.Util;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.FunctionType;
import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.analysis.unionFind.UnionClass;
import dk.webbies.tscreate.analysis.unionFind.nodes.*;

import java.util.*;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.Util.cast;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class TypeConverter {
    public static DeclarationType convert(UnionClass unionClass, Map<UnionNode, UnionClass> classes) {
        if (unionClass == null) {
            throw new NullPointerException();
        }
        List<UnionNode> nodes = unionClass.getNodes();

        // Cannot use null here, make them into that it cannot be void.
        nodes = nodes.stream().map((node -> {
            if (node instanceof PrimitiveUnionNode && ((PrimitiveUnionNode) node).getType() == PrimitiveDeclarationType.NULL) {
                return new NonVoidNode();
            } else {
                return node;
            }
        })).collect(Collectors.toList());

        // TODO: Make method non static, to include a cache, that looks at the instances of nodes (since they are unique). Use System.identityHashCode().
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
            } else {
                throw new UnsupportedOperationException("Does not yet support this union type: " + node.getClass());
            }
        }

        int numberOfNonEmptyLists = (primitives.isEmpty() ? 0 : 1) + (functions.isEmpty() ? 0 : 1);

        if (numberOfNonEmptyLists > 1) {
            return PrimitiveDeclarationType.ANY;
        }
        if (!functions.isEmpty()) {
            return FunctionType.fromNode(functions.get(0), classes);
        }

        if (primitives.isEmpty() && !adds.isEmpty()) {
            List<DeclarationType> addTypes = new ArrayList<>();
            for (AddNode add : adds) {
                addTypes.add(convert(classes.get(add.getLhs()), classes));
                addTypes.add(convert(classes.get(add.getRhs()), classes));
            }
            if (addTypes.stream().anyMatch(type -> (!(type instanceof PrimitiveDeclarationType)))) {
                throw new RuntimeException("Don't know what to do with non-primitives with adds");
            }
            Set<PrimitiveDeclarationType> primitiveTypes = new HashSet<>(cast(PrimitiveDeclarationType.class, addTypes));
            if (primitives.size() > 2) {
                return PrimitiveDeclarationType.ANY;
            }
            boolean hasNumber = primitiveTypes.contains(PrimitiveDeclarationType.NUMBER);
            boolean hasString = primitiveTypes.contains(PrimitiveDeclarationType.STRING);
            if (hasString && hasNumber) {
                return PrimitiveDeclarationType.STRING; // TODO: Union Type?
            }
            if (primitiveTypes.size() == 1 && hasNumber) {
                return PrimitiveDeclarationType.NUMBER;
            }
            if (primitiveTypes.size() == 1 && hasString) {
                return PrimitiveDeclarationType.STRING;
            }
            throw new RuntimeException("Could not resolve the add. ");
        }


        if (primitives.isEmpty()) {
            return null;
        } else {
            Set<PrimitiveDeclarationType> types = new HashSet<>();
            for (PrimitiveUnionNode primitive : primitives) {
                types.add(primitive.getType());
            }
            // TODO: Union type?
            if (types.size() != 1) {
                return PrimitiveDeclarationType.ANY;
            }
            return types.iterator().next();
        }
    }
}
