package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.SameTypeReducer;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.SingleTypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.ClassInstanceType;
import dk.webbies.tscreate.analysis.declarations.types.ClassType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;

import java.util.List;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 27-10-2015.
 */
public class ClassInstanceReducer extends SameTypeReducer<ClassInstanceType> {
    public ClassInstanceReducer(Map<DeclarationType, List<DeclarationType>> originals) {
        super(originals);
    }

    @Override
    public Class<ClassInstanceType> getTheClass() {
        return ClassInstanceType.class;
    }

    @Override
    public ClassInstanceType reduceIt(ClassInstanceType one, ClassInstanceType two) {
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
            if (subClass.getSuperClass() instanceof ClassType) {
                subClass = (ClassType) subClass.getSuperClass();
            } else {
                break;
            }
        }
        return false;
    }
}
