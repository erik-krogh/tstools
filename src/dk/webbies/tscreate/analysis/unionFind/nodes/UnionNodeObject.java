package dk.webbies.tscreate.analysis.unionFind.nodes;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class UnionNodeObject extends UnionNodeWithFields {
    public UnionNodeObject() {

    }

    public void addField(String fieldName, UnionNode node) {
        super.addField("field-" + fieldName, node);
    }
}
