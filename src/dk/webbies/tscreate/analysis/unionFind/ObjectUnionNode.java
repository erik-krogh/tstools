package dk.webbies.tscreate.analysis.unionFind;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class ObjectUnionNode extends UnionNodeWithFields {
    private Map<String, UnionNode> objectFields = new HashMap<>();
    private String typeName;

    private static int instanceCounter = 0;
    public final int counter;
    public ObjectUnionNode() {
        this.counter = instanceCounter++;
    }

    @Override
    public void addTo(UnionClass unionClass) {
        UnionFeature feature = unionClass.getFeature();
        this.objectFields.forEach((name, node) -> {
            if (!feature.objectFields.containsKey(name)) {
                feature.objectFields.put(name, node);
            }
        });
        feature.typeNames.add(this.typeName);
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
