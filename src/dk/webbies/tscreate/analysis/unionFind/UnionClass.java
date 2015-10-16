package dk.webbies.tscreate.analysis.unionFind;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public final class UnionClass {
    public UnionFindSolver solver;
    List<UnionNode> nodes = new ArrayList<>();
    Map<String, UnionNode> fields = new HashMap<>();
    public final List<Runnable> callbacks = new ArrayList<>();

    private int hasRunAtIteration = -1;

    public UnionClass(UnionFindSolver solver, UnionNode... nodes) {
        this.solver = solver;
        for (UnionNode node : nodes) {
            if (!(node instanceof EmptyUnionNode || node instanceof IsIndexedUnionNode || node instanceof IndexerExpUnionNode /*TODO: Do not ignore. */)) {
                this.nodes.add(node);
            }
            if (node instanceof UnionNodeWithFields) {
                UnionNodeWithFields fieldNode = (UnionNodeWithFields) node;
                fieldNode.unionClass = this;
                this.takeIn(fieldNode);
            }
        }
    }

    public void mergeWith(UnionClass other) {
        merge(other.fields);
        this.nodes.addAll(other.nodes);
        if (other.nodes.size() > 0 && this.nodes.size() > 0) {
            int currentIteration = solver.iteration;
            // TODO: Test this somehow.
            if (this.hasRunAtIteration < currentIteration || true) {
                for (Runnable callback : callbacks) {
                    solver.addDoneCallback(callback);
                }
                this.hasRunAtIteration = currentIteration;
            }
            if (other.hasRunAtIteration < currentIteration || true) {
                for (Runnable callback : other.callbacks) {
                    solver.addDoneCallback(callback);
                }
                other.hasRunAtIteration = currentIteration;
            }

        }

        this.callbacks.addAll(other.callbacks);
    }

    void takeIn(UnionNodeWithFields node) {
        merge(node.getUnionNodeFields());
    }

    private void merge(Map<String, UnionNode> fields) {
        for (Map.Entry<String, UnionNode> entry : fields.entrySet()) {
            addField(entry.getKey(), entry.getValue());
        }
    }

    public void addField(String key, UnionNode value) {
        if (this.fields.containsKey(key)) {
            solver.union(value, this.fields.get(key));
        } else {
            solver.add(value);
            this.fields.put(key, value);
        }
    }

    public Collection<UnionNode> getNodes() {
        return nodes;
    }

    public void addChangeCallback(Runnable callback) {
        this.callbacks.add(callback);
        solver.addDoneCallback(callback); // Run at least once.
    }
}
