package dk.webbies.tscreate.analysis.declarations.typeCombiner;

import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;

/**
 * Created by erik1 on 12-02-2016.
 */
public interface SameTypeSingleInstanceReducer<T extends DeclarationType> {
    DeclarationType reduce(T declarationType);

    Class<T> getTheClass();
}
