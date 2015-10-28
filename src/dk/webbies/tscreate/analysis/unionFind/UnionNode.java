package dk.webbies.tscreate.analysis.unionFind;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
// TODO: After UnionFeature, UnionNode is not needed anymore, it is nothing but a step to the UnionFeature (bad name).
public abstract class UnionNode {
    UnionNode parent;
    int rank = 0;
    UnionClass unionClass;

    private final UnionFindSolver solver;

    private static int instanceCounter = 0;
    private final int counter;

    public UnionNode(UnionFindSolver solver) {
        this.solver = solver;
        this.counter = instanceCounter++;
        if (counter == 549035 || counter == 556729) {
            System.out.println();
        }
    }

    public UnionClass getUnionClass() {
        if (parent == null) {
            solver.add(this);
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

    public abstract void addTo(UnionClass unionClass);

    public UnionFeature getFeature() {
        return getUnionClass().getFeature();
    }
}
