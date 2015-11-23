package dk.webbies.tscreate.analysis.declarations;

import com.google.common.collect.BiMap;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.analysis.TypeFactory;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class DeclarationBuilder {
    private final Snap.Obj librarySnap;
    private final TypeFactory typeFactory;

    public DeclarationBuilder(Snap.Obj librarySnap, HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj globalObject, NativeClassesMap nativeClasses) {
        this.librarySnap = librarySnap;
        TypeAnalysis typeAnalysis = new TypeAnalysis(libraryClasses, options, globalObject, nativeClasses);
        this.typeFactory = typeAnalysis.getTypeFactory();
        typeAnalysis.analyseFunctions();
    }

    public Map<String, DeclarationType> buildDeclaration() {
        Map<String, DeclarationType> declarations = new HashMap<>();

        for (Snap.Property property : this.librarySnap.properties) {
            declarations.put(property.name, typeFactory.getHeapPropType(property));
        }

        return declarations;
    }

}
