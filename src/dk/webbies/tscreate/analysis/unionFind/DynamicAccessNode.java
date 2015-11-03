package dk.webbies.tscreate.analysis.unionFind;

/**
 * Created by Erik Krogh Kristensen on 09-09-2015.
 *
 * Encodes the type for a "foo[lookupExp]" expression, of type "returnType".
 */
public class DynamicAccessNode extends UnionNodeWithFields {
    private final UnionNode returnType;
    private final UnionNode lookupExp;

    public DynamicAccessNode(UnionNode returnType, UnionNode lookupExp, UnionFindSolver solver) {
        super(solver);
        this.returnType = returnType;
        addField("isIndexer-returnType", returnType);
        this.lookupExp = lookupExp;
        addField("isIndexer-lookupExp", lookupExp);
    }

    @Override
    public void addTo(UnionClass unionClass) {
        // UnionClass fields makes sure that even if i overwrite, it doesn't matter.
        unionClass.getFeature().dynamicAccessLookupExp = this.lookupExp;
        unionClass.getFeature().dynamicAccessReturnType = this.returnType;
    }
}
