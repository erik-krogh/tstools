package dk.webbies.tscreate.analysis.unionFind.nodes;


import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.jsnapconvert.Snap;

import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class PrimitiveUnionNode implements UnionNode {
    private PrimitiveDeclarationType type;

    private PrimitiveUnionNode(PrimitiveDeclarationType type) {
        this.type = type;
    }

    public PrimitiveDeclarationType getType() {
        return this.type;
    }

    public static class Factory {
        private UnionFindSolver solver;
        private Map<String, Snap.Value> globalValues;

        public Factory(UnionFindSolver solver, Map<String, Snap.Value> globalValues) {
            this.solver = solver;
            this.globalValues = globalValues;
        }

        private UnionNode gen(PrimitiveDeclarationType type, String constructorName) {
            PrimitiveUnionNode result = new PrimitiveUnionNode(type);
            if (constructorName != null) {
                Snap.Obj prototype = (Snap.Obj) ((Snap.Obj) this.globalValues.get(constructorName)).getProperty("prototype").value;
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
