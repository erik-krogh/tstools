package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.types.ClassInstanceType;
import dk.webbies.tscreate.analysis.declarations.types.ClassType;
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
        } else if (isSuperClass(one.getClazz(), two.getClazz())) {
            // One is a superClass of two.
            return one;
        } else if (isSuperClass(two.getClazz(), one.getClazz())) {
            // Two is a superClass of one.
            return two;
        } else{
            return null;
        }
    }

    private boolean isSuperClass(ClassType superClass, ClassType subClass) {
        while (subClass != null) {
            if (superClass == subClass) {
                return true;
            }
            subClass = subClass.getSuperClass();
        }
        return false;
    }
}
