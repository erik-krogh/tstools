package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.SingleTypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.FunctionType;
import dk.webbies.tscreate.analysis.declarations.types.UnionDeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.UnnamedObjectType;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 17-10-2015.
 */
public class FunctionObjectReducer extends SingleTypeReducer<FunctionType, UnnamedObjectType> {
    private Snap.Obj globalObject;

    public FunctionObjectReducer(Snap.Obj globalObject, Map<DeclarationType, List<DeclarationType>> originals) {
        super(originals);
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
    public DeclarationType reduceIt(FunctionType function, UnnamedObjectType object) {
        if (objectMatchFunctionPrototype(object, globalObject)) {
            return function;
        } else {
            List<String> matchingProperties = getMatchingProperties(object, globalObject);
            if (matchingProperties.isEmpty()) {
                return null;
            } else {
                HashMap<String, DeclarationType> declarations = new HashMap<>(object.getDeclarations());
                UnnamedObjectType newObject = new UnnamedObjectType(declarations, object.getNames());
                for (String matchingProperty : matchingProperties) {
                    declarations.remove(matchingProperty);
                }
                return new UnionDeclarationType(function, newObject);
            }
        }
    }

    private static boolean objectMatchFunctionPrototype(UnnamedObjectType object, Snap.Obj globalObject) {
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
