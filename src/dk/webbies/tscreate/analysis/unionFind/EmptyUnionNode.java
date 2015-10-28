package dk.webbies.tscreate.analysis.unionFind;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 *
 * This UnionNode is used everywhere to combine UnionNodes.
 */
public class EmptyUnionNode extends UnionNode{
    public EmptyUnionNode(UnionFindSolver solver) {
        super(solver);
    }

    @Override
    public void addTo(UnionClass unionClass) {
        // Do nothing...
    }
}
