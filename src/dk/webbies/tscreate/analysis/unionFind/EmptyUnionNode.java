package dk.webbies.tscreate.analysis.unionFind;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 *
 * This UnionNode is used everywhere to combine UnionNodes.
 */
public class EmptyUnionNode extends UnionNode{

    private static int instanceCounter = 0;
    private final int counter;

    public EmptyUnionNode() {
        this.counter = instanceCounter++;
        if (this.counter == 2605) { // TODO:
            doNothing();
        }
    }

    private void doNothing() {

    }
}
