package dk.webbies.tscreate.analysis.unionFind;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public abstract class UnionNodeWithFields extends UnionNode {
    public UnionClass unionClass;

    protected Map<String, UnionNode> fields = new HashMap<>();

    public Map<String, UnionNode> getUnionNodeFields() {
        return fields;
    }

    public void addField(String name, UnionNode node) {
        if (unionClass != null) {
            unionClass.addField(name, node);
        }
        this.fields.put(name, node);
    }
}
