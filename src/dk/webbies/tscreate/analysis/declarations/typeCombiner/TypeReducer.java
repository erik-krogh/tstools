package dk.webbies.tscreate.analysis.declarations.typeCombiner;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers.*;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.util.Pair;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.function.Function;

import static dk.webbies.tscreate.analysis.declarations.types.UnresolvedDeclarationType.NotResolvedException;
import static dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;

/**
 * Created by Erik Krogh Kristensen on 16-10-2015.
 */
public class TypeReducer {
    public final HashMap<DeclarationType, List<DeclarationType>> originals = new HashMap<>();
    private final Options options;
    private final Map<Pair<Class<? extends DeclarationType>, Class<? extends DeclarationType>>, SingleTypeReducerInterface> handlers = new HashMap<>();
    private final Map<Class<? extends DeclarationType>, SameTypeMultiReducer> multiHandlers = new HashMap<>();
    private final Map<Class<? extends DeclarationType>, SameTypeSingleInstanceReducer> singleInstanceHandlers = new HashMap<>();

    public HashMap<Set<DeclarationType>, DeclarationType> combinationTypeCache = new HashMap<>(); // Used inside combinationType.

    private void register(SameTypeSingleInstanceReducer<? extends DeclarationType> handler) {
        if (this.singleInstanceHandlers.containsKey(handler.getTheClass())) {
            throw new RuntimeException("Duplicate");
        }
        this.singleInstanceHandlers.put(handler.getTheClass(), handler);
    }

