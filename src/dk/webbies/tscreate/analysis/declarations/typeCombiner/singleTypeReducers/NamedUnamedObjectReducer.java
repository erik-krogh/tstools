package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.au.cs.casa.typescript.types.GenericType;
import dk.au.cs.casa.typescript.types.InterfaceType;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.SingleTypeReducer;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 18-10-2015.
 */
public class NamedUnamedObjectReducer implements SingleTypeReducer<UnnamedObjectType, NamedObjectType> {
    private NativeClassesMap nativeClasses;
    private TypeReducer combiner;

    public NamedUnamedObjectReducer(NativeClassesMap nativeClasses, TypeReducer combiner) {
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
    public DeclarationType reduce(UnnamedObjectType unnamedObjectType, NamedObjectType named) {
        if (named.getName().equals("Array")) {
            if (unnamedObjectType.getDeclarations().keySet().stream().anyMatch(Util::isInteger)) {
                List<DeclarationType> indexTypes = unnamedObjectType.getDeclarations().entrySet().stream().filter(entry -> Util.isInteger(entry.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
                indexTypes.add(named.indexType);

                NamedObjectType resultNamed = new NamedObjectType("Array", new CombinationType(combiner, indexTypes));
                UnnamedObjectType resultUnnamed = new UnnamedObjectType(unnamedObjectType.getDeclarations().entrySet().stream().filter(entry -> !Util.isInteger(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                return new UnionDeclarationType(resultNamed, resultUnnamed); // This is OK, it will run again and reach the below, since we run until fixpoint.
            }
        }
        if (objectMatchPrototype(unnamedObjectType, named)) {
            return named;
        } else {
            return null;
        }
    }

    public boolean objectMatchPrototype(UnnamedObjectType object, NamedObjectType type) {
        HashSet<String> keysNotAccountedFor = new HashSet<>(object.getDeclarations().keySet());
        removeKeys(type.getName(), keysNotAccountedFor);

        for (String name : type.getKnownSubTypes()) {
            if (keysNotAccountedFor.isEmpty()) {
                return true;
            }
            removeKeys(name, keysNotAccountedFor);
        }

        return keysNotAccountedFor.isEmpty();
    }


    private Map<String, Set<String>> nameToPropertiesCache = new HashMap<>();
    private void removeKeys(String name, Set<String> toRemoveFrom) {
        if (!nameToPropertiesCache.containsKey(name)) {
            HashSet<String> keys = new HashSet<>();
            nameToPropertiesCache.put(name, keys);

            Snap.Obj prototype = this.nativeClasses.prototypeFromName(name);

            addAllProperties(keys, prototype);

            Type type = this.nativeClasses.typeFromName(name);
            if (type instanceof InterfaceType) {
                keys.addAll(((InterfaceType) type).getDeclaredProperties().keySet());
            } else if (type instanceof GenericType) {
                keys.addAll(((GenericType) type).getDeclaredProperties().keySet());
            }

            if (this.nativeClasses.objectFromName(name) != null) {
                Snap.Obj obj = this.nativeClasses.objectFromName(name);
                addAllProperties(keys, obj);
            }
        }

        toRemoveFrom.removeAll(nameToPropertiesCache.get(name));
    }

    private void addAllProperties(HashSet<String> keys, Snap.Obj prototype) {
        while (prototype != null) {
            keys.addAll(prototype.getPropertyMap().keySet());
            if (prototype == prototype.prototype) {
                break;
            }
            prototype = prototype.prototype;
        }
    }
}
