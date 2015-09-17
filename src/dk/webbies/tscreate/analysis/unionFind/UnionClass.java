package dk.webbies.tscreate.analysis.unionFind;

import dk.webbies.tscreate.analysis.unionFind.nodes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public final class UnionClass {
    UnionFindSolver solver;
    List<UnionNode> nodes = new ArrayList<>();
    Map<String, UnionNode> fields = new HashMap<>();
    List<Runnable> callbacks = new ArrayList<>();

    private int hasRunAtIteration = -1;

    UnionClass(UnionFindSolver solver, UnionNode node) {
        this.solver = solver;
        if (!(node instanceof EmptyUnionNode || node instanceof IsIndexedUnionNode || node instanceof IndexerExpUnionNode /*TODO: Do not ignore. */)) {
            this.nodes.add(node);
        }
        if (node instanceof UnionNodeWithFields) {
            UnionNodeWithFields fieldNode = (UnionNodeWithFields) node;
            fieldNode.unionClass = this;
            this.takeIn(fieldNode);
        }
    }

    void mergeWith(UnionClass other) {
        merge(other.fields);
        this.nodes.addAll(other.nodes);
        if (other.nodes.size() > 0 && this.nodes.size() > 0) {
            int currentIteration = solver.iteration;
            // TODO: Test this somehow.
            if (this.hasRunAtIteration < currentIteration) {
                for (Runnable callback : callbacks) {
                    solver.addDoneCallback(callback);
                }
                this.hasRunAtIteration = currentIteration;
            }
            if (other.hasRunAtIteration < currentIteration) {
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
            String key = entry.getKey();
            UnionNode value = entry.getValue();
            addField(key, value);
        }
    }

    public void addField(String key, UnionNode value) {
        if (this.fields.containsKey(key)) {
            solver.union(value, this.fields.get(key));
        } else {
            this.fields.put(key, value);
        }
    }

    public List<UnionNode> getNodes() {
        return nodes;
    }

    public Map<String, UnionNode> getFields() {
        return fields;
    }

    public void addChangeCallback(Runnable callback) {
        this.callbacks.add(callback);
        solver.addDoneCallback(callback); // Run at least once.
    }
}
