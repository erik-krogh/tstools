package dk.webbies.tscreate.analysis.unionFind;


import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.Collections;
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
        feature.primitives.add(this.type.getType());
    }

    public static class Factory {
        private UnionFindSolver solver;
        private Snap.Obj globalObject;
        private UnionNode bool;
        private UnionNode number;
        private UnionNode undefined;
        private UnionNode string;
        private UnionNode  any;
        private UnionNode stringOrNumber;
        private UnionNode nonVoid;
        private UnionNode function;
        private UnionNode array;

        public Factory(UnionFindSolver solver, Snap.Obj globalObject) {
            this.solver = solver;
            this.globalObject = globalObject;
            this.bool = gen(PrimitiveDeclarationType.Boolean(), "Boolean");
            this.number = gen(PrimitiveDeclarationType.Number(), "Number");
            this.undefined = gen(PrimitiveDeclarationType.Void());
            this.string = gen(PrimitiveDeclarationType.String(), "String");
            this.any = gen(PrimitiveDeclarationType.Any());
            this.stringOrNumber = gen(PrimitiveDeclarationType.StringOrNumber(), "Number", "String");
            this.nonVoid = gen(PrimitiveDeclarationType.NonVoid());
            this.function = solver.union(getPrototype("Function"), FunctionNode.create(Collections.EMPTY_LIST, this.solver));
            this.array = solver.union(getPrototype("Array"), new DynamicAccessNode(this.solver, new EmptyNode(this.solver), number()));
        }

        private UnionNode gen(PrimitiveDeclarationType type, String... constructorNames) {
            PrimitiveNode result = new PrimitiveNode(type, solver);
            for (String constructorName : constructorNames) {
                solver.union(result, getPrototype(constructorName));
            }
            return result;
        }

        private HasPrototypeNode getPrototype(String constructorName) {
            Snap.Obj prototype = (Snap.Obj) ((Snap.Obj) this.globalObject.getProperty(constructorName).value).getProperty("prototype").value;
            return new HasPrototypeNode(solver, prototype);
        }

        public UnionNode number() {
            return new IncludeNode(solver, number);
        }

        public UnionNode undefined() {
            return new IncludeNode(solver, undefined);
        }

        public UnionNode bool() {
            return new IncludeNode(solver, bool);
        }

        public UnionNode string() {
            return new IncludeNode(solver, string);
        }

        public UnionNode any() {
            return new IncludeNode(solver, any);
        }

        public UnionNode stringOrNumber() {
            return new IncludeNode(solver, stringOrNumber);
        }

        public UnionNode nonVoid() {
            return new IncludeNode(solver, nonVoid);
        }

        public UnionNode function() {
            return new IncludeNode(solver, function);
        }

        public UnionNode array() {
            return new IncludeNode(solver, array);
        }
    }
}
