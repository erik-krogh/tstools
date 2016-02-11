package dk.webbies.tscreate.analysis.declarations.typeCombiner;

import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;

import java.util.Collection;

/**
 * Created by erik1 on 11-02-2016.
 */
public interface SameTypeMultiReducer<T extends DeclarationType> extends SingleTypeReducer<T, T> {
    DeclarationType reduce(Collection<T> types);
}
