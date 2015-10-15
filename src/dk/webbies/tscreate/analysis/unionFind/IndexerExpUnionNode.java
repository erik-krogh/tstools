package dk.webbies.tscreate.analysis.unionFind;

/**
 * Created by Erik Krogh Kristensen on 08-09-2015.
 *
 * This is for the objects that are used as keys for an indexer.
 */
public class IndexerExpUnionNode extends UnionNode {
    @Override
    public int hashCode() {
        return 37;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IndexerExpUnionNode;
    }
}
