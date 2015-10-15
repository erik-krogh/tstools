package dk.webbies.tscreate.analysis.unionFind;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 *
 * This UnionNode is used everywhere to combine UnionNodes.
 */
public class EmptyUnionNode extends UnionNode{
    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EmptyUnionNode;
    }
}
