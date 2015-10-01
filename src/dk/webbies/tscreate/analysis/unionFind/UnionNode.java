package dk.webbies.tscreate.analysis.unionFind;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public abstract class UnionNode {
    UnionNode parent;
    int rank = 0;
    UnionClass unionClass;

    public UnionClass getUnionClass() {
        if (parent == null) {
            throw new NullPointerException();
        }
        return findParent().unionClass;
    }

    private UnionNode findParent() {
        if (this.parent == null) {
            throw new RuntimeException();
        }
        while (this.parent != this.parent.parent) {
            this.parent = this.parent.parent;
        }
        return this.parent;
    }
}
