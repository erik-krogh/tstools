package dk.webbies.tscreate.analysis.methods.unionRecursively;

import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.Collections;

import static java.util.Collections.EMPTY_SET;

/**
 * Created by Erik Krogh Kristensen on 08-02-2016.
 */
public class DumbPrimitiveFactory implements PrimitiveNode.Factory {
    private UnionFindSolver solver;
    private Snap.Obj globalObject;

    public DumbPrimitiveFactory(UnionFindSolver solver, Snap.Obj globalObject) {
        this.solver = solver;
        this.globalObject = globalObject;
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
        return gen(PrimitiveDeclarationType.Number(EMPTY_SET), "Number");
    }

    public UnionNode undefined() {
        return gen(PrimitiveDeclarationType.Void(EMPTY_SET));
    }

    public UnionNode bool() {
        return gen(PrimitiveDeclarationType.Boolean(EMPTY_SET), "Boolean");
    }

    public UnionNode string() {
        return gen(PrimitiveDeclarationType.String(EMPTY_SET), "String");
    }

    public UnionNode any() {
        return gen(PrimitiveDeclarationType.Any(EMPTY_SET));
    }

    public UnionNode stringOrNumber() {
        return gen(PrimitiveDeclarationType.StringOrNumber(EMPTY_SET), "Number", "String");
    }

    public UnionNode nonVoid() {
        return gen(PrimitiveDeclarationType.NonVoid(EMPTY_SET));
    }

    public UnionNode function() {
        return solver.union(getPrototype("Function"), FunctionNode.create(Collections.EMPTY_LIST, this.solver));
    }

    public UnionNode array() {
        return solver.union(getPrototype("Array"), new DynamicAccessNode(this.solver, new EmptyNode(this.solver), number()));
    }
}
