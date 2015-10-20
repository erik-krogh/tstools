package dk.webbies.tscreate.analysis.unionFind;

/**
 * Created by Erik Krogh Kristensen on 04-09-2015.
 */
public class NonVoidNode extends UnionNode {

    @Override
    public void addTo(UnionClass unionClass) {
        unionClass.getFeature().nonVoid = true;
    }
}
