package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.analysis.unionFind.PrimitiveNode;
import dk.webbies.tscreate.analysis.unionFind.UnionNode;
import dk.webbies.tscreate.jsnap.Snap;

/**
 * Created by erik1 on 08-02-2016.
 */
public interface HeapValueFactory {
    UnionNode fromValue(Snap.Value value);

    PrimitiveNode.Factory getPrimitivesFactory();

    UnionNode fromProperty(Snap.Property property);
}
