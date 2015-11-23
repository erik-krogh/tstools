package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.au.cs.casa.typescript.types.GenericType;
import dk.au.cs.casa.typescript.types.InterfaceType;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.SingleTypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.DynamicAccessType;
import dk.webbies.tscreate.analysis.declarations.types.NamedObjectType;
import dk.webbies.tscreate.declarationReader.DeclarationParser;

/**
 * Created by Erik Krogh Kristensen on 11-11-2015.
 */
public class DynamicAccessNamedObjectReducer implements SingleTypeReducer<DynamicAccessType, NamedObjectType> {
    private final DeclarationParser.NativeClassesMap nativeClasses;

    public DynamicAccessNamedObjectReducer(DeclarationParser.NativeClassesMap nativeClasses) {
        this.nativeClasses = nativeClasses;
    }

    @Override
    public Class<DynamicAccessType> getAClass() {
        return DynamicAccessType.class;
    }

    @Override
    public Class<NamedObjectType> getBClass() {
        return NamedObjectType.class;
    }

    @Override
    public DeclarationType reduce(DynamicAccessType dynamic, NamedObjectType named) {
        Type type = nativeClasses.typeFromName(named.getName());
        if (type == null) {
            return null;
        }
        if (type instanceof GenericType) {
            type = ((GenericType) type).toInterface();
        }
        if (type instanceof InterfaceType) {
            InterfaceType inter = (InterfaceType) type;
            if (inter.getDeclaredNumberIndexType() != null || inter.getDeclaredStringIndexType() != null) {
                return named;
            }
        }
        return null;
    }
}
