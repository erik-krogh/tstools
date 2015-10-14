package dk.webbies.tscreate.analysis.unionFind;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 13-10-2015.
 */
public class GreatestCommonOfUnionNode extends UnionNodeWithFields {
    // A counter, so i can add each node as a field (and thus get the union-find-solve to know about it).
    // But meanwhile not union any 2 together, but having each field have a unique name.
    private static int counter = 0;

    private final List<UnionNode> nodes;
    public GreatestCommonOfUnionNode(UnionNode... nodes) {
        this.nodes = Arrays.asList(nodes).stream().filter(node -> node != null).collect(Collectors.toList());
        if (this.nodes.size() == 0) {
            throw new IllegalArgumentException();
        }
        for (UnionNode node : this.nodes) {
            addField("GC-Of-" + counter++, node);
        }

    }

    public List<UnionNode> getNodes() {
        return nodes;
    }
}
