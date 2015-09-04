package dk.webbies.tscreate.analysis.unionFind.nodes;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class PrimitiveUnionNode implements UnionNode {
    public enum Type {
        NUMBER, BOOL, STRING, NULL;

    }

    private Type type;
    private PrimitiveUnionNode(Type type) {
        this.type = type;
    }

    public static PrimitiveUnionNode number() {
        return new PrimitiveUnionNode(Type.NUMBER);
    }
    public Type getType() {
        return type;
    }

    public static UnionNode bool() {
        return new PrimitiveUnionNode(Type.BOOL);
    }

    public static UnionNode string() {
        return new PrimitiveUnionNode(Type.STRING);
    }

    public static UnionNode nullType() {
        return new PrimitiveUnionNode(Type.NULL);
    }
}
