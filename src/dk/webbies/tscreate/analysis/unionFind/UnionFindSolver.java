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

import java.util.*;


/**
 * A class representing the union-find abstraction.
 *
 * @author Keith Schwarz (htiek@cs.stanford.edu)
 *
 * Modified by Erik Krogh Kristensen, for use in TSCreate (working title).
 */
public class UnionFindSolver {
    private Set<UnionClass> doneCallbacks = new HashSet<>();

    int iteration = 1;

    public void finish() {
        while (doneCallbacks.size() > 0) {
            int count = iteration++;
            System.out.println(count + " (" + doneCallbacks.size() + ")");
            ArrayList<UnionClass> copy = new ArrayList<>(doneCallbacks);
            doneCallbacks.clear();
            for (UnionClass unionClass : copy) {
                unionClass.doneCallback(iteration);
            }
        }
    }

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
     * @param node The element to insert.
     * @return Whether the element was successfully inserted.
     * @throws NullPointerException If elem is null.
     */
    public boolean add(UnionNode node) {
        /* Check for null. */
        if (node == null)
            throw new NullPointerException("UnionFind does not support null.");

        /* Check whether this entry exists; fail if it does. */
        if (node.parent != null) {
            return false;
        }

        /* Otherwise add the element as its own parent. */
        node.parent = node;

        node.unionClass = new UnionClass(this, node);

        return true;
    }

    public void runWhenChanged(UnionNode node, Runnable callback) {
        recFind(node).unionClass.addChangeCallback(callback);
    }

    public void addDoneCallback(UnionClass unionClass) {
        this.doneCallbacks.add(unionClass);
    }

    public void removeDoneCallback(UnionClass unionClass) {
        this.doneCallbacks.remove(unionClass);
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
        if (elem.parent == null)
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
        /* If the element is its own parent, it's the representative of its
         * class and we should say so.
         */
        if (elem.parent == elem)
            return elem;

        /* Otherwise, look up the parent of this element, then compress the
         * path.
         */
        elem.parent = recFind(elem.parent);
        return elem.parent;
    }

    public UnionNode union(UnionNode... nodes) {
        return union(Arrays.asList(nodes));
    }

    public UnionNode union(List<UnionNode> nodes) {
        if (nodes.size() >= 1) {
            UnionNode first = nodes.get(0);
            for (int i = 1; i < nodes.size(); i++) {
                union(first, nodes.get(i));
            }
            return first;
        }
        return null;
    }

    public UnionNode union(UnionNode one, Collection<? extends UnionNode> list) {
        for (UnionNode unionNode : list) {
            this.union(one, unionNode);
        }
        return one;
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
        if (one.parent == null) {
            add(one);
        }
        if (two.parent == null) {
            add(two);
        }
        /* Get the link info for the parents.  This also handles the exception
         * guarantee.
         */
        one = recFind(one);
        two = recFind(two);

        /* If these are the same object, we're done. */
        if (one == two) return one;

        UnionNode oneParentBefore = one.parent;

        /* Otherwise, link the two.  We'll do a union-by-rank, where the parent
         * with the lower rank will merge with the parent with higher rank.
         */
        if (one.rank > two.rank) {
            /* Since each parent must link to itself, the value of oneLink.parent
             * is the representative of one.
             */
            two.parent = one.parent;
        } else if (one.rank < two.rank) {
            /* Same logic as above. */
            one.parent = two.parent;
        } else {
            /* Arbitrarily wire one to be the parent of two. */
            two.parent = one.parent;

            /* Bump up the representative of one to the next rank. */
            one.rank++;
        }

        if (one.parent == oneParentBefore) {
            // One is the representative.
            one.unionClass.representative = one;
            one.unionClass.takeIn(two.unionClass);
            two.unionClass = null;
        } else {
            // Two is the representative.
            two.unionClass.representative = two;
            two.unionClass.takeIn(one.unionClass);
            one.unionClass = null;
        }

        return one;
    }
}
