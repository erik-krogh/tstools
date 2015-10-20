package dk.webbies.tscreate.analysis.declarations.types.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.types.ClassType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.InterfaceType;
import dk.webbies.tscreate.analysis.declarations.types.typeCombiner.TypeReducer;
import dk.webbies.tscreate.jsnap.Snap;

/**
 * Created by Erik Krogh Kristensen on 20-10-2015.
 */
public class ClassInterfaceReducer implements SingleTypeReducer<ClassType, InterfaceType> {
    private final Snap.Obj globalObject;
    private final TypeReducer combiner;

    public ClassInterfaceReducer(Snap.Obj globalObject, TypeReducer combiner) {
        this.globalObject = globalObject;
        this.combiner = combiner;
    }

    @Override
    public Class<ClassType> getAClass() {
        return ClassType.class;
    }

    @Override
    public Class<InterfaceType> getBClass() {
        return InterfaceType.class;
    }

    @Override
    public DeclarationType reduce(ClassType classType, InterfaceType interfaceType) throws CantReduceException {
        if (interfaceType.object != null) {
            return new ClassObjectReducer(globalObject, combiner).reduce(classType, interfaceType.getObject());
        } else {
            return classType;
        }
    }
}
