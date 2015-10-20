package dk.webbies.tscreate.analysis.unionFind;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public final class UnionClass {
    public UnionFindSolver solver;
    Map<String, UnionNode> fields = new HashMap<>();
    public final List<Runnable> callbacks = new ArrayList<>();

    private UnionFeature feature = new UnionFeature(this);

    public final Set<UnionClass> includes = new HashSet<>();
    public final Set<UnionClass> includesUs = new HashSet<>();

    private int hasRunAtIteration = -1;
    private boolean waitingForCallback = false;

    public UnionClass(UnionFindSolver solver, UnionNode... nodes) {
        this.solver = solver;
        for (UnionNode node : nodes) {
            node.addTo(this);

            if (node instanceof UnionNodeWithFields) {
                UnionNodeWithFields fieldNode = (UnionNodeWithFields) node;
                fieldNode.unionClass = this;
                this.takeIn(fieldNode);
            }
        }
    }

    public void takeIn(UnionClass other) {
        merge(other.fields);

        this.feature.takeIn(other.feature);

        for (UnionClass includesOther : other.includesUs) {
            if (!includesOther.includes.remove(other)) {
                throw new RuntimeException(); // TODO: Remove this throw.
            }
            includesOther.includes.add(this);
        }

        this.includes.addAll(other.includes);

        if (other.callbacks.size() > 0) {
            solver.removeDoneCallback(other);
        }

        this.callbacks.addAll(other.callbacks);

        if (this.callbacks.size() > 0 && !waitingForCallback) {
            solver.addDoneCallback(this);
        }
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

    public void addChangeCallback(Runnable callback) {
        this.callbacks.add(callback);
        if (!waitingForCallback) {
            solver.addDoneCallback(this);
        }
    }

    public UnionFeature getFeature() {
        return feature;
    }

    public void doneCallback(int iteration) {
        if (iteration == hasRunAtIteration) {
            return;
        }
        hasRunAtIteration = iteration;
        waitingForCallback = false;
        for (Runnable callback : new ArrayList<>(this.callbacks)) {
            callback.run();
        }
        for (UnionClass includesUs : this.includesUs) {
            includesUs.doneCallback(iteration);
        }
    }
}
