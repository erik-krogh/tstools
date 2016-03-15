package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.SameTypeReducer;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.CombinationType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.DynamicAccessType;
import dk.webbies.tscreate.util.Util;

import java.util.List;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 05-11-2015.
 */
public class DynamicAccessReducer extends SameTypeReducer<DynamicAccessType> {
    private final TypeReducer combiner;

    public DynamicAccessReducer(TypeReducer combiner, Map<DeclarationType, List<DeclarationType>> originals) {
        super(originals);
        this.combiner = combiner;
    }

    @Override
    public Class<DynamicAccessType> getTheClass() {
        return DynamicAccessType.class;
    }

    @Override
    public DynamicAccessType reduceIt(DynamicAccessType one, DynamicAccessType two) {
        CombinationType returnType = new CombinationType(combiner, one.getReturnType(), two.getReturnType());
        CombinationType lookupType = new CombinationType(combiner, one.getLookupType(), two.getLookupType());
        return new DynamicAccessType(lookupType, returnType, Util.concatSet(one.getNames(), two.getNames()));
    }
}
