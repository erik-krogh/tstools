package dk.webbies.tscreate.analysis.unionFind;


import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.Collections;
import java.util.HashSet;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class PrimitiveNode extends UnionNode {
    private PrimitiveDeclarationType type;

    public PrimitiveNode(PrimitiveDeclarationType type, UnionFindSolver solver) {
        super(solver);
        this.type = type;
    }

    public PrimitiveDeclarationType getType() {
        return this.type;
    }

    @Override
    public void addTo(UnionClass unionClass) {
        UnionFeature feature = unionClass.getFeature();
        if (feature.primitives == null) {
            feature.primitives = new HashSet<>();
        }
        feature.primitives.add(this.type.getType());
    }

    public interface Factory {
        UnionNode number();

        UnionNode undefined();

        UnionNode bool();

        UnionNode string();

        UnionNode any();

        UnionNode stringOrNumber();

        UnionNode nonVoid();

        UnionNode function();

        UnionNode array();
    }
}
