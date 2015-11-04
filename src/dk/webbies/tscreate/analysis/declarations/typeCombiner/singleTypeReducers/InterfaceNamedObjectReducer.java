package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.InterfaceType;
import dk.webbies.tscreate.analysis.declarations.types.NamedObjectType;
import dk.webbies.tscreate.jsnap.Snap;

/**
 * Created by Erik Krogh Kristensen on 04-11-2015.
 */
public class InterfaceNamedObjectReducer implements SingleTypeReducer<InterfaceType, NamedObjectType> {
    private TypeReducer typeReducer;
    private Snap.Obj global;

    public InterfaceNamedObjectReducer(TypeReducer typeReducer, Snap.Obj global) {
        this.typeReducer = typeReducer;
        this.global = global;
    }

    @Override
    public Class<InterfaceType> getAClass() {
        return InterfaceType.class;
    }

    @Override
    public Class<NamedObjectType> getBClass() {
        return NamedObjectType.class;
    }

    @Override
    public DeclarationType reduce(InterfaceType inter, NamedObjectType named) {
        // TODO: What about dynamic access stuff.
        if (inter.object == null) {
            return named;
        }
        if (new NamedUnamedObjectReducer(global).reduce(inter.getObject(), named) == null) {
            // The named object has all the fields that the interface has.
            return named;
        } else {
            // The interface contains more than the named object.
            return null;
        }
    }
}
