package dk.webbies.tscreate.analysis.unionFind;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class ObjectNode extends UnionNodeWithFields {
    public static final String FIELD_PREFIX = "field-";
    private Map<String, UnionNode> objectFields = new HashMap<>();
    private String typeName;

    public ObjectNode(UnionFindSolver solver) {
        super(solver);
    }

    @Override
    public void addTo(UnionClass unionClass) {
        UnionFeature feature = unionClass.getFeature();
        if (!this.objectFields.isEmpty() && feature.objectFields == null) {
            feature.objectFields = new HashMap<>();
        }
        this.objectFields.forEach((name, node) -> {
            if (!feature.objectFields.containsKey(name)) {
                feature.objectFields.put(name, node);
            }
        });

        if (this.typeName != null) {
            if (feature.typeNames == null) {
                feature.typeNames = new HashSet<>();
            }
            feature.typeNames.add(this.typeName);
        }
    }
    public void addField(String fieldName, UnionNode node) {
        this.objectFields.put(fieldName, node);
        super.addField(FIELD_PREFIX + fieldName, node);
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
