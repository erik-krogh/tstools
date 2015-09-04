package dk.webbies.tscreate.analysis.unionFind;

/************************************************************************
 * File: UnionFindSolver.java
 * Author: Keith Schwarz (htiek@cs.stanford.edu)
 *
 * An implementation of a union-find (disjoint set) data structure using
 * a disjoint-set forest.  This implementation implements the path
 * compression and union by rank optimizations, which results in very
 * efficient amortized cost for any sequence of operations.  In
 * particular, any m operations will complete in O(m a(m)), where a(m)
 * is the Ackermann inverse function.  This function grows incredibly
 * slowly, and is effectively a constant for any value of m less than
 * the number of atoms in the universe.
 *
 * Disjoint set structures can be used to implement relational
 * unification, Kruskal's MST algorithm, or Hindley-Milner type
 * inference.  They are also good at finding connected components of
 * an undirected graph.
 *
 * Modified by Erik Krogh Kristensen, for use in TSCreate (working title).
 */

import dk.webbies.tscreate.analysis.unionFind.nodes.UnionNode;
import dk.webbies.tscreate.analysis.unionFind.nodes.UnionNodeWithFields;

import java.util.*; // For Map, HashMap


/**
 * A class representing the union-find abstraction.
 *
 * @author Keith Schwarz (htiek@cs.stanford.edu)
 */
public class UnionFindSolver {
    /**
     * A utility struct holding an an object's parent and rank.
     */
    private static final class Link<T> {
        public T parent;
        public int rank = 0;
        public MasterNode masterNode;

        /**
         * Creates a new Link object with the specified parent.
         *
         * @param parent The initial value of the parent field.
         */
        Link(T parent) {
            this.parent = parent;
        }
    }

    /**
     * A map from objects in the UnionFindSolver structure to their associated
     * rank and parent.
     */
    private final Map<UnionNode, Link<UnionNode>> elems = new HashMap<>();

    /**
     * Creates a new UnionFindSolver structure that is initially empty.
     */
    public UnionFindSolver() {
        // Handled implicitly
    }

    /**
     * Creates a new UnionFindSolver structure that initially contains all of
     * the elements from the specified container.  Duplicate elements
     * will appear with multiplicity one.
     *
     * @param elems The elements to store in the UnionFindSolver structure.
     */
    public UnionFindSolver(Collection<? extends UnionNode> elems) {
        /* Iterate across the collection, adding each to this structure. */
        for (UnionNode elem: elems)
            add(elem);
    }

    /**
     * Inserts a new element into the UnionFindSolver structure that initially
     * is in its own partition.  If the element already exists, this
     * function returns false.  Otherwise, it returns true.
     *
     * @param elem The element to insert.
     * @return Whether the element was successfully inserted.
     * @throws NullPointerException If elem is null.
     */
    public boolean add(UnionNode elem) {
        /* Check for null. */
        if (elem == null)
            throw new NullPointerException("UnionFind does not support null.");

        /* Check whether this entry exists; fail if it does. */
        if (elems.containsKey(elem))
            return false;

        /* Otherwise add the element as its own parent. */
        Link<UnionNode> link = new Link<>(elem);
        elems.put(elem, link);

        link.masterNode = new MasterNode(this);
        if (elem instanceof UnionNodeWithFields) {
            UnionNodeWithFields fieldNode = (UnionNodeWithFields) elem;
            link.masterNode.takeIn(fieldNode);
        }
        return true;
    }

    /**
     * Given an element, returns the representative element of the set
     * containing that element.  If the element is not contained in the
     * structure, this method throws a NoSuchElementException.
     *
     * @param elem The element to look up.
     * @return The representative of the set containing the element.
     * @throws NoSuchElementException If the element does not exist.
     */
    public UnionNode find(UnionNode elem) {
        /* Check whether the element exists; fail if it doesn't. */
        if (!elems.containsKey(elem))
            throw new NoSuchElementException(elem + " is not an element.");

        /* Recursively search the structure and return the result. */
        return recFind(elem);
    }

