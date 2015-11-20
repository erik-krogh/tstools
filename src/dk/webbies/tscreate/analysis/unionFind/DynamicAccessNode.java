package dk.webbies.tscreate.analysis.unionFind;

/**
 * Created by Erik Krogh Kristensen on 09-09-2015.
 *
 * Encodes the type for a "foo[lookupExp]" expression, of type "returnType".
 */
public class DynamicAccessNode extends UnionNodeWithFields {
    private final UnionNode returnType;
    private final UnionNode lookupExp;

    public static final String RETURN_TYPE_KEY = "isIndexer-returnType";
    public static final String LOOKUP_EXP_KEY = "isIndexer-lookupExp";

    public DynamicAccessNode(UnionNode returnType, UnionNode lookupExp, UnionFindSolver solver) {
        super(solver);
        this.returnType = returnType;
        addField(RETURN_TYPE_KEY, returnType);
        this.lookupExp = lookupExp;
        addField(LOOKUP_EXP_KEY, lookupExp);
    }

    public UnionNode getReturnType() {
        return returnType;
    }

    public UnionNode getLookupExp() {
        return lookupExp;
    }

    @Override
    public void addTo(UnionClass unionClass) {
        // UnionClass fields makes sure that even if i overwrite, it doesn't matter.
        unionClass.getFeature().dynamicAccessLookupExp = this.lookupExp;
        unionClass.getFeature().dynamicAccessReturnType = this.returnType;
    }
}
