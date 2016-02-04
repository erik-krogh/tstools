package dk.webbies.tscreate.util;


/**
 *     Java Program to Implement Tarjan Algorithm
 *     Copy pasta from: http://www.sanfoundry.com/java-program-tarjan-algorithm/
 **/

import java.util.*;

/** class Tarjan **/
public class Tarjan<T extends Tarjan.Node<T>> {
    public static abstract class Node<T extends Node> {
        int low;
        int visited;

        public abstract Collection<T> getEdges();
    }

    /** preorder number counter **/
    private static int iterationCounter = 1;
    private int preCount;

    /**
     * function to get all strongly connected components
     **/
    public List<List<T>> getSCComponents(T graph) {
        return getSCComponents(Arrays.asList(graph), iterationCounter++);
    }

    /**
     * function to get all strongly connected components
     **/
    public List<List<T>> getSCComponents(List<T> graph) {
        return getSCComponents(graph, iterationCounter++);
    }

    /**
     * function to get all strongly connected components
     **/
    private List<List<T>> getSCComponents(List<T> graph, int iteration) {
        Deque<T> stack = new LinkedList<>();
        List<List<T>> sccComp = new ArrayList<>();

        for (T edge : graph) {
            if (edge.visited != iteration) {
                dfs(edge, iteration, stack, sccComp);
            }
        }

        return sccComp;
    }

    public List<T> getReachableSet(T graph) {
        return getReachableSet(graph, iterationCounter++);
    }

    /*
     * Finds how heep in the tree the different elements are.
     * The returned list of lists only contain the elements given as input.
     * The first element is the deepest in the tree (a leaf).
     */
    public List<List<T>> getLevels(Collection<T> elements) {
        return getLevels(elements, iterationCounter++);
    }

    private List<List<T>> getLevels(Collection<T> elements, int iteration) {
        for (T element : elements) {
            markLevel(element, iteration);
        }
        List<List<T>> result = new ArrayList<>();
        for (T element : elements) {
            for (int i = result.size(); i <= element.low; i++) {
                result.add(i, new ArrayList<T>());
            }
            result.get(element.low).add(element);
        }

        return result;
    }

    private int markLevel(T node, int iteration) {
        if (node.visited == iteration) {
            node.low = 0;
            return node.low;
        } else {
            node.visited = iteration;
            int max = 0;
            for (T edge : node.getEdges()) {
                max = Math.max(max, markLevel(edge, iteration));
            }
            node.low = max + 1;
            return node.low;
        }
    }

    private List<T> getReachableSet(T graph, int iteration) {
        ArrayList<T> result = new ArrayList<>();
        reachableDfs(graph, iteration, result);
        return result;
    }

    private void reachableDfs(T node, int iteration, List<T> result) {
        if (node.visited != iteration) {
            node.visited = iteration;
            result.add(node);
            for (T edge : node.getEdges()) {
                reachableDfs(edge, iteration, result);
            }
        }
    }

    /**
     * function dfs
     **/
    private void dfs(T v, int iteration, Deque<T> stack, List<List<T>> sccComp) {
        v.low = preCount++;
        v.visited = iteration;
        stack.push(v);
        int min = v.low;
        for (T edge : v.getEdges()) {
            if (edge.visited != iteration) {
                dfs(edge, iteration, stack, sccComp);
            }
            if (edge.low < min) {
                min = edge.low;
            }
        }

        if (min < v.low) {
            v.low = min;
            return;
        }

        List<T> component = new ArrayList<>();

        T w;
        do {
            w = stack.pop();
            component.add(w);
            w.low = Integer.MAX_VALUE;
        } while (w != v);

        sccComp.add(component);
    }

    private static class SimpleNode extends Node<SimpleNode> {
        List<SimpleNode> list = new ArrayList<>();
        private String descrip;

        public SimpleNode(String descrip) {
            this.descrip = descrip;
        }

        @Override
        public String toString() {
            return descrip;
        }

        @Override
        public Collection<SimpleNode> getEdges() {
            return list;
        }
    }

    /** main **/
    public static void main(String[] args) {
        SimpleNode node1 = new SimpleNode("1");
        SimpleNode node2 = new SimpleNode("2");
        SimpleNode node3 = new SimpleNode("3");

        node1.list.add(node2);
        node2.list.add(node3);

        Tarjan<SimpleNode> t = new Tarjan<>();
        System.out.println("\nSCC : ");
        /** print all strongly connected components **/
        List<List<SimpleNode>> scComponents = t.getLevels(Arrays.asList(node1, node2, node3));
        System.out.println(scComponents);
    }
}