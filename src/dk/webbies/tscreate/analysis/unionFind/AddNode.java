package dk.webbies.tscreate.analysis.unionFind;

/**
 * Created by Erik Krogh Kristensen on 04-09-2015.
 */
public class AddNode extends UnionNode {
    private final UnionNode lhs;
    private final UnionNode rhs;

    public AddNode(UnionNode lhs, UnionNode rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public UnionNode getLhs() {
        return lhs;
    }

    public UnionNode getRhs() {
        return rhs;
    }
}
