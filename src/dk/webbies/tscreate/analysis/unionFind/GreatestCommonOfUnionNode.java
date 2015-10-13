package dk.webbies.tscreate.analysis.unionFind;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Erik Krogh Kristensen on 13-10-2015.
 */
public class GreatestCommonOfUnionNode extends UnionNode {
    private final List<UnionNode> nodes;
    public GreatestCommonOfUnionNode(UnionNode... nodes) {
        this.nodes = Arrays.asList(nodes);
    }

    public List<UnionNode> getNodes() {
        return nodes;
    }
}
