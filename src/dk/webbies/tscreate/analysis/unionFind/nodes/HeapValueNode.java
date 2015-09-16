package dk.webbies.tscreate.analysis.unionFind.nodes;

import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.jsnapconvert.Snap;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class HeapValueNode extends UnionNodeObject {
    public final Snap.Value value;

    private HeapValueNode(Snap.Value value, UnionFindSolver solver, PrimitiveUnionNode.Factory primitivesBuilder) {
        this.value = value;
        cache.put(value, this);
        if (value instanceof Snap.Obj) {
            Snap.Obj obj = (Snap.Obj) value;
            if (obj.properties != null) {
                for (Snap.Property property : obj.properties) {
                    List<UnionNode> fieldNodes = fromValue(property.value, solver, primitivesBuilder);
                    EmptyUnionNode fieldNode = new EmptyUnionNode();
                    for (UnionNode node : fieldNodes) {
                        solver.union(node, fieldNode);
                    }
                    addField(property.name, fieldNode);
                }
            }
        }
    }

    private static Map<Snap.Value, HeapValueNode> cache = new HashMap<>();
    public static List<UnionNode> fromValue(Snap.Value value, UnionFindSolver solver, PrimitiveUnionNode.Factory primitivesBuilder) {
        UnionNode primitive = getPrimitiveValue(value, primitivesBuilder);
        if (primitive != null) {
            return Arrays.asList(primitive);
        }

        List<UnionNode> result = new ArrayList<>();

        Snap.Obj obj = (Snap.Obj) value;
        if (obj.prototype != null) {
            result.add(new HasPrototypeUnionNode(obj.prototype));
        }

        if (cache.containsKey(value)) {
            result.add(cache.get(value));
        } else {
            result.add(new HeapValueNode(value, solver, primitivesBuilder));
        }
        return result;
    }

    private static UnionNode getPrimitiveValue(Snap.Value value, PrimitiveUnionNode.Factory primitivesBuilder) {
        if (value instanceof Snap.BooleanConstant) {
            return primitivesBuilder.bool();
        }
        if (value instanceof Snap.NumberConstant) {
            return primitivesBuilder.number();
        }
        if (value instanceof Snap.StringConstant) {
            return primitivesBuilder.string();
        }
        if (value instanceof Snap.UndefinedConstant) {
            return primitivesBuilder.undefined();
        }
        if (value == null) {
            return new NonVoidNode();
        }
        return null;
    }
}
