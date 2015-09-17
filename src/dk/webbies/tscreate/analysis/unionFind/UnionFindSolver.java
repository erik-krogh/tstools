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
 */

import dk.webbies.tscreate.analysis.unionFind.nodes.UnionNode;

import java.util.*; // For Map, HashMap


/**
 * A class representing the union-find abstraction.
 *
 * @author Keith Schwarz (htiek@cs.stanford.edu)
 *
 * Modified by Erik Krogh Kristensen, for use in TSCreate (working title).
 */
public class UnionFindSolver {
    private Set<Runnable> doneCallbacks = new HashSet<>();

    int iteration = 1;
    public void finish() {
        while (doneCallbacks.size() > 0) {
            int count = iteration++;
//            System.out.println(count + " (" + doneCallbacks.size() + ")");
            for (Runnable callback : new ArrayList<>(doneCallbacks)) {
                doneCallbacks.remove(callback);
                callback.run();
            }
        }
    }

    /**
     * A utility struct holding an an object's parent and rank.
     */
    private static final class Link<T> {
        public T parent;
        public int rank = 0;
        public UnionClass unionClass;

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

        link.unionClass = new UnionClass(this, elem);
        return true;
    }

    public void runWhenChanged(UnionNode node, Runnable callback) {
        elems.get(recFind(node)).unionClass.addChangeCallback(callback);
    }

    void addDoneCallback(Runnable callback) {
        this.doneCallbacks.add(callback);
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
        if (info == null) { // TODO: This happens with the TypeScript inheritance example.
            this.add(elem);
            info = elems.get(elem);
        }

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

    public Map<UnionNode, UnionClass> getUnionClasses() {
        Map<UnionNode, UnionClass> classes = new HashMap<>();

        for (UnionNode node : elems.keySet()) {
            UnionClass UnionClass = elems.get(recFind(node)).unionClass;
            classes.put(node, UnionClass);
        }

        return classes;
    }

    public UnionClass getUnionClass(UnionNode node) {
        return elems.get(recFind(node)).unionClass;
    }

    public void union(UnionNode one, List<UnionNode> list) {
        for (UnionNode unionNode : list) {
            this.union(one, unionNode);
        }
    }

    /**
     * Given two elements, unions together the sets containing those
     * elements.  If either element is not contained in the set,
     * throws a NoSuchElementException.
     *
     * @param one The first element to link.
     * @param two The second element to link.
     * @throws NoSuchElementException If either element does not exist.
     * @return UnionNode, returns the first argument, for chaining.
     */
    public UnionNode union(UnionNode one, UnionNode two) {
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
        if (oneLink == twoLink) return one;

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
            oneLink.unionClass.mergeWith(twoLink.unionClass);
            twoLink.unionClass = null;
        } else {
            // TwoLink is the representative.
            twoLink.unionClass.mergeWith(oneLink.unionClass);
            oneLink.unionClass = null;
        }

        return one;
    }
}
