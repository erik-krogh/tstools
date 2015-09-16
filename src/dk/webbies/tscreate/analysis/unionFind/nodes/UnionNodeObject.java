package dk.webbies.tscreate.analysis.unionFind.nodes;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class UnionNodeObject extends UnionNodeWithFields {
    private String typeName = null;
    public Map<String, UnionNode> objectFields = new HashMap<>();

    public UnionNodeObject() {

    }

    public Map<String, UnionNode> getObjectFields() {
        return objectFields;
    }

    // For objects, that have a known type name from the standard library.
    public UnionNodeObject(String typeName) {
        this.typeName = typeName;
    }

    public void addField(String fieldName, UnionNode node) {
        this.objectFields.put(fieldName, node);
        super.addField("field-" + fieldName, node);
    }

    public String getTypeName() {
        return typeName;
    }
}
