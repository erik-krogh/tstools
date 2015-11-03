package dk.webbies.tscreate.analysis.unionFind;

/**
 * Created by Erik Krogh Kristensen on 09-09-2015.
 */
public class IsIndexedUnionNode extends UnionNodeWithFields {
    private final UnionNode returnType;
    private final UnionNode lookupExp;

    public IsIndexedUnionNode(UnionNode returnType, UnionNode lookupExp, UnionFindSolver solver) {
        super(solver);
        this.returnType = returnType;
        addField("isIndexer-returnType", returnType);
        this.lookupExp = lookupExp;
        addField("isIndexer-lookupExp", lookupExp);
    }

    public UnionNode getReturnType() {
        return returnType;
    }

    public UnionNode getLookupExp() {
        return lookupExp;
    }

    @Override
    public void addTo(UnionClass unionClass) {
        // FIXME: Mark it or something here.
    }
}
