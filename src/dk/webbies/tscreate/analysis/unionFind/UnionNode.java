package dk.webbies.tscreate.analysis.unionFind;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public abstract class UnionNode {
    UnionNode parent;
    int rank = 0;
    UnionClass unionClass;

    private final UnionFindSolver solver;

    private static int instanceCounter = 0;
    private final int counter;

    public UnionNode(UnionFindSolver solver) {
        this.counter = instanceCounter++;
        this.solver = solver;
    }

    public UnionClass getUnionClass() {
        if (parent == null) {
            solver.add(this);
        }
        return findParent().unionClass;
    }

    public UnionNode findParent() {
        if (this.parent == null) {
            return this;
        }
        while (this.parent != this.parent.parent) {
            this.parent = this.parent.parent;
        }
        return this.parent;
    }

    public abstract void addTo(UnionClass unionClass);

    public UnionFeature getFeature() {
        return getUnionClass().getFeature();
    }
}
