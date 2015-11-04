package dk.webbies.tscreate.analysis.unionFind;


import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.HashSet;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class PrimitiveNode extends UnionNode {
    private PrimitiveDeclarationType type;

    private PrimitiveNode(PrimitiveDeclarationType type, UnionFindSolver solver) {
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
            PrimitiveNode result = new PrimitiveNode(type, solver);
            if (constructorName != null) {
                try {
                    HasPrototypeNode hasProto = getPrototype(constructorName);
                    solver.union(result, hasProto);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return result;
        }

        private HasPrototypeNode getPrototype(String constructorName) {
            Snap.Obj prototype = (Snap.Obj) ((Snap.Obj) this.globalObject.getProperty(constructorName).value).getProperty("prototype").value;
            return new HasPrototypeNode(solver, prototype);
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
            return new PrimitiveNode(PrimitiveDeclarationType.ANY, solver);
        }

        public UnionNode stringOrNumber() {
            PrimitiveNode result = new PrimitiveNode(PrimitiveDeclarationType.STRING_OR_NUMBER, solver);
            solver.union(result, getPrototype("Number"), getPrototype("String"));
            return result;
        }

        public UnionNode nonVoid() {
            return new PrimitiveNode(PrimitiveDeclarationType.NON_VOID, solver);
        }
    }
}
