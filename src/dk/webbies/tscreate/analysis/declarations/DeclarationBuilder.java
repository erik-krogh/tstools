package dk.webbies.tscreate.analysis.declarations;

import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.analysis.TypeFactory;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class DeclarationBuilder {
    private final Snap.Obj librarySnap;
    private final TypeFactory typeFactory;

    public DeclarationBuilder(Snap.Obj librarySnap, HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj globalObject, Map<Type, String> typeNames) {
        this.librarySnap = librarySnap;
        TypeAnalysis typeAnalysis = new TypeAnalysis(libraryClasses, options, globalObject, typeNames);
        typeAnalysis.analyseFunctions();
        this.typeFactory = typeAnalysis.getTypeFactory();
    }

    public Map<String, DeclarationType> buildDeclaration() {
        return buildDeclaration(this.librarySnap);
    }


    private Map<String, DeclarationType> buildDeclaration(Snap.Obj obj) {
        Map<String, DeclarationType> declarations = new HashMap<>();

        for (Snap.Property property : obj.properties) {
            declarations.put(property.name, typeFactory.getHeapPropType(property));
        }

        return declarations;
    }
}
