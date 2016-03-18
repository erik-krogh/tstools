package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.au.cs.casa.typescript.types.GenericType;
import dk.au.cs.casa.typescript.types.InterfaceType;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.SingleTypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.FunctionType;
import dk.webbies.tscreate.analysis.declarations.types.NamedObjectType;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.List;
import java.util.Map;

import static dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;

/**
 * Created by Erik Krogh Kristensen on 19-11-2015.
 */
public class FunctionNamedObjectReducer extends SingleTypeReducer<FunctionType, NamedObjectType> {
    private NativeClassesMap nativeClasses;

    public FunctionNamedObjectReducer(NativeClassesMap nativeClasses, Map<DeclarationType, List<DeclarationType>> originals) {
        super(originals);
        this.nativeClasses = nativeClasses;
    }

    @Override
    public Class<FunctionType> getAClass() {
        return FunctionType.class;
    }

    @Override
    public Class<NamedObjectType> getBClass() {
        return NamedObjectType.class;
    }

    @Override
    public DeclarationType reduceIt(FunctionType function, NamedObjectType named) {
        Snap.Obj obj = this.nativeClasses.objectFromName(named.getName());
        if (obj != null && obj.function != null) {
            return named; // Lot of assuming here,
        }
        Type type = this.nativeClasses.typeFromName(named.getName());
        if (type instanceof GenericType) {
            type = ((GenericType) type).toInterface();
        }
        if (type instanceof InterfaceType) {
            InterfaceType inter = (InterfaceType) type;
            if (!inter.getDeclaredCallSignatures().isEmpty() || !inter.getDeclaredConstructSignatures().isEmpty()) {
                return named;
            }
        }
        return null;
    }
}
