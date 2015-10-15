package dk.webbies.tscreate.analysis.unionFind;

/**
 * Created by Erik Krogh Kristensen on 04-09-2015.
 */
public class NonVoidNode extends UnionNode {
    @Override
    public int hashCode() {
        return 39;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NonVoidNode;
    }
}
