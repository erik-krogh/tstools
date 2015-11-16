package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.util.Pair;

import java.util.HashMap;
import java.util.Map;

import static dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType.Type.*;

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

    private static void register(PrimitiveDeclarationType.Type one, PrimitiveDeclarationType.Type two, PrimitiveDeclarationType.Type result) {
        PrimitiveDeclarationType.Type prev = reductionRules.put(new Pair<>(one, two), result);
        if (prev != null) {
            throw new RuntimeException();
        }
        prev = reductionRules.put(new Pair<>(two, one), result);
        if (prev != null) {
            throw new RuntimeException();
        }
    }

    private static final Map<Pair<PrimitiveDeclarationType.Type, PrimitiveDeclarationType.Type>, PrimitiveDeclarationType.Type> reductionRules = new HashMap<>();
    static {
        register(STRING_OR_NUMBER, STRING, STRING);
        register(STRING_OR_NUMBER, NUMBER, NUMBER);

        register(STRING, BOOLEAN, ANY);
        register(NUMBER, BOOLEAN, ANY);
        register(STRING_OR_NUMBER, BOOLEAN, ANY);

        register(NUMBER, STRING, STRING);
    }

    @Override
    public DeclarationType reduce(PrimitiveDeclarationType one, PrimitiveDeclarationType two) {
        if (one.getType() == two.getType()) {
            return one;
        }
        if (reductionRules.containsKey(new Pair<>(one.getType(), two.getType()))) {
            return PrimitiveDeclarationType.fromType(reductionRules.get(new Pair<>(one.getType(), two.getType())));
        } else {
            throw new RuntimeException("Dont know how to reduce primitive " + one.getType() + " with " + two.getType());
        }
    }
}
