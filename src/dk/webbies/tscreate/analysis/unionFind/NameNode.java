package dk.webbies.tscreate.analysis.unionFind;

import dk.webbies.tscreate.util.Util;

import java.util.HashSet;

/**
 * Created by erik1 on 15-03-2016.
 */
public class NameNode extends UnionNode {
    private final String name;

    public NameNode(UnionFindSolver solver, String name) {
        super(solver);
        this.name = name;
    }

    @Override
    public void addTo(UnionClass unionClass) {
        UnionFeature feature = unionClass.getFeature();
        if (!Util.isInteger(this.name)) {
            if (feature.names == null) {
                feature.names = new HashSet<>();
            }
            feature.names.add(this.name);
        }
    }
}
