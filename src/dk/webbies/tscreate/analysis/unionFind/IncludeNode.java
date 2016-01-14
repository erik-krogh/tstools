package dk.webbies.tscreate.analysis.unionFind;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 13-10-2015.
 */
public class IncludeNode extends UnionNode {
    private final List<UnionNode> nodes;
    public IncludeNode(UnionFindSolver solver, UnionNode... nodes) {
        this(solver, Arrays.asList(nodes));
    }

    public IncludeNode(UnionFindSolver solver, List<? extends UnionNode> nodes) {
        super(solver);
        this.nodes = nodes.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public void addTo(UnionClass unionClass) {
        for (UnionNode node : this.nodes) {
            node = node.findParent();
            UnionClass otherClass = node.getUnionClass();
            if (otherClass == unionClass) {
                throw new RuntimeException();
            }
            if (unionClass.includes == null) {
                unionClass.includes = new HashSet<>();
            }
            unionClass.includes.add(node);

            if (otherClass.includesUs == null) {
                otherClass.includesUs = new HashSet<>();
            }
            otherClass.includesUs.add(node);
        }
    }
}
