package dk.webbies.tscreate.analysis.unionFind;


import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.jsnap.Snap;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class PrimitiveUnionNode extends UnionNode {
    private PrimitiveDeclarationType type;

    private PrimitiveUnionNode(PrimitiveDeclarationType type) {
        this.type = type;
    }

    public PrimitiveDeclarationType getType() {
        return this.type;
    }

    public static class Factory {
        private UnionFindSolver solver;
        private Snap.Obj globalObject;

        public Factory(UnionFindSolver solver, Snap.Obj globalObject) {
            this.solver = solver;
            this.globalObject = globalObject;
        }

        private UnionNode gen(PrimitiveDeclarationType type, String constructorName) {
            PrimitiveUnionNode result = new PrimitiveUnionNode(type);
            if (constructorName != null) {
                Snap.Obj prototype = (Snap.Obj) ((Snap.Obj) this.globalObject.getProperty(constructorName).value).getProperty("prototype").value;
                solver.union(result, new HasPrototypeUnionNode(prototype));
            }
            return result;
        }

        public UnionNode number() {
            return gen(PrimitiveDeclarationType.NUMBER, "Number");
        }

        public UnionNode undefined() {
            return gen(PrimitiveDeclarationType.UNDEFINED, null);
        }

        public UnionNode bool() {
            return gen(PrimitiveDeclarationType.BOOLEAN, "Boolean");
        }

        public UnionNode string() {
            return gen(PrimitiveDeclarationType.STRING, "String");
        }

        public UnionNode any() {
            return gen(PrimitiveDeclarationType.ANY, null);
        }
    }
}
