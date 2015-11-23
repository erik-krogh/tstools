package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.SingleTypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;

/**
 * Created by Erik Krogh Kristensen on 05-11-2015.
 */
public class CantReduceReducer<A extends DeclarationType, B extends DeclarationType> implements SingleTypeReducer<A, B> {
    private final Class<A> aClass;
    private final Class<B> bClass;

    public CantReduceReducer(Class<A> aClass, Class<B> bClass) {
        this.aClass = aClass;
        this.bClass = bClass;
    }

    @Override
    public Class<A> getAClass() {
        return aClass;
    }

    @Override
    public Class<B> getBClass() {
        return bClass;
    }

    @Override
    public DeclarationType reduce(A a, B b) {
        return null;
    }
}
