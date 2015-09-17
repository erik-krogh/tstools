package dk.webbies.tscreate.analysis.unionFind.nodes;

import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class HeapValueNode extends ObjectUnionNode {
    public final Snap.Value value;

    private HeapValueNode(Snap.Value value, Factory factory) {
        this.value = value;
        factory.cache.put(value, this);
        if (value instanceof Snap.Obj) {
            Snap.Obj obj = (Snap.Obj) value;
            if (obj.properties != null) {
                for (Snap.Property property : obj.properties) {
                    List<UnionNode> fieldNodes = factory.fromValue(property.value);
                    EmptyUnionNode fieldNode = new EmptyUnionNode();
                    for (UnionNode node : fieldNodes) {
                        factory.solver.union(node, fieldNode);
                    }
                    addField(property.name, fieldNode);
                }
            }
        }
    }

    public static class Factory {
        private Map<Snap.Value, HeapValueNode> cache = new HashMap<>();
        private PrimitiveUnionNode.Factory primitivesFactory;
        private UnionFindSolver solver;

        public Factory(Snap.Obj globalObject, UnionFindSolver solver) {
            this.primitivesFactory = new PrimitiveUnionNode.Factory(solver, globalObject);
            this.solver = solver;
        }

        public List<UnionNode> fromValue(Snap.Value value) {
            UnionNode primitive = getPrimitiveValue(value, primitivesFactory);
            if (primitive != null) {
                return Arrays.asList(primitive);
            }

            List<UnionNode> result = new ArrayList<>();
            result.add(new ObjectUnionNode());

            Snap.Obj obj = (Snap.Obj) value;
            if (obj.prototype != null) {
                result.add(new HasPrototypeUnionNode(obj.prototype));
            }

            if (cache.containsKey(value)) {
                result.add(cache.get(value));
            } else {
                result.add(new HeapValueNode(value, this));
            }
            return result;
        }

        private UnionNode getPrimitiveValue(Snap.Value value, PrimitiveUnionNode.Factory primitivesBuilder) {
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
}
