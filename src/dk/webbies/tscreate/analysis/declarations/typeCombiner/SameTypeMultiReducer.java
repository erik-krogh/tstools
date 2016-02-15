package dk.webbies.tscreate.analysis.declarations.typeCombiner;

import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by erik1 on 11-02-2016.
 */
public abstract class SameTypeMultiReducer<T extends DeclarationType> extends SameTypeReducer<T> {
    protected SameTypeMultiReducer(Map<DeclarationType, List<DeclarationType>> originals) {
        super(originals);
    }

    @Override
    protected DeclarationType reduceIt(T one, T two) {
        return reduce(Arrays.asList(one, two));
    }

    public abstract DeclarationType reduce(Collection<T> types);
}
