package dk.webbies.tscreate.analysis.declarations.types.typeCombiner.typeHandlers;

import dk.webbies.tscreate.Util;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;

import java.util.HashMap;
import java.util.Map;

import static dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType.*;

/**
 * Created by Erik Krogh Kristensen on 18-10-2015.
 */
public class PrimitiveReducer implements SingleTypeReducer<PrimitiveDeclarationType, PrimitiveDeclarationType> {
    @Override
    public Class<PrimitiveDeclarationType> getAClass() {
        return PrimitiveDeclarationType.class;
    }

    @Override
    public Class<PrimitiveDeclarationType> getBClass() {
        return PrimitiveDeclarationType.class;
    }

    private static void register(PrimitiveDeclarationType one, PrimitiveDeclarationType two, PrimitiveDeclarationType result) {
        PrimitiveDeclarationType prev = reductionRules.put(new Util.Pair<>(one, two), result);
        if (prev != null) {
            throw new RuntimeException();
        }
        prev = reductionRules.put(new Util.Pair<>(two, one), result);
        if (prev != null) {
            throw new RuntimeException();
        }
    }

    private static final Map<Util.Pair<PrimitiveDeclarationType, PrimitiveDeclarationType>, PrimitiveDeclarationType> reductionRules = new HashMap<>();
    static {
        register(STRING_OR_NUMBER, STRING, STRING);
        register(STRING_OR_NUMBER, NUMBER, NUMBER);

        register(STRING, BOOLEAN, ANY);
        register(NUMBER, BOOLEAN, ANY);
        register(STRING_OR_NUMBER, BOOLEAN, ANY);

        register(NUMBER, STRING, STRING);
    }

    @Override
    public DeclarationType reduce(PrimitiveDeclarationType one, PrimitiveDeclarationType two) throws CantReduceException {
        if (reductionRules.containsKey(new Util.Pair<>(one, two))) {
            PrimitiveDeclarationType result = reductionRules.get(new Util.Pair<>(one, two));
            if (result == null) {
                throw new CantReduceException();
            }
            return result;
        } else {
            throw new RuntimeException("Dont know how to reduce primitive " + one + " with " + two);
        }
    }
}
