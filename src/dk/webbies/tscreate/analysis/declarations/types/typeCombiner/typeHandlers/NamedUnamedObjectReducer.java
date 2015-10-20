package dk.webbies.tscreate.analysis.declarations.types.typeCombiner.typeHandlers;

import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.NamedObjectType;
import dk.webbies.tscreate.analysis.declarations.types.UnnamedObjectType;
import dk.webbies.tscreate.jsnap.Snap;

/**
 * Created by Erik Krogh Kristensen on 18-10-2015.
 */
public class NamedUnamedObjectReducer implements SingleTypeReducer<UnnamedObjectType, NamedObjectType> {
    private final Snap.Obj global;

    public NamedUnamedObjectReducer(Snap.Obj global) {
        this.global = global;
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
    public DeclarationType reduce(UnnamedObjectType unnamedObjectType, NamedObjectType named) throws CantReduceException {
        Snap.Property prop = global.getProperty(named.getName());
        if (prop == null) {
            // Can't say anything, so we assume that the object does contain it.
            return named;
        }
        if (objectMatchPrototype(unnamedObjectType, (Snap.Obj) prop.value)) {
            return named;
        } else {
            throw new CantReduceException();
        }
    }

    public static boolean objectMatchPrototype(UnnamedObjectType object, Snap.Obj prototype) {
        return object.getDeclarations().keySet().stream().allMatch(key -> {
            return PrimitiveObjectReducer.objectFieldMatchPrototypeOf(prototype, key);
        });
    }
}