    /**
     * Given an element which is known to be in the structure, searches
     * for its representative element and returns it, applying path
     * compression at each step.
     *
     * @param elem The element to look up.
     * @return The representative of the set containing it.
     */
    private UnionNode recFind(UnionNode elem) {
        /* Get the info on this object. */
        Link<UnionNode> info = elems.get(elem);

        /* If the element is its own parent, it's the representative of its
         * class and we should say so.
         */
        if (info.parent.equals(elem))
            return elem;

        /* Otherwise, look up the parent of this element, then compress the
         * path.
         */
        info.parent = recFind(info.parent);
        return info.parent;
    }

    public Map<UnionNode, List<UnionNode>> getUnionClasses() {
        Map<UnionNode, List<UnionNode>> classes = new HashMap<>();
        for (UnionNode node : elems.keySet()) {
            UnionNode parent = recFind(node);
            if (classes.containsKey(parent)) {
                classes.get(parent).add(node);
            } else {
                ArrayList<UnionNode> list = new ArrayList<>();
                list.add(node);
                classes.put(parent, list);
            }
            classes.put(node, classes.get(parent));
        }

        return classes;
    }

    /**
     * Given two elements, unions together the sets containing those
     * elements.  If either element is not contained in the set,
     * throws a NoSuchElementException.
     *
     * @param one The first element to link.
     * @param two The second element to link.
     * @throws NoSuchElementException If either element does not exist.
     */
    public void union(UnionNode one, UnionNode two) {
        if (one == null || two == null) {
            throw new RuntimeException("A unionNode cannot be null");
        }
        if (!elems.containsKey(one)) {
            add(one);
        }
        if (!elems.containsKey(two)) {
            add(two);
        }
        /* Get the link info for the parents.  This also handles the exception
         * guarantee.
         */
        Link<UnionNode> oneLink = elems.get(find(one));
        Link<UnionNode> twoLink = elems.get(find(two));

        /* If these are the same object, we're done. */
        if (oneLink == twoLink) return;

        UnionNode oneParentBefore = oneLink.parent;

        /* Otherwise, link the two.  We'll do a union-by-rank, where the parent
         * with the lower rank will merge with the parent with higher rank.
         */
        if (oneLink.rank > twoLink.rank) {
            /* Since each parent must link to itself, the value of oneLink.parent
             * is the representative of one.
             */
            twoLink.parent = oneLink.parent;
        } else if (oneLink.rank < twoLink.rank) {
            /* Same logic as above. */
            oneLink.parent = twoLink.parent;
        } else {
            /* Arbitrarily wire one to be the parent of two. */
            twoLink.parent = oneLink.parent;

            /* Bump up the representative of one to the next rank. */
            oneLink.rank++;
        }

        if (twoLink.parent == oneParentBefore) {
            // OneLink is the representative.
            oneLink.masterNode.mergeWith(twoLink.masterNode);
            twoLink.masterNode = null;
        } else {
            // TwoLink is the representative.
            twoLink.masterNode.mergeWith(oneLink.masterNode);
            oneLink.masterNode = null;
        }


    }

    private final class MasterNode {
        UnionFindSolver solver;
        Map<String, UnionNode> fields = new HashMap<>();
        public MasterNode(UnionFindSolver solver) {
            this.solver = solver;
        }

        void mergeWith(MasterNode other) {
            merge(other.fields);
        }

        void takeIn(UnionNodeWithFields node) {
            merge(node.getFields());
        }

        private void merge(Map<String, UnionNode> fields) {
            for (Map.Entry<String, UnionNode> entry : fields.entrySet()) {
                String key = entry.getKey();
                UnionNode value = entry.getValue();
                if (this.fields.containsKey(key)) {
                    solver.union(value, this.fields.get(key));
                } else {
                    this.fields.put(key, value);
                }
            }
        }
    }
}
