package dk.webbies.tscreate.analysis.declarations.typeCombiner;

import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;

/**
 * Created by Erik Krogh Kristensen on 17-10-2015.
 */
public interface SingleTypeReducerInterface<A extends DeclarationType, B extends DeclarationType> {
    Class<A> getAClass();
    Class<B> getBClass();
    DeclarationType reduce(A a, B b);
}
