package dk.webbies.tscreate.analysis.declarations.types.typeCombiner;

import dk.webbies.tscreate.Util;
import dk.webbies.tscreate.analysis.declarations.types.ClassType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.UnionDeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.typeCombiner.typeHandlers.*;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Created by Erik Krogh Kristensen on 16-10-2015.
 */
public class TypeReducer {
    private Map<Util.Pair<Class<? extends DeclarationType>, Class<? extends DeclarationType>>, SingleTypeReducer> handlers = new HashMap<>();

    public HashMap<Set<DeclarationType>, DeclarationType> combinationTypeCache = new HashMap<>(); // Used inside combinationType.

    private void register(SingleTypeReducer handler) {
        Util.Pair<Class<? extends DeclarationType>, Class<? extends DeclarationType>> key1 = new Util.Pair<>(handler.getAClass(), handler.getBClass());
        if (this.handlers.containsKey(key1)) {
            throw new RuntimeException("Duplicate handler registration, " + key1);
        }
        this.handlers.put(key1, handler);
        if (handler.getAClass() != handler.getBClass()) {
            Util.Pair<Class<? extends DeclarationType>, Class<? extends DeclarationType>> key2 = new Util.Pair<>(handler.getBClass(), handler.getAClass());
            if (this.handlers.containsKey(key2)) {
                throw new RuntimeException("Duplicate handler registration, " + key2);
            }
            this.handlers.put(key2, new ReverseReducer<>(handler));
        }
    }

    public TypeReducer(Snap.Obj globalObject) {
        register(new PrimitiveObjectReducer(globalObject));
        register(new FunctionObjectReducer(globalObject));
        register(new FunctionClassReducer(this));
        register(new ClassObjectReducer(globalObject, this));
        register(new FunctionReducer(this));
        register(new PrimitiveReducer());
        register(new NamedUnamedObjectReducer(globalObject));
        register(new InterfaceFunctionReducer(this));
        register(new InterfaceUnnamedObjectReducer(this));
        register(new UnnamedObjectReducer(this));
    }

    private static Map<PrimitiveDeclarationType, Function<DeclarationType, DeclarationType>> specialPrimitives = new HashMap<>();
    static {
        specialPrimitives.put(PrimitiveDeclarationType.VOID, other -> other);
        specialPrimitives.put(PrimitiveDeclarationType.NON_VOID, other -> {
            if (other == PrimitiveDeclarationType.VOID) {
                return PrimitiveDeclarationType.NON_VOID;
            } else {
                return other;
            }
        });
        specialPrimitives.put(PrimitiveDeclarationType.ANY, other -> PrimitiveDeclarationType.ANY);
    }



    // TODO: Something better in the end?
    // TODO: Make sure UnionTypes are unfolded before this step.
    public DeclarationType combineTypes(DeclarationType one, DeclarationType two) {
        if (one == two) {
            return one;
        }

        // TODO: Ugly.... Instead find out why the Underscore class is being unified with "any".
        /*if (one instanceof ClassType) {
            return one;
        }
        if (two instanceof ClassType) {
            return two;
        }*/

        if (one instanceof PrimitiveDeclarationType && specialPrimitives.containsKey(one)) {
            return specialPrimitives.get(one).apply(two);
        }
        if (two instanceof PrimitiveDeclarationType && specialPrimitives.containsKey(two)) {
            return specialPrimitives.get(two).apply(one);
        }

        Util.Pair<Class<? extends DeclarationType>, Class<? extends DeclarationType>> typePair = new Util.Pair<>(one.getClass(), two.getClass());
        if (!handlers.containsKey(typePair)) {
            // TODO: This need to be better.
            return new UnionDeclarationType(one, two);
        }


        try {
            return handlers.get(typePair).reduce(one, two);
        } catch (CantReduceException e) {
            // TODO: Throw it further, and do some more.
            return new UnionDeclarationType(one, two);
        }
    }
}
