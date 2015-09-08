package dk.webbies.tscreate.analysis.unionFind.nodes;


import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class PrimitiveUnionNode implements UnionNode {
    private PrimitiveDeclarationType type;

    private PrimitiveUnionNode(PrimitiveDeclarationType type) {
        this.type = type;
    }
    public static PrimitiveUnionNode number() {
        return new PrimitiveUnionNode(PrimitiveDeclarationType.NUMBER);
    }

    public PrimitiveDeclarationType getType() {
        return this.type;
    }
    public static PrimitiveUnionNode undefined() {
        return new PrimitiveUnionNode(PrimitiveDeclarationType.UNDEFINED);
    }

    public static PrimitiveUnionNode bool() {
        return new PrimitiveUnionNode(PrimitiveDeclarationType.BOOLEAN);
    }

    public static PrimitiveUnionNode string() {
        return new PrimitiveUnionNode(PrimitiveDeclarationType.STRING);
    }
}
