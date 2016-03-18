package dk.webbies.tscreate.analysis.declarations.typeCombiner;

import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 23-11-2015.
 */
public abstract class SameTypeReducer<T extends DeclarationType> extends SingleTypeReducer<T, T> {
    protected SameTypeReducer(Map<DeclarationType, List<DeclarationType>> originals) {
        super(originals);
    }

    @Override
    public final Class<T> getAClass() {
        return getTheClass();
    }

    @Override
    public final Class<T> getBClass() {
        return getTheClass();
    }

    public abstract Class<T> getTheClass();
}
