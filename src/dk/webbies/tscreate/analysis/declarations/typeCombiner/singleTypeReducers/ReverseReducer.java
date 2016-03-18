package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.SingleTypeReducerInterface;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;

/**
 * Created by Erik Krogh Kristensen on 17-10-2015.
 */
public class ReverseReducer<A extends DeclarationType, B extends DeclarationType> implements SingleTypeReducerInterface<A, B> {
    private SingleTypeReducerInterface<B, A> original;

    public ReverseReducer(SingleTypeReducerInterface<B, A> original) {
        this.original = original;
    }
    @Override
    public Class<A> getAClass() {
        return original.getBClass();
    }

    @Override
    public Class<B> getBClass() {
        return original.getAClass();
    }

    @Override
    public DeclarationType reduce(A a, B b) {
        return original.reduce(b, a);
    }
}
