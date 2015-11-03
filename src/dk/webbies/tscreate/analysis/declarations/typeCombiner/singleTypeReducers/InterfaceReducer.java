package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.CombinationType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.InterfaceType;

/**
 * Created by Erik Krogh Kristensen on 03-11-2015.
 */
public class InterfaceReducer implements SingleTypeReducer<InterfaceType, InterfaceType> {
    private final TypeReducer combiner;

    public InterfaceReducer(TypeReducer combiner) {
        this.combiner = combiner;
    }

    @Override
    public Class<InterfaceType> getAClass() {
        return InterfaceType.class;
    }

    @Override
    public Class<InterfaceType> getBClass() {
        return InterfaceType.class;
    }

    @Override
    public DeclarationType reduce(InterfaceType one, InterfaceType two) {
        InterfaceType result = new InterfaceType();
        result.object = combine(one.object, two.object);
        result.function = combine(one.function, two.function);
        result.dynamicAccessLookupExp = combine(one.dynamicAccessLookupExp, two.dynamicAccessLookupExp);
        result.dynamicAccessReturnExp = combine(one.dynamicAccessReturnExp, two.dynamicAccessReturnExp);
        return result;
    }

    private DeclarationType combine(DeclarationType one, DeclarationType two) {
        if (one == null) {
            return two;
        }
        if (two == null) {
            return one;
        }
        return new CombinationType(combiner, one, two);
    }
}
