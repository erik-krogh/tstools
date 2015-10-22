package dk.webbies.tscreate.analysis.declarations.types.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.types.ClassType;
import dk.webbies.tscreate.analysis.declarations.types.CombinationType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.UnnamedObjectType;
import dk.webbies.tscreate.analysis.declarations.types.typeCombiner.TypeReducer;
import dk.webbies.tscreate.jsnap.Snap;

/**
 * Created by Erik Krogh Kristensen on 18-10-2015.
 */
public class ClassObjectReducer implements SingleTypeReducer<ClassType, UnnamedObjectType> {
    private final Snap.Obj globalObject;
    private final TypeReducer combiner;

    public ClassObjectReducer(Snap.Obj globalObject, TypeReducer combiner) {
        this.globalObject = globalObject;
        this.combiner = combiner;
    }

    @Override
    public Class<ClassType> getAClass() {
        return ClassType.class;
    }

    @Override
    public Class<UnnamedObjectType> getBClass() {
        return UnnamedObjectType.class;
    }

    @Override
    public DeclarationType reduce(ClassType clazz, UnnamedObjectType object) {
        if (true) {
            return clazz; // TODO:
        }
        if (FunctionObjectReducer.objectMatchFunctionPrototype(object, globalObject)) {
            return clazz;
        }
        Snap.Value functionPrototype = globalObject.getProperty("Function").value;
        object.getDeclarations().forEach((name, type) -> {
            if (PrimitiveObjectReducer.objectFieldMatchPrototypeOf(functionPrototype, name)) {
                // Do nothing, its just something on the prototype
            } else {
                if (clazz.getStaticFields().containsKey(name)) {
                    DeclarationType prevType = clazz.getStaticFields().get(name);
                    CombinationType newType = new CombinationType(combiner);
                    newType.addType(prevType);
                    newType.addType(type);
                    clazz.getStaticFields().put(name, newType);
                } else {
                    clazz.getStaticFields().put(name, type);
                }
            }
        });
        return clazz;
    }
}
