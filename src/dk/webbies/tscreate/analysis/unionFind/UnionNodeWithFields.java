package dk.webbies.tscreate.analysis.unionFind;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public abstract class UnionNodeWithFields extends UnionNode {
    protected Map<String, UnionNode> fields = new HashMap<>();
    private UnionFindSolver solver;

    public UnionNodeWithFields(UnionFindSolver solver) {
        super(solver);
        this.solver = solver;
    }

    public Map<String, UnionNode> getUnionNodeFields() {
        return fields;
    }

    public void addField(String name, UnionNode node) {
        if (unionClass != null) {
            unionClass.addField(name, node);
        }
        if (this.fields.containsKey(name)) {
            this.solver.union(this.fields.get(name), node);
        } else {
            this.fields.put(name, node);
        }
    }
}
