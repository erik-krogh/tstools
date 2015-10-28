package dk.webbies.tscreate.analysis.unionFind;


import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.HashSet;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class PrimitiveUnionNode extends UnionNode {
    private PrimitiveDeclarationType type;

    private PrimitiveUnionNode(PrimitiveDeclarationType type, UnionFindSolver solver) {
        super(solver);
        this.type = type;
    }

    public PrimitiveDeclarationType getType() {
        return this.type;
    }

    @Override
    public void addTo(UnionClass unionClass) {
        UnionFeature feature = unionClass.getFeature();
        if (feature.primitives == null) {
            feature.primitives = new HashSet<>();
        }
        feature.primitives.add(this.type);
    }

    public static class Factory {
        private UnionFindSolver solver;
        private Snap.Obj globalObject;

        public Factory(UnionFindSolver solver, Snap.Obj globalObject) {
            this.solver = solver;
            this.globalObject = globalObject;
        }

        private UnionNode gen(PrimitiveDeclarationType type, String constructorName) {
            PrimitiveUnionNode result = new PrimitiveUnionNode(type, solver);
            if (constructorName != null) {
                try {
                    Snap.Obj prototype = (Snap.Obj) ((Snap.Obj) this.globalObject.getProperty(constructorName).value).getProperty("prototype").value;
                    solver.union(result, HasPrototypeUnionNode.create(prototype, solver));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return result;
        }

        public UnionNode number() {
            return gen(PrimitiveDeclarationType.NUMBER, "Number");
        }

        public UnionNode undefined() {
            return gen(PrimitiveDeclarationType.VOID, null);
        }

        public UnionNode bool() {
            return gen(PrimitiveDeclarationType.BOOLEAN, "Boolean");
        }

        public UnionNode string() {
            return gen(PrimitiveDeclarationType.STRING, "String");
        }

        public UnionNode any() {
            return new PrimitiveUnionNode(PrimitiveDeclarationType.ANY, solver);
        }

        public UnionNode stringOrNumber() {
            return new PrimitiveUnionNode(PrimitiveDeclarationType.STRING_OR_NUMBER, solver);
        }

        public UnionNode nonVoid() {
            return new PrimitiveUnionNode(PrimitiveDeclarationType.NON_VOID, solver);
        }
    }
}
