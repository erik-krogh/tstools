package dk.webbies.tscreate.analysis.unionFind;

import dk.webbies.tscreate.util.MappedCollection;
import dk.webbies.tscreate.util.Tarjan;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public UnionNode representative;

    public UnionClass(UnionFindSolver solver, UnionNode node) {
        this.representative = node;
        this.solver = solver;
        node.addTo(this);

        if (node instanceof UnionNodeWithFields) {
            UnionNodeWithFields fieldNode = (UnionNodeWithFields) node;
            fieldNode.unionClass = this;
            this.takeIn(fieldNode);
        }
    }

    public void takeIn(UnionClass other) {
        merge(other.fields);

        this.feature.takeIn(other.feature);

        for (UnionClass includesOther : other.includesUs) {
            includesOther.includes.remove(other);
            includesOther.includes.add(this);
            this.includesUs.add(includesOther);
        }
        this.includes.remove(this);
        this.includes.remove(other);
        this.includesUs.remove(this);
        this.includesUs.remove(other);

        this.includes.addAll(other.includes);

        if (other.callbacks.size() > 0) {
            solver.removeDoneCallback(other);
        }

        this.callbacks.addAll(other.callbacks);

        if (this.callbacks.size() > 0 && !waitingForCallback) {
            solver.addDoneCallback(this);
        }

        collapseCycles();
    }


    private void collapseCycles() {
        Tarjan<TarjanNode> tarjan = new Tarjan<>();
        List<List<TarjanNode>> components = tarjan.getSCComponents(this.tarjanNode);
        for (List<TarjanNode> component : components) {
            if (component.size() > 1) {
                solver.union(component.stream().map(node -> node.getUnionClass().representative).collect(Collectors.toList()));
            }
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

    public <T> List<T> getReachable(Function<UnionClass, T> mapFunc) {
        if (mapFunc == null) {
            mapFunc = (x) -> (T)x;
        }
        return new Tarjan<TarjanNode>().getReachableSet(this.tarjanNode).stream().map(TarjanNode::getUnionClass).map(mapFunc).collect(Collectors.toList());
    }


    private TarjanNode tarjanNode = new TarjanNode();
    private class TarjanNode extends Tarjan.Node<TarjanNode> {
        @Override
        public Collection<TarjanNode> getEdges() {
            return new MappedCollection<>(UnionClass.this.includes, unionClass -> unionClass.tarjanNode);
        }

        private UnionClass getUnionClass() {
            return UnionClass.this;
        }
    }
}
