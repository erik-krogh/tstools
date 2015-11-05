package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.CombinationType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.DynamicAccessType;

/**
 * Created by Erik Krogh Kristensen on 05-11-2015.
 */
public class DynamicAccessReducer implements SingleTypeReducer<DynamicAccessType, DynamicAccessType> {
    private final TypeReducer combiner;

    public DynamicAccessReducer(TypeReducer combiner) {
        this.combiner = combiner;
    }

    @Override
    public Class<DynamicAccessType> getAClass() {
        return DynamicAccessType.class;
    }

    @Override
    public Class<DynamicAccessType> getBClass() {
        return DynamicAccessType.class;
    }

    @Override
    public DeclarationType reduce(DynamicAccessType one, DynamicAccessType two) {
        CombinationType returnType = new CombinationType(combiner, one.getReturnType(), two.getReturnType());
        CombinationType lookupType = new CombinationType(combiner, one.getLookupType(), two.getLookupType());
        return new DynamicAccessType(lookupType, returnType);
    }
}
