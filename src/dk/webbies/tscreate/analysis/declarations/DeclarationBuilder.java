package dk.webbies.tscreate.analysis.declarations;

import dk.webbies.tscreate.analysis.TypeFactory;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class DeclarationBuilder {
    private final Snap.Obj emptySnap;
    private Snap.Obj globalObject;
    private final TypeFactory typeFactory;

    public DeclarationBuilder(Snap.Obj emptySnap, Snap.Obj globalObject, TypeFactory typeFactory) {
        this.emptySnap = emptySnap;
        this.globalObject = globalObject;
        this.typeFactory = typeFactory;
    }

    public Map<String, DeclarationType> buildDeclaration() {
        Map<String, DeclarationType> declarations = new HashMap<>();

        for (Snap.Property property : this.globalObject.properties) {
            if (this.emptySnap.getProperty(property.name) == null) {
                declarations.put(property.name, typeFactory.getHeapPropType(property));
            }
        }

        return declarations;
    }

}
