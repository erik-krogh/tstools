package dk.webbies.tscreate.analysis.declarations.typeCombiner;

import dk.webbies.tscreate.analysis.declarations.types.CombinationType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.UnionDeclarationType;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.util.Pair;

import java.util.*;
import java.util.function.Function;

/**
 * Created by Erik Krogh Kristensen on 16-10-2015.
 */
public class TypeReducer {
    private Map<Pair<Class<? extends DeclarationType>, Class<? extends DeclarationType>>, SingleTypeReducer> handlers = new HashMap<>();

    public HashMap<Set<DeclarationType>, DeclarationType> combinationTypeCache = new HashMap<>(); // Used inside combinationType.

    private void register(SingleTypeReducer handler) {
        Pair<Class<? extends DeclarationType>, Class<? extends DeclarationType>> key1 = new Pair<>(handler.getAClass(), handler.getBClass());
        if (this.handlers.containsKey(key1)) {
            throw new RuntimeException("Duplicate handler registration, " + key1);
        }
        this.handlers.put(key1, handler);
        if (handler.getAClass() != handler.getBClass()) {
            Pair<Class<? extends DeclarationType>, Class<? extends DeclarationType>> key2 = new Pair<>(handler.getBClass(), handler.getAClass());
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
        register(new ClassInterfaceReducer(globalObject, this));
        register(new ClassInstanceUnnamedObjectReducer());
        register(new ClassInstanceReducer());
    }

    private List<CombinationType> unresolvedTypes = new ArrayList<>();

    public void registerUnresolved(CombinationType type) {
        this.unresolvedTypes.add(type);
    }

    public void resolveCombinationTypes() {
        while (!this.unresolvedTypes.isEmpty()) {
            List<CombinationType> copy = new ArrayList<>(this.unresolvedTypes);
            this.unresolvedTypes.clear();

            copy.forEach(CombinationType::createCombined);
        }
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



    private DeclarationType combineTypes(DeclarationType one, DeclarationType two) {
        if (one == two) {
            return one;
        }

        if (one instanceof PrimitiveDeclarationType && specialPrimitives.containsKey(one)) {
            return specialPrimitives.get(one).apply(two);
        }
        if (two instanceof PrimitiveDeclarationType && specialPrimitives.containsKey(two)) {
            return specialPrimitives.get(two).apply(one);
        }

        Pair<Class<? extends DeclarationType>, Class<? extends DeclarationType>> typePair = new Pair<>(one.getClass(), two.getClass());
        if (!handlers.containsKey(typePair)) {
            return null;
        }


        return handlers.get(typePair).reduce(one, two);
    }

    public DeclarationType combineTypes(Collection<DeclarationType> typeCollection) {
        // Copy, because i modify the list.
        ArrayList<DeclarationType> types = new ArrayList<>(typeCollection);

        for (int i = 0; i < types.size(); i++) {
            DeclarationType one = types.get(i);
            for (int j = i + 1; j < types.size(); j++) {
                DeclarationType two = types.get(j);
                DeclarationType combinedType = combineTypes(one, two);
                if (combinedType == null) {
                    continue;
                }
                types.set(i, combinedType);
                types.remove(j);
                i--;
                break;
            }
        }

        if (types.size() == 0) {
            return PrimitiveDeclarationType.VOID;
        } else if (types.size() == 1) {
            return types.get(0);
        } else {
            return new UnionDeclarationType(types);
        }
    }
}
