package dk.webbies.tscreate.analysis.unionFind.nodes;

import dk.webbies.tscreate.jsnapconvert.Snap;

/**
 * Created by Erik Krogh Kristensen on 09-09-2015.
 */
public class HasPrototypeUnionNode implements UnionNode {
    private Snap.Obj prototype;

    public HasPrototypeUnionNode(Snap.Obj prototype) {
        this.prototype = prototype;
    }

    public Snap.Obj getPrototype() {
        return prototype;
    }
}
