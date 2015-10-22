package dk.webbies.tscreate.analysis.declarations.types.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.types.CombinationType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.FunctionType;
import dk.webbies.tscreate.analysis.declarations.types.InterfaceType;
import dk.webbies.tscreate.analysis.declarations.types.typeCombiner.TypeReducer;

/**
 * Created by Erik Krogh Kristensen on 18-10-2015.
 */
public class InterfaceFunctionReducer implements SingleTypeReducer<FunctionType, InterfaceType> {
    private final TypeReducer combiner;

    public InterfaceFunctionReducer(TypeReducer combiner) {
        this.combiner = combiner;
    }

    @Override
    public Class<FunctionType> getAClass() {
        return FunctionType.class;
    }

    @Override
    public Class<InterfaceType> getBClass() {
        return InterfaceType.class;
    }

    @Override
    public DeclarationType reduce(FunctionType functionType, InterfaceType interfaceType) {
        CombinationType newFunction = new CombinationType(combiner, functionType, interfaceType.function);
        interfaceType.function = newFunction;
        return interfaceType;
    }
}
