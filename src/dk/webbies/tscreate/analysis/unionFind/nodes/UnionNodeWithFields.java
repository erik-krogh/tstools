package dk.webbies.tscreate.analysis.unionFind.nodes;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public abstract class UnionNodeWithFields implements UnionNode {
    protected Map<String, UnionNode> fields = new HashMap<>();

    public Map<String, UnionNode> getFields() {
        return fields;
    }

    public void addField(String name, UnionNode node) {
        this.fields.put(name, node);
    }
}