    private void register(SingleTypeReducerInterface<? extends DeclarationType, ? extends DeclarationType> handler) {
        if (!DeclarationType.class.isAssignableFrom(handler.getAClass()) || !DeclarationType.class.isAssignableFrom(handler.getBClass())) {
            throw new RuntimeException("Only works on declarationTypes and subclasses.");
        }

        if (handler.getAClass() == handler.getBClass() && !(handler instanceof SameTypeReducer) && !(handler instanceof CantReduceReducer)) {
            throw new RuntimeException("I have an abstract class, use it for " + handler.getClass().getSimpleName());
        }

        if (options.reduceNothing) {
            handler = new CantReduceReducer<>(handler.getAClass(), handler.getBClass());
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

        if (handler instanceof SameTypeMultiReducer) {
            assert handler.getAClass() == handler.getBClass();
            multiHandlers.put(handler.getAClass(), (SameTypeMultiReducer) handler);
        }
    }

    public TypeReducer(Snap.Obj globalObject, NativeClassesMap nativeClasses, Options options) {
        this.options = options;
        register(new PrimitiveObjectReducer(globalObject));
        register(new FunctionObjectReducer(globalObject, originals));
        register(new FunctionClassReducer(this));
        register(new ClassObjectReducer(globalObject, this));
        register(new FunctionReducer(this, originals));
        register(new PrimitiveReducer(originals));
        register(new NamedUnamedObjectReducer(nativeClasses, this, originals));
        register(new UnnamedObjectReducer(this, originals));
        register(new ClassInstanceUnnamedObjectReducer(nativeClasses, originals));
        register(new ClassInstanceReducer(originals));
        register(new DynamicAccessReducer(this, originals));
        register(new NamedObjectReducer(globalObject, nativeClasses, originals, this));
        register(new DynamicAccessNamedObjectReducer(nativeClasses, this, originals));
        register(new FunctionNamedObjectReducer(nativeClasses, originals));
        register(new DynamicAccessUnnamedObjectReducer(this, originals));
        register(new ObjectToArrayReducer(this));

        // The ones that I cant do anything about.
        register(new CantReduceReducer(ClassInstanceType.class, NamedObjectType.class));
        register(new CantReduceReducer(FunctionType.class, PrimitiveDeclarationType.class));
        register(new CantReduceReducer(DynamicAccessType.class, FunctionType.class));
        register(new CantReduceReducer(DynamicAccessType.class, PrimitiveDeclarationType.class));
        register(new CantReduceReducer(DynamicAccessType.class, ClassInstanceType.class));
        register(new CantReduceReducer(DynamicAccessType.class, ClassType.class));
        register(new CantReduceReducer(NamedObjectType.class, PrimitiveDeclarationType.class));
        register(new CantReduceReducer(ClassType.class, PrimitiveDeclarationType.class));
        register(new CantReduceReducer(ClassInstanceType.class, PrimitiveDeclarationType.class));
        register(new CantReduceReducer(ClassType.class, NamedObjectType.class));
        register(new CantReduceReducer(ClassInstanceType.class, FunctionType.class));
        register(new CantReduceReducer(ClassType.class, ClassType.class));

        register(new CantReduceReducer(ClassInstanceType.class, ClassType.class));
    }

    private static Map<PrimitiveDeclarationType.Type, Function<Pair<DeclarationType, DeclarationType>, DeclarationType>> specialPrimitives = new HashMap<>();
    static {
        specialPrimitives.put(PrimitiveDeclarationType.Type.VOID, other -> other.right);
        specialPrimitives.put(PrimitiveDeclarationType.Type.NON_VOID, pair -> {
            DeclarationType other = pair.right;
            if (other instanceof PrimitiveDeclarationType &&  ((PrimitiveDeclarationType)other).getType() == PrimitiveDeclarationType.Type.VOID) {
                return pair.left;
            } else {
                return other;
            }
        });
        specialPrimitives.put(PrimitiveDeclarationType.Type.ANY, pair -> pair.left);
    }



    public DeclarationType combineTypes(DeclarationType one, DeclarationType two, boolean avoidUnresolved) {
        if (one == two) {
            return one;
        }

        if (one instanceof PrimitiveDeclarationType && specialPrimitives.containsKey(((PrimitiveDeclarationType) one).getType())) {
            DeclarationType result = specialPrimitives.get(((PrimitiveDeclarationType) one).getType()).apply(new Pair<>(one, two));
//            result.setNames(Util.concatSet(one.getNames(), two.getNames())); // Commented out on purpose, since they return the same object as was given in, this potentially merges every single name.
            return result;
        }
        if (two instanceof PrimitiveDeclarationType && specialPrimitives.containsKey(((PrimitiveDeclarationType) two).getType())) {
            DeclarationType result = specialPrimitives.get(((PrimitiveDeclarationType) two).getType()).apply(new Pair<>(two, one));
//            result.setNames(Util.concatSet(one.getNames(), two.getNames()));
            return result;
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
        List<DeclarationType> types = new ArrayList<>(typeCollection);

        // In 3 steps
        // - first see if a whole group of the same type can be reduced (Named-type, looking at you).
        // - Then in a loop:
        //   - See if a single type can be reduced.
        //   - See if a pair of types can be reduced.


        Multimap<Class<? extends DeclarationType>, DeclarationType> typesMultiMap = ArrayListMultimap.create();
        types.forEach((type) -> typesMultiMap.put(type.getClass(), type));
        for (Map.Entry<Class<? extends DeclarationType>, Collection<DeclarationType>> entry : typesMultiMap.asMap().entrySet()) {
            Collection<DeclarationType> collection = entry.getValue();
            if (multiHandlers.containsKey(entry.getKey()) && collection.size() > 1) {
                DeclarationType result = multiHandlers.get(entry.getKey()).reduce(collection);
                if (result == null) {
                    continue;
                }
                types.removeAll(collection);
                if (result instanceof UnionDeclarationType) {
                    types.addAll(((UnionDeclarationType) result).getTypes());
                } else {
                    types.add(result);
                }
            }
        }

        boolean fixPoint = false;
        while (!fixPoint) {
            fixPoint = true;
            for (int i = 0; i < types.size(); i++) {
                DeclarationType type = types.get(i);
                if (this.singleInstanceHandlers.containsKey(type.getClass())) {
                    SameTypeSingleInstanceReducer handler = this.singleInstanceHandlers.get(type.getClass());
                    DeclarationType result = handler.reduce(type);
                    if (result == null) {
                        continue;
                    }
                    types.remove(i);
                    if (result instanceof UnionDeclarationType) {
                        types.addAll(((UnionDeclarationType) result).getTypes());
                    } else {
                        types.add(result);
                    }
                    fixPoint = false;
                    break;
                }
            }



            for (int i = 0; i < types.size(); i++) {
                DeclarationType one = types.get(i);
                for (int j = i + 1; j < types.size(); j++) {
                    DeclarationType two = types.get(j);
                    DeclarationType combinedType = combineTypes(one, two, avoidUnresolved);
                    if (combinedType == null) {
                        continue;
                    } else if (combinedType instanceof UnionDeclarationType) {
                        List<DeclarationType> unfoldedTypes = ((UnionDeclarationType) combinedType).getTypes();
                        types.addAll(unfoldedTypes);
                    } else {
                        types.add(combinedType);
                    }
                    fixPoint = false;
                    types.remove(j);
                    types.remove(i);
                    i--;
                    break;
                }
            }
        }

        extractInterfaces(types);

        if (types.size() == 0) {
            return PrimitiveDeclarationType.Void(Collections.EMPTY_SET);
        } else if (types.size() == 1) {
            return types.get(0);
        } else {
            HashSet<DeclarationType> key = new HashSet<>(types);
            if (unionDeclarationTypeCache.containsKey(key)) {
                return unionDeclarationTypeCache.get(key);
            } else {
                UnionDeclarationType result = new UnionDeclarationType(types);
                unionDeclarationTypeCache.put(key, result);
                return result;
            }
        }
    }
    private Map<Set<DeclarationType>, UnionDeclarationType> unionDeclarationTypeCache = new HashMap<>();

    private void extractInterfaces(List<DeclarationType> types) {
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

        Set<String> interfaceNames = interfaceParts.stream().map(DeclarationType::getNames).reduce(new HashSet<>(), Util::reduceSet);

        if (options.reduceNothing) {
            types.removeAll(interfaceParts);
            for (DeclarationType part : interfaceParts) {
                InterfaceDeclarationType result = new InterfaceDeclarationType(interfaceNames);
                if (part instanceof DynamicAccessType) {
                    assert result.dynamicAccess == null;
                    result.dynamicAccess = (DynamicAccessType) part;
                    types.add(result);
                } else {
                    types.add(part);
                }
            }
        } else {
            if (interfaceParts.size() >= 2 || hadDynamicAccess) {
                types.removeAll(interfaceParts);
                InterfaceDeclarationType result = new InterfaceDeclarationType(interfaceNames);
                types.add(result);
                for (DeclarationType part : interfaceParts) {
                    populateInterface(result, part);
                }
                originals.put(result, interfaceParts);
            }
        }
    }

    private void populateInterface(InterfaceDeclarationType result, DeclarationType part) {
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
