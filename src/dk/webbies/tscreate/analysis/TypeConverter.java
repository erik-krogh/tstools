package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.analysis.unionFind.nodes.EmptyUnionNode;
import dk.webbies.tscreate.analysis.unionFind.nodes.PrimitiveUnionNode;
import dk.webbies.tscreate.analysis.unionFind.nodes.UnionNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class TypeConverter {
    public static DeclarationType convert(List<UnionNode> nodes) {
        // TODO: Make method non static, to include a cache, that looks at the instances of nodes (since they are unique). Use System.identityHashCode().
        if (nodes.stream().noneMatch(node -> !(node instanceof EmptyUnionNode))) {
            return PrimitiveDeclarationType.VOID;
        }
        List<PrimitiveUnionNode> primitives = new ArrayList<>();
        for (UnionNode node : nodes) {
            if (node instanceof EmptyUnionNode) {
                continue;
            }
            if (node instanceof PrimitiveUnionNode) {
                primitives.add((PrimitiveUnionNode) node);
            } else {
                throw new UnsupportedOperationException("Does not yet support this union type");
            }
        }
        if (primitives.isEmpty()) {
            return null;
        } else {
            Set<PrimitiveUnionNode.Type> types = new HashSet<>();
            for (PrimitiveUnionNode primitive : primitives) {
                types.add(primitive.getType());
            }
            if (types.size() != 1) {
                return null;
            }
            switch (types.iterator().next()) {
                case NUMBER:
                    return PrimitiveDeclarationType.NUMBER;
                case BOOL:
                    return PrimitiveDeclarationType.BOOLEAN;
                case STRING:
                    return PrimitiveDeclarationType.STRING;
                case NULL:
                    return PrimitiveDeclarationType.NULL;
                default:
                    throw new RuntimeException("Not implemented yet");
            }
        }
    }
}
