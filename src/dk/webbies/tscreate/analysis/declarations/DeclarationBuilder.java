package dk.webbies.tscreate.analysis.declarations;

import dk.webbies.tscreate.analysis.declarations.types.FunctionType;
import dk.webbies.tscreate.analysis.declarations.types.InterfaceType;
import dk.webbies.tscreate.analysis.declarations.types.UnnamedObjectType;
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

    private static Map<Snap.Obj, DeclarationBlock> cache = new HashMap<>();

    private DeclarationBlock buildDeclaration(Snap.Obj obj) {
        if (cache.containsKey(obj)) {
            return cache.get(obj);
        }
        ArrayList<Declaration> declarations = new ArrayList<>();
        DeclarationBlock result = new DeclarationBlock(declarations);

        cache.put(obj, result);

        for (Snap.Property property : obj.properties) {
            if (obj.function != null) {
                if (property.name.equals("length") || property.name.equals("name") || property.name.equals("prototype")) {
                    continue;
                }
            }
            Snap.Value value = property.value;
            if (value == null) {
                continue;
            }
            if (value instanceof Snap.BooleanConstant) {
                declarations.add(new VariableDeclaration(property.name, PrimitiveDeclarationType.BOOLEAN));
            } else if (value instanceof Snap.NumberConstant) {
                declarations.add(new VariableDeclaration(property.name, PrimitiveDeclarationType.NUMBER));
            } else if (value instanceof Snap.StringConstant) {
                declarations.add(new VariableDeclaration(property.name, PrimitiveDeclarationType.STRING));
            } else if (value instanceof Snap.UndefinedConstant) {
                declarations.add(new VariableDeclaration(property.name, PrimitiveDeclarationType.UNDEFINED));
            } else {
                Snap.Obj propertyObj = (Snap.Obj) value;
                FunctionType functionType = null;
                if (propertyObj.function != null) {
                    Snap.Property prototypeProperty = propertyObj.getProperty("prototype");
                    if (prototypeProperty != null && prototypeProperty.value != null && prototypeProperty.value instanceof Snap.Obj && classes.containsKey(prototypeProperty.value) && classes.get(prototypeProperty.value).isUsedAsClass) {
                        functionType = functions.get(propertyObj); // TODO: This is temp, actually handle classes.
                        // throw new RuntimeException("Cannot build declarations for classes yet");
                    } else {
                        // Just a function
                        functionType = functions.get(propertyObj);
                    }
                }
                UnnamedObjectType objectType = new UnnamedObjectType(buildDeclaration(propertyObj));
                if (functionType != null) {
                    if (objectType.getBlock().getDeclarations().isEmpty()) {
                        declarations.add(new VariableDeclaration(property.name, functionType));
                    } else {
                        InterfaceType interfaceType = new InterfaceType(property.name);
                        interfaceType.function = functionType;
                        interfaceType.object = objectType;
                        declarations.add(new VariableDeclaration(property.name, interfaceType));
                    }
                } else {
                    declarations.add(new VariableDeclaration(property.name, objectType));
                }
            }
        }


        return result;
    }
}
