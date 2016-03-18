package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.au.cs.casa.typescript.types.GenericType;
import dk.au.cs.casa.typescript.types.InterfaceType;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.SingleTypeReducer;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.CombinationType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.DynamicAccessType;
import dk.webbies.tscreate.analysis.declarations.types.NamedObjectType;
import dk.webbies.tscreate.declarationReader.DeclarationParser;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Erik Krogh Kristensen on 11-11-2015.
 */
public class DynamicAccessNamedObjectReducer extends SingleTypeReducer<DynamicAccessType, NamedObjectType> {
    private final DeclarationParser.NativeClassesMap nativeClasses;
    private final TypeReducer combiner;

    public DynamicAccessNamedObjectReducer(DeclarationParser.NativeClassesMap nativeClasses, TypeReducer combiner, Map<DeclarationType, List<DeclarationType>> originals) {
        super(originals);
        this.nativeClasses = nativeClasses;
        this.combiner = combiner;
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
    public DeclarationType reduceIt(DynamicAccessType dynamic, NamedObjectType named) {
        String name = named.getName();
        if (name.equals("Array")) {
            CombinationType arrayType = new CombinationType(combiner, dynamic.getReturnType());
            if (named.getIndexType() != null) {
                arrayType.addType(named.getIndexType());
            }
            return new NamedObjectType("Array", named.isBaseType, arrayType);
        }
        Type type = nativeClasses.typeFromName(name);
        if (type == null) {
            return null;
        }
        if (nameHasIndexer(type)) return named;
        if (named.getKnownSubTypes().stream().map(nativeClasses::typeFromName).filter(Objects::nonNull).anyMatch(this::nameHasIndexer)) {
            return named;
        }
        return null;
    }

    private boolean nameHasIndexer(Type type) {
        if (type instanceof GenericType) {
            type = ((GenericType) type).toInterface();
        }
        if (type instanceof InterfaceType) {
            InterfaceType inter = (InterfaceType) type;
            if (inter.getDeclaredNumberIndexType() != null || inter.getDeclaredStringIndexType() != null) {
                return true;
            }
        }
        return false;
    }
}
