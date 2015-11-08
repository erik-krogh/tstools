package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.NamedObjectType;

/**
 * Created by Erik Krogh Kristensen on 08-11-2015.
 */
public class NamedObjectReducer implements SingleTypeReducer<NamedObjectType, NamedObjectType> {
    @Override
    public Class<NamedObjectType> getAClass() {
        return NamedObjectType.class;
    }

    @Override
    public Class<NamedObjectType> getBClass() {
        return NamedObjectType.class;
    }

    @Override
    public DeclarationType reduce(NamedObjectType one, NamedObjectType two) {
        if (one.getName().equals(two.getName())) {
            return one;
        } else {
            return null;
        }
    }
}
