package dk.webbies.tscreate.analysis.declarations.typeCombiner;

import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 23-11-2015.
 */
public abstract class SameTypeReducer<T extends DeclarationType> implements SingleTypeReducer<T, T> {
    private final Map<DeclarationType, List<DeclarationType>> originals;

    protected SameTypeReducer(Map<DeclarationType, List<DeclarationType>> originals) {
        this.originals = originals;
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

    @Override
    public final DeclarationType reduce(T one, T two) {
        T result = reduceIt(one, two);
        if (result != one && result != two) {
            ArrayList<DeclarationType> originalsList = new ArrayList<>();
            originals.put(result, originalsList);
            if (originals.containsKey(one)) {
                originalsList.addAll(originals.get(one));
            } else {
                originalsList.add(one);
            }
            if (originals.containsKey(two)) {
                originalsList.addAll(originals.get(two));
            } else {
                originalsList.add(two);
            }

        }
        return result;
    }

    protected abstract T reduceIt(T one, T two);

}
