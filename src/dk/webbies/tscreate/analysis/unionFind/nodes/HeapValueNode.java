package dk.webbies.tscreate.analysis.unionFind.nodes;

import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.jsnapconvert.Snap;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class HeapValueNode extends UnionNodeObject {
    public final Snap.Value value;

    private HeapValueNode(Snap.Value value, UnionFindSolver solver, Map<Snap.Obj, FunctionNode> functionNodes) {
        this.value = value;
        cache.put(value, this);
        if (value instanceof Snap.Obj) {
            Snap.Obj obj = (Snap.Obj) value;
            if (obj.properties != null) {
                for (Snap.Property property : obj.properties) {
                    List<UnionNode> fieldNodes = fromValue(property.value, solver, functionNodes);
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
    public static List<UnionNode> fromValue(Snap.Value value, UnionFindSolver solver, Map<Snap.Obj, FunctionNode> functionNodes) {
        UnionNode primitive = getPrimitiveValue(value);
        if (primitive != null) {
            return Arrays.asList(primitive);
        }

        List<UnionNode> result = new ArrayList<>();

        Snap.Obj obj = (Snap.Obj) value;
        if (obj.function != null) {
            if (obj.function.type.equals("user")) {
                FunctionNode functionNode = new FunctionNode(obj);
                solver.union(functionNodes.get(obj), functionNode);
                result.add(functionNode);
            } else {
                throw new UnsupportedOperationException("Don't know functions of type: " + obj.function.type + " yet");
            }
        }

        if (cache.containsKey(value)) {
            result.add(cache.get(value));
        } else {
            result.add(new HeapValueNode(value, solver, functionNodes));
        }
        return result;
    }

    private static UnionNode getPrimitiveValue(Snap.Value value) {
        if (value instanceof Snap.BooleanConstant) {
            return PrimitiveUnionNode.bool();
        }
        if (value instanceof Snap.NumberConstant) {
            return PrimitiveUnionNode.number();
        }
        if (value instanceof Snap.StringConstant) {
            return PrimitiveUnionNode.string();
        }
        if (value instanceof Snap.UndefinedConstant) {
            return PrimitiveUnionNode.undefined();
        }
        if (value == null) {
            return new NonVoidNode();
        }
        return null;
    }
}
