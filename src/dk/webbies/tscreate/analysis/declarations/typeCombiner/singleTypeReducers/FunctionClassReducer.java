package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.SingleTypeReducerInterface;
import dk.webbies.tscreate.analysis.declarations.types.ClassType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.FunctionType;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;

/**
 * Created by Erik Krogh Kristensen on 18-10-2015.
 */
public class FunctionClassReducer implements SingleTypeReducerInterface<FunctionType, ClassType> {
    private final TypeReducer combiner;

    public FunctionClassReducer(TypeReducer combiner) {
        this.combiner = combiner;
    }

    @Override
    public Class<FunctionType> getAClass() {
        return FunctionType.class;
    }

    @Override
    public Class<ClassType> getBClass() {
        return ClassType.class;
    }

    @Override
    public DeclarationType reduce(FunctionType functionType, ClassType classType) {
        // Same reasoning as in ClassObjectReducer.
        return classType;
    }
}
