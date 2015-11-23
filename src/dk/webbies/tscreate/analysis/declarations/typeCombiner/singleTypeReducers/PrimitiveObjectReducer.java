package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.SingleTypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.UnnamedObjectType;
import dk.webbies.tscreate.jsnap.Snap;

/**
 * Created by Erik Krogh Kristensen on 17-10-2015.
 */
public class PrimitiveObjectReducer implements SingleTypeReducer<PrimitiveDeclarationType, UnnamedObjectType> {
    private final Snap.Obj globalObject;

    public PrimitiveObjectReducer(Snap.Obj globalObject) {
        this.globalObject = globalObject;
    }

    @Override
    public Class<PrimitiveDeclarationType> getAClass() {
        return PrimitiveDeclarationType.class;
    }

    @Override
    public Class<UnnamedObjectType> getBClass() {
        return UnnamedObjectType.class;
    }

    @Override
    public DeclarationType reduce(PrimitiveDeclarationType primitive, UnnamedObjectType object) {
        if (objectPropsMatchPrimitivePrototypes(primitive, object)) {
            return primitive;
        } else {
            return null;
        }
    }

    private boolean objectPropsMatchPrimitivePrototypes(PrimitiveDeclarationType primitive, UnnamedObjectType object) {
        return object.getDeclarations().keySet().stream().allMatch(key -> {
            boolean matchObject = objectFieldMatchPrototypeOf(this.globalObject.getProperty("Object").value, key);
            boolean matchPrimitive;
            switch (primitive.getType()) {
                case NUMBER:
                    matchPrimitive = objectFieldMatchPrototypeOf(this.globalObject.getProperty("Number").value, key); break;
                case BOOLEAN:
                    matchPrimitive = objectFieldMatchPrototypeOf(this.globalObject.getProperty("Boolean").value, key); break;
                case STRING:
                    matchPrimitive = objectFieldMatchPrototypeOf(this.globalObject.getProperty("String").value, key); break;
                case STRING_OR_NUMBER:
                    matchPrimitive = objectFieldMatchPrototypeOf(this.globalObject.getProperty("String").value, key)
                                  || objectFieldMatchPrototypeOf(this.globalObject.getProperty("Number").value, key);
                    break;
                case VOID:
                case NON_VOID:
                case ANY:
                    throw new RuntimeException("Not supposed to be any any, void or undefined at this point.");
                default:
                    throw new UnsupportedOperationException("Don't know this " + primitive + ", when checking if the object accesses match the primitive");
            }
            return matchObject || matchPrimitive;
        });
    }

    static boolean objectFieldMatchPrototypeOf(Snap.Value value, String key) {
        if (!(value instanceof Snap.Obj)) {
            throw new RuntimeException();
        }
        Snap.Obj prototype = (Snap.Obj) ((Snap.Obj) value).getProperty("prototype").value;
        return prototype.getProperty(key) != null;
    }
}
