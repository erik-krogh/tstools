package dk.webbies.tscreate.analysis.unionFind.nodes;

import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.jsnapconvert.Snap;

import java.util.HashMap;
import java.util.Map;

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
                    addField(property.name, fromValue(property.value, solver, functionNodes));
                }
            }
        }
    }

    private static Map<Snap.Value, HeapValueNode> cache = new HashMap<>();
    public static UnionNode fromValue(Snap.Value value, UnionFindSolver solver, Map<Snap.Obj, FunctionNode> functionNodes) {
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
            return PrimitiveUnionNode.nullType();
        }
        Snap.Obj obj = (Snap.Obj) value;
        if (obj.function != null) {
            if (obj.function.type.equals("user")) {
                FunctionNode functionNode = new FunctionNode(obj.function.astNode);
                solver.union(functionNodes.get(obj), functionNode);
                return functionNode; // TODO: Adding some fields?
            } else {
                throw new UnsupportedOperationException("Dont know functions of type: " + obj.function.type + " yet");
            }
        }

        if (cache.containsKey(value)) {
            return cache.get(value);
        } else {
            return new HeapValueNode(value, solver, functionNodes);
        }
    }
}
