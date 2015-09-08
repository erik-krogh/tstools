package dk.webbies.tscreate.analysis.declarations;

import dk.webbies.tscreate.analysis.declarations.types.FunctionType;
import dk.webbies.tscreate.analysis.declarations.types.ObjectType;
import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.jsnapconvert.Snap;
import dk.webbies.tscreate.jsnapconvert.classes.LibraryClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class DeclarationBuilder {
    private final Snap.Obj librarySnap;
    private final HashMap<Snap.Obj, LibraryClass> classes;
    private final Map<Snap.Obj, FunctionType> functions;

    public DeclarationBuilder(Snap.Obj librarySnap, HashMap<Snap.Obj, LibraryClass> classes, Map<Snap.Obj, FunctionType> functions) {
        this.librarySnap = librarySnap;
        this.classes = classes;
        this.functions = functions;
    }

    public DeclarationBlock buildDeclaration() {
        Snap.Obj obj = this.librarySnap;
        return buildDeclaration(obj);
    }

    private DeclarationBlock buildDeclaration(Snap.Obj obj) {
        ArrayList<Declaration> result = new ArrayList<>();
        for (Snap.Property property : obj.properties) {
            Snap.Value value = property.value;
            if (value == null) {
                continue;
            }
            if (value instanceof Snap.BooleanConstant) {
                result.add(new VariableDeclaration(property.name, PrimitiveDeclarationType.BOOLEAN));
            } else if (value instanceof Snap.NumberConstant) {
                result.add(new VariableDeclaration(property.name, PrimitiveDeclarationType.NUMBER));
            } else if (value instanceof Snap.StringConstant) {
                result.add(new VariableDeclaration(property.name, PrimitiveDeclarationType.STRING));
            } else if (value instanceof Snap.UndefinedConstant) {
                result.add(new VariableDeclaration(property.name, PrimitiveDeclarationType.UNDEFINED));
            } else {
                Snap.Obj propertyObj = (Snap.Obj) value;
                if (propertyObj.function != null) {
                    Snap.Property prototypeProperty = propertyObj.getProperty("prototype");
                    if (prototypeProperty != null && prototypeProperty.value != null && prototypeProperty.value instanceof Snap.Obj && classes.containsKey(prototypeProperty.value) && classes.get(prototypeProperty.value).isUsedAsClass) {
                        throw new RuntimeException("Cannot build declarations for classes yet");
                    } else {
                        // Just a function
                        result.add(new VariableDeclaration(property.name, functions.get(propertyObj)));
                    }


                } else {
                    result.add(new VariableDeclaration(property.name, new ObjectType(buildDeclaration(propertyObj))));
                }
            }
        }

        return new DeclarationBlock(result);
    }
}
