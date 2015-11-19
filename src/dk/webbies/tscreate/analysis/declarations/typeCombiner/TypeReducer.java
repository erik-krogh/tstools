package dk.webbies.tscreate.analysis.declarations.typeCombiner;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers.*;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.util.Pair;

import java.util.*;
import java.util.function.Function;

import static dk.webbies.tscreate.analysis.declarations.types.UnresolvedDeclarationType.NotResolvedException;
import static dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;

/**
 * Created by Erik Krogh Kristensen on 16-10-2015.
 */
public class TypeReducer {
    private Map<Pair<Class<? extends DeclarationType>, Class<? extends DeclarationType>>, SingleTypeReducer> handlers = new HashMap<>();

    public HashMap<Set<DeclarationType>, DeclarationType> combinationTypeCache = new HashMap<>(); // Used inside combinationType.

    private void register(SingleTypeReducer<? extends DeclarationType, ? extends DeclarationType> handler) {
        if (!DeclarationType.class.isAssignableFrom(handler.getAClass()) || !DeclarationType.class.isAssignableFrom(handler.getBClass())) {
            throw new RuntimeException("Only works on declarationTypes and subclasses.");
        }

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

    public TypeReducer(Snap.Obj globalObject, NativeClassesMap nativeClasses) {
        register(new PrimitiveObjectReducer(globalObject));
        register(new FunctionObjectReducer(globalObject));
        register(new FunctionClassReducer(this));
        register(new ClassObjectReducer(globalObject, this));
        register(new FunctionReducer(this));
        register(new PrimitiveReducer());
        register(new NamedUnamedObjectReducer(globalObject, nativeClasses));
        register(new UnnamedObjectReducer(this));
        register(new ClassInstanceUnnamedObjectReducer());
        register(new ClassInstanceReducer());
        register(new DynamicAccessReducer(this));
        register(new NamedObjectReducer(globalObject, nativeClasses));
        register(new DynamicAccessNamedObjectReducer(nativeClasses));
        register(new FunctionNamedObjectReducer(nativeClasses));

        // The ones that I cant do anything about. // FIXME: Look to see if any of these can actually be handled.

        register(new CantReduceReducer(FunctionType.class, PrimitiveDeclarationType.class));
        register(new CantReduceReducer(DynamicAccessType.class, UnnamedObjectType.class));
        register(new CantReduceReducer(DynamicAccessType.class, FunctionType.class));
        register(new CantReduceReducer(DynamicAccessType.class, PrimitiveDeclarationType.class));
        register(new CantReduceReducer(DynamicAccessType.class, ClassInstanceType.class));
        register(new CantReduceReducer(DynamicAccessType.class, ClassType.class));
        register(new CantReduceReducer(NamedObjectType.class, PrimitiveDeclarationType.class));
        register(new CantReduceReducer(ClassType.class, PrimitiveDeclarationType.class));
        register(new CantReduceReducer(ClassInstanceType.class, PrimitiveDeclarationType.class));
        register(new CantReduceReducer(ClassInstanceType.class, NamedObjectType.class));
        register(new CantReduceReducer(ClassType.class, NamedObjectType.class));
        register(new CantReduceReducer(ClassInstanceType.class, FunctionType.class));
        register(new CantReduceReducer(ClassType.class, ClassType.class));

        register(new CantReduceReducer(ClassInstanceType.class, ClassType.class));
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

    private static Map<PrimitiveDeclarationType.Type, Function<DeclarationType, DeclarationType>> specialPrimitives = new HashMap<>();
    static {
        specialPrimitives.put(PrimitiveDeclarationType.Type.VOID, other -> other);
        specialPrimitives.put(PrimitiveDeclarationType.Type.NON_VOID, other -> {
            if (other instanceof PrimitiveDeclarationType &&  ((PrimitiveDeclarationType)other).getType() == PrimitiveDeclarationType.Type.VOID) {
                return PrimitiveDeclarationType.NonVoid();
            } else {
                return other;
            }
        });
        specialPrimitives.put(PrimitiveDeclarationType.Type.ANY, other -> PrimitiveDeclarationType.Any());
    }



    public DeclarationType combineTypes(DeclarationType one, DeclarationType two, boolean avoidUnresolved) {
        if (one == two) {
            return one;
        }

        if (one instanceof PrimitiveDeclarationType && specialPrimitives.containsKey(((PrimitiveDeclarationType) one).getType())) {
            return specialPrimitives.get(((PrimitiveDeclarationType) one).getType()).apply(two);
        }
        if (two instanceof PrimitiveDeclarationType && specialPrimitives.containsKey(((PrimitiveDeclarationType) two).getType())) {
            return specialPrimitives.get(((PrimitiveDeclarationType) two).getType()).apply(one);
        }

        Pair<Class<? extends DeclarationType>, Class<? extends DeclarationType>> typePair = new Pair<>(one.getClass(), two.getClass());
        if (!handlers.containsKey(typePair)) {
            throw new RuntimeException("Don't know how to handle " + one.getClass().getSimpleName() + " - " + two.getClass().getSimpleName());
        }

        try {
            return handlers.get(typePair).reduce(one, two);
        } catch (NotResolvedException e) {
            if (avoidUnresolved) {
                return null;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public DeclarationType combineTypes(Collection<DeclarationType> typeCollection, boolean avoidUnresolved) {
        // Copy, because i modify the list.
        ArrayList<DeclarationType> types = new ArrayList<>(typeCollection);

        boolean fixPoint = false;
        while (!fixPoint) {
            fixPoint = true;
            for (int i = 0; i < types.size(); i++) {
                DeclarationType one = types.get(i);
                for (int j = i + 1; j < types.size(); j++) {
                    DeclarationType two = types.get(j);
                    DeclarationType combinedType = combineTypes(one, two, avoidUnresolved);
                    if (combinedType == null) {
                        continue;
                    }
                    fixPoint = false;
                    types.set(i, combinedType);
                    types.remove(j);
                    i--;
                    break;
                }
            }
        }

        extractInterfaces(types);

        if (types.size() == 0) {
            return PrimitiveDeclarationType.Void();
        } else if (types.size() == 1) {
            return types.get(0);
        } else {
            return new UnionDeclarationType(types);
        }
    }

    private void extractInterfaces(ArrayList<DeclarationType> types) {
        // Works in place, so no return.

        List<DeclarationType> interfaceParts = new ArrayList<>();
        boolean hadDynamicAccess = false;
        for (DeclarationType type : types) {
            if (type instanceof FunctionType) {
                interfaceParts.add(type);
            } else if (type instanceof DynamicAccessType) {
                hadDynamicAccess = true;
                interfaceParts.add(type);
            } else if (type instanceof UnnamedObjectType) {
                interfaceParts.add(type);
            }
        }
        if (interfaceParts.size() >= 2 || hadDynamicAccess) {
            types.removeAll(interfaceParts);
            InterfaceType result = new InterfaceType();
            types.add(result);
            for (DeclarationType part : interfaceParts) {
                if (part instanceof FunctionType) {
                    assert result.function == null;
                    result.function = (FunctionType) part;
                } else if (part instanceof DynamicAccessType) {
                    assert result.dynamicAccess == null;
                    result.dynamicAccess = (DynamicAccessType) part;
                } else if (part instanceof UnnamedObjectType) {
                    assert result.object == null;
                    result.object = (UnnamedObjectType) part;
                }
            }
        }

    }
}
