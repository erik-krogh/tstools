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
    private Map<String, UnionNode> fields = null;
    public List<Runnable> callbacks = null;

    private UnionFeature feature = new UnionFeature(this);

    public Set<UnionNode> includes = null;
    public Set<UnionNode> includesUs = null;

    private int hasRunAtIteration = -1;
    private boolean waitingForCallback = false;

    public UnionNode representative;

    public UnionClass(UnionFindSolver solver, UnionNode node) {
        this.representative = node;
        this.solver = solver;
        node.addTo(this);

        if (node instanceof UnionNodeWithFields) {
            UnionNodeWithFields fieldNode = (UnionNodeWithFields) node;
            this.takeIn(fieldNode);
        }
    }

    public void takeIn(UnionClass other) {
        if (other == this) {
            return;
        }
        if (other == null) {
            throw new NullPointerException();
        }

        other.representative.unionClass = null;

        merge(other.fields);

        this.feature.takeIn(other.feature);

        this.makeIncludesPointToParent();
        other.makeIncludesPointToParent();

        if (other.includesUs != null && !other.includesUs.isEmpty()) {
            if (this.includesUs == null) {
                this.includesUs = new HashSet<>();
            }
            for (UnionNode includesOther : other.includesUs) {
                includesOther = includesOther.findParent();

                UnionClass otherUnionClass = includesOther.getUnionClass();
                otherUnionClass.makeIncludesPointToParent();
                if (otherUnionClass.includes != null) {
                    otherUnionClass.includes.remove(other.representative.findParent());
                    otherUnionClass.includes.add(this.representative);
                } else {
                    otherUnionClass.includes = new HashSet<>(Arrays.asList(this.representative));
                }
                this.includesUs.add(includesOther);
            }
        }

        if (this.includes != null) {
            this.includes.remove(this.representative);
            this.includes.remove(other.representative.findParent());
        }
        if (this.includesUs != null) {
            this.includesUs.remove(this.representative);
            this.includesUs.remove(other.representative.findParent());
        }

        if (other.includes != null && !other.includes.isEmpty()) {
            if (this.includes == null) {
                this.includes = new HashSet<>();
            }
            this.includes.addAll(other.includes);
        }

        if (other.callbacks != null && other.callbacks.size() > 0) {
            solver.removeDoneCallback(other);
            if (this.callbacks == null) {
                this.callbacks = new ArrayList<>();
            }
            this.callbacks.addAll(other.callbacks);
        }

        if (this.callbacks != null && this.callbacks.size() > 0 && !waitingForCallback) {
            solver.addDoneCallback(this);
        }

//        collapseCycles();
    }

    private void makeIncludesPointToParent() {
        this.representative = this.representative.findParent();
        if (this.includes != null) {
            makePointToParent(this.includes);
            this.includes.remove(this.representative);
        }
        if (this.includesUs != null) {
            makePointToParent(this.includesUs);
            this.includesUs.remove(this.representative);
        }
    }

    private void makePointToParent(Set<UnionNode> includes) {
        List<UnionNode> toAdd = new ArrayList<>();
        Iterator<UnionNode> includesIterator = includes.iterator();
        while (includesIterator.hasNext()) {
            UnionNode node = includesIterator.next();
            UnionNode parent = node.findParent();
            if (node != parent) {
                includesIterator.remove();
                toAdd.add(parent);
            }
        }

        toAdd.forEach(includes::add);
    }

    private void collapseCycles() {
        UnionClass.getStronglyConnectedComponents(Collections.singletonList(this)).forEach(collection -> {
            if (collection.size() > 1) {
                solver.union(collection.stream().map(clazz -> clazz.representative).collect(Collectors.toList()));
            }
        });
    }

    public static List<List<UnionClass>> getStronglyConnectedComponents(Collection<UnionClass> unionClasses) {
        Tarjan<TarjanNode> tarjan = new Tarjan<>();
        List<TarjanNode> nodes = unionClasses.stream().map(unionClass -> unionClass.tarjanNode).collect(Collectors.toList());
        List<List<TarjanNode>> components = tarjan.getSCComponents(nodes);
        return components.stream().map(list -> list.stream().map(TarjanNode::getUnionClass).collect(Collectors.toList())).collect(Collectors.toList());
    }

    void takeIn(UnionNodeWithFields node) {
        merge(node.getUnionNodeFields());
    }

    private void merge(Map<String, UnionNode> fields) {
        if (fields == null) {
            return;
        }
        for (Map.Entry<String, UnionNode> entry : fields.entrySet()) {
            addField(entry.getKey(), entry.getValue());
        }
    }

    public void addField(String key, UnionNode value) {
        if (this.fields == null) {
            this.fields = new HashMap<>();
        }
        if (this.fields.containsKey(key)) {
            solver.union(value, this.fields.get(key));
        } else {
            this.fields.put(key, value);
        }
    }

    public void addChangeCallback(Runnable callback) {
        if (this.callbacks == null) {
            this.callbacks = new ArrayList<>();
        }
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
        if (this.callbacks != null) {
            for (Runnable callback : new ArrayList<>(this.callbacks)) {
                callback.run();
            }
        }
        if (this.includesUs != null) {
            for (UnionNode includesUs : new ArrayList<>(this.includesUs)) {
                includesUs.getUnionClass().doneCallback(iteration);
            }
        }
    }

    public List<UnionClass> getReachable() {
        return getReachable((obj) -> obj);
    }

    public <T> List<T> getReachable(Function<UnionClass, T> mapFunc) {
        return new Tarjan<TarjanNode>().getReachableSet(this.tarjanNode).stream().map(TarjanNode::getUnionClass).map(mapFunc).collect(Collectors.toList());
    }


    private TarjanNode tarjanNode = new TarjanNode();

    public Map<String, UnionNode> getFields() {
        return fields;
    }

    private class TarjanNode extends Tarjan.Node<TarjanNode> {
        @Override
        public Collection<TarjanNode> getEdges() {
            if (UnionClass.this.includes == null) {
                return Collections.EMPTY_LIST;
            }
            return new MappedCollection<>(UnionClass.this.includes, node -> node.getUnionClass().tarjanNode);
        }

        private UnionClass getUnionClass() {
            return UnionClass.this;
        }
    }
}
