package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.au.cs.casa.typescript.types.GenericType;
import dk.au.cs.casa.typescript.types.InterfaceType;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.NamedObjectType;
import dk.webbies.tscreate.analysis.declarations.types.UnnamedObjectType;
import dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Erik Krogh Kristensen on 18-10-2015.
 */
public class NamedUnamedObjectReducer implements SingleTypeReducer<UnnamedObjectType, NamedObjectType> {
    private NativeClassesMap nativeClasses;

    public NamedUnamedObjectReducer(NativeClassesMap nativeClasses) {
        this.nativeClasses = nativeClasses;
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
