package dk.webbies.tscreate.analysis.unionFind.nodes;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class ObjectUnionNode extends UnionNodeWithFields {
    private Map<String, UnionNode> objectFields = new HashMap<>();
    private String typeName;

    public ObjectUnionNode() {

    }

    public Map<String, UnionNode> getObjectFields() {
        return objectFields;
    }

    public void addField(String fieldName, UnionNode node) {
        this.objectFields.put(fieldName, node);
        super.addField("field-" + fieldName, node);
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}
