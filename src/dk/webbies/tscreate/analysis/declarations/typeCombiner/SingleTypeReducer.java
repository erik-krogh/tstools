package dk.webbies.tscreate.analysis.declarations.typeCombiner;

import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.UnionDeclarationType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 23-11-2015.
 */
public abstract class SingleTypeReducer<T extends DeclarationType, S extends DeclarationType> implements SingleTypeReducerInterface<T, S> {
    private final Map<DeclarationType, List<DeclarationType>> originals;

    protected SingleTypeReducer(Map<DeclarationType, List<DeclarationType>> originals) {
        this.originals = originals;
    }

    @Override
    public final DeclarationType reduce(T one, S two) {
        DeclarationType result = reduceIt(one, two);
        if (result == null) {
            return null;
        }
        if (result instanceof UnionDeclarationType) {
            UnionDeclarationType union = (UnionDeclarationType) result;
            assert union.getTypes().size() == 2;
            DeclarationType oneResult = union.getTypes().get(0);
            DeclarationType twoResult = union.getTypes().get(1);
            if (two.getClass().isInstance(oneResult)) {
                DeclarationType tmp = oneResult;
                oneResult = twoResult;
                twoResult = tmp;
            }
            assert one.getClass().isInstance(oneResult);
            assert two.getClass().isInstance(twoResult);
            if (one != oneResult) {
                if (originals.containsKey(one)) {
                    originals.put(oneResult, originals.get(one));
                } else {
                    originals.put(oneResult, Collections.singletonList(one));
                }
            }
            if (two != twoResult) {
                if (originals.containsKey(two)) {
                    originals.put(twoResult, originals.get(two));
                } else {
                    originals.put(twoResult, Collections.singletonList(two));
                }
            }
        } else if (result != one && result != two) {
            ArrayList<DeclarationType> originalsList = new ArrayList<>();
            assert !originals.containsKey(result);
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

    protected abstract DeclarationType reduceIt(T one, S two);

}
