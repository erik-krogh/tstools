package dk.webbies.tscreate.analysis.declarations.types.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.types.ClassInstanceType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;

/**
 * Created by Erik Krogh Kristensen on 27-10-2015.
 */
public class ClassInstanceReducer implements SingleTypeReducer<ClassInstanceType, ClassInstanceType> {
    @Override
    public Class<ClassInstanceType> getAClass() {
        return ClassInstanceType.class;
    }

    @Override
    public Class<ClassInstanceType> getBClass() {
        return ClassInstanceType.class;
    }

    @Override
    public DeclarationType reduce(ClassInstanceType one, ClassInstanceType two) {
        if (one.getClazz() == two.getClazz()) {
            return one;
        } else {
            return null;
        }
    }
}
