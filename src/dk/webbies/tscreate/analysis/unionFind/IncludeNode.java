package dk.webbies.tscreate.analysis.unionFind;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 13-10-2015.
 */
public class IncludeNode extends UnionNode {
    private final List<UnionNode> nodes;
    public IncludeNode(UnionFindSolver solver, UnionNode... nodes) {
        super(solver);
        this.nodes = Arrays.asList(nodes).stream().filter(node -> node != null).collect(Collectors.toList());
        if (this.nodes.size() == 0) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void addTo(UnionClass unionClass) {
        for (UnionNode node : this.nodes) {
            if (node.getUnionClass() == null) {
                unionClass.solver.add(node);
            }
            UnionClass otherClass = node.getUnionClass();
            if (unionClass.includes == null) {
                unionClass.includes = new HashSet<>();
            }
            unionClass.includes.add(otherClass);

            if (otherClass.includesUs == null) {
                otherClass.includesUs = new HashSet<>();
            }
            otherClass.includesUs.add(unionClass);
        }
    }
}
