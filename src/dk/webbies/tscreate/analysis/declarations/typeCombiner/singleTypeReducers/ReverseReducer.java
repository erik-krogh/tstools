package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.SingleTypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;

/**
 * Created by Erik Krogh Kristensen on 17-10-2015.
 */
public class ReverseReducer<A extends DeclarationType, B extends DeclarationType> implements SingleTypeReducer<A, B> {
    private SingleTypeReducer<B, A> original;

    public ReverseReducer(SingleTypeReducer<B, A> original) {
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
