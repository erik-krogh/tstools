package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.SingleTypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.FunctionType;
import dk.webbies.tscreate.analysis.declarations.types.UnnamedObjectType;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 17-10-2015.
 */
// FIXME: Make it possible to remove the fields that are on the prototype. As in returning two types. This will fix to many prototypes in Prototype
public class FunctionObjectReducer implements SingleTypeReducer<FunctionType, UnnamedObjectType> {
    private Snap.Obj globalObject;

    public FunctionObjectReducer(Snap.Obj globalObject) {
        this.globalObject = globalObject;
    }

    @Override
    public Class<FunctionType> getAClass() {
        return FunctionType.class;
    }

    @Override
    public Class<UnnamedObjectType> getBClass() {
        return UnnamedObjectType.class;
    }

    @Override
    public DeclarationType reduce(FunctionType function, UnnamedObjectType object) {
        if (objectMatchFunctionPrototype(object, globalObject)) {
            return function;
        } else {
            return null;
        }
    }

    public static boolean objectMatchFunctionPrototype(UnnamedObjectType object, Snap.Obj globalObject) {
        return getMatchingProperties(object, globalObject).size() == object.getDeclarations().size();
    }

    private static List<String> getMatchingProperties(UnnamedObjectType object, Snap.Obj globalObject) {
        return object.getDeclarations().keySet().stream().filter(key -> {
            boolean matchObject = PrimitiveObjectReducer.objectFieldMatchPrototypeOf(globalObject.getProperty("Object").value, key);
            boolean matchFunction = key.equals("prototype") || key.equals("caller") || key.equals("length") || key.equals("name") || key.equals("arguments") || PrimitiveObjectReducer.objectFieldMatchPrototypeOf(globalObject.getProperty("Function").value, key);
            return matchObject || matchFunction;
        }).collect(Collectors.toList());
    }
}
