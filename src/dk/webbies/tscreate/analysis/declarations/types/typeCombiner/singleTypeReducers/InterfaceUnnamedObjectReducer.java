package dk.webbies.tscreate.analysis.declarations.types.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.types.CombinationType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.InterfaceType;
import dk.webbies.tscreate.analysis.declarations.types.UnnamedObjectType;
import dk.webbies.tscreate.analysis.declarations.types.typeCombiner.TypeReducer;

/**
 * Created by Erik Krogh Kristensen on 18-10-2015.
 */
public class InterfaceUnnamedObjectReducer implements SingleTypeReducer<InterfaceType, UnnamedObjectType> {
    private final TypeReducer combiner;

    public InterfaceUnnamedObjectReducer(TypeReducer combiner) {
        this.combiner = combiner;
    }

    @Override
    public Class<InterfaceType> getAClass() {
        return InterfaceType.class;
    }

    @Override
    public Class<UnnamedObjectType> getBClass() {
        return UnnamedObjectType.class;
    }

    @Override
    public DeclarationType reduce(InterfaceType interfaceType, UnnamedObjectType unnamedObjectType) {
        CombinationType newObjectType = new CombinationType(combiner, interfaceType.object, unnamedObjectType);
        interfaceType.object = newObjectType;
        return interfaceType;
    }
}
