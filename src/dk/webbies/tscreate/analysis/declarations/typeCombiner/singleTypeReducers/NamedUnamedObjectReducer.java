package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.au.cs.casa.typescript.types.GenericType;
import dk.au.cs.casa.typescript.types.InterfaceType;
import dk.au.cs.casa.typescript.types.ReferenceType;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.SingleTypeReducer;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 18-10-2015.
 */
public class NamedUnamedObjectReducer extends SingleTypeReducer<UnnamedObjectType, NamedObjectType> {
    private NativeClassesMap nativeClasses;
    private TypeReducer combiner;

    public NamedUnamedObjectReducer(NativeClassesMap nativeClasses, TypeReducer combiner, Map<DeclarationType, List<DeclarationType>> originals) {
        super(originals);
        this.nativeClasses = nativeClasses;
        this.combiner = combiner;
    }

    @Override
    public Class<UnnamedObjectType> getAClass() {
        return UnnamedObjectType.class;
    }

    @Override
    public Class<NamedObjectType> getBClass() {
        return NamedObjectType.class;
    }

    @Override
    public DeclarationType reduceIt(UnnamedObjectType unnamedObjectType, NamedObjectType named) {
        if (hasNumberIndexer(named.getName())) {
            if (unnamedObjectType.getDeclarations().keySet().stream().anyMatch(Util::isInteger)) {
                List<DeclarationType> indexTypes = unnamedObjectType.getDeclarations().entrySet().stream().filter(entry -> Util.isInteger(entry.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
                indexTypes.add(named.getIndexType());

                NamedObjectType resultNamed = new NamedObjectType(named.getName(), named.isBaseType, new CombinationType(combiner, indexTypes));
                UnnamedObjectType resultUnnamed = new UnnamedObjectType(unnamedObjectType.getDeclarations().entrySet().stream().filter(entry -> !Util.isInteger(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)), unnamedObjectType.getNames());
                return new UnionDeclarationType(resultNamed, resultUnnamed); // This is OK, it will run again and reach the below, since we run until fixpoint.
            }
        }
        HashSet<String> keysNotAccountedFor = getKeysNotAccountedFor(unnamedObjectType, named);
        if (keysNotAccountedFor.isEmpty()) {
            return named;
        } else if (unnamedObjectType.getDeclarations().size() == keysNotAccountedFor.size()){
            return null;
        } else {
            UnnamedObjectType newUnnamed = new UnnamedObjectType(unnamedObjectType.getDeclarations().keySet().stream().filter(keysNotAccountedFor::contains).collect(Collectors.toMap(Function.identity(), (key) -> unnamedObjectType.getDeclarations().get(key))), unnamedObjectType.getNames());
            return new UnionDeclarationType(named, newUnnamed);
        }
    }

    private boolean hasNumberIndexer(String name) {
        Type type = nativeClasses.typeFromName(name);
        if (type instanceof ReferenceType) {
            type = ((ReferenceType) type).getTarget();
        }
        if (type instanceof GenericType) {
            type = ((GenericType) type).toInterface();
        }
        if (type instanceof InterfaceType) {
            Type numberType = ((InterfaceType) type).getDeclaredNumberIndexType();
            if (name.equals("Array")) {
                assert numberType != null;
            }
            return numberType != null;
        } else {
            assert !name.equals("Array");
            return false;
        }

    }

    private HashSet<String> getKeysNotAccountedFor(UnnamedObjectType object, NamedObjectType type) {
        HashSet<String> keysNotAccountedFor = new HashSet<>(object.getDeclarations().keySet());
        keysNotAccountedFor.removeAll(getKeysFromName(type.getName()));

        for (String name : type.getKnownSubTypes()) {
            if (keysNotAccountedFor.isEmpty()) {
                break;
            }
            keysNotAccountedFor.removeAll(getKeysFromName(name));
        }
        return keysNotAccountedFor;
    }

    private final Map<String, Set<String>> cache = new HashMap<>();
    private Set<String> getKeysFromName(String name) {
        if (cache.containsKey(name)) {
            return cache.get(name);
        }

        Set<String> result = getKeysFromName(name, this.nativeClasses);

        cache.put(name, result);

        return result;
    }

    public static Set<String> getKeysFromName(String name, NativeClassesMap nativeClasses) {
        HashSet<String> result = new HashSet<>();

        addAllProperties(result, nativeClasses.prototypeFromName(name));

        if (nativeClasses.typeFromName(name) != null) {
            addAllProperties(result, nativeClasses.typeFromName(name));
        }

        addAllProperties(result, nativeClasses.objectFromName(name));

        return result;
    }


    private static void addAllProperties(HashSet<String> keys, Type type) {
        if (type instanceof InterfaceType) {
            InterfaceType inter = (InterfaceType) type;
            keys.addAll(inter.getDeclaredProperties().keySet());
            inter.getBaseTypes().forEach(base -> addAllProperties(keys, base));
        } else if (type instanceof GenericType) {
            GenericType generic = (GenericType) type;
            keys.addAll(generic.getDeclaredProperties().keySet());
            generic.getBaseTypes().forEach(base -> addAllProperties(keys, base));
        } else if (type instanceof ReferenceType) {
            GenericType generic = (GenericType) ((ReferenceType) type).getTarget();
            keys.addAll(generic.getDeclaredProperties().keySet());
            generic.getBaseTypes().forEach(base -> addAllProperties(keys, base));
        } else {
            throw new RuntimeException("Don't know how to add keys from type " + type.getClass().getSimpleName());
        }
    }

    private static void addAllProperties(HashSet<String> keys, Snap.Obj prototype) {
        while (prototype != null) {
            keys.addAll(prototype.getPropertyMap().keySet());
            if (prototype == prototype.prototype) {
                break;
            }
            prototype = prototype.prototype;
        }
    }
}
