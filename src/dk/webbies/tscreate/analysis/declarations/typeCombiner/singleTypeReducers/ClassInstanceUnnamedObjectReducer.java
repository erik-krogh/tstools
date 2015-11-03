package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.types.ClassInstanceType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.UnnamedObjectType;

/**
 * Created by Erik Krogh Kristensen on 27-10-2015.
 */
public class ClassInstanceUnnamedObjectReducer implements SingleTypeReducer<ClassInstanceType, UnnamedObjectType> {
    @Override
    public Class<ClassInstanceType> getAClass() {
        return ClassInstanceType.class;
    }

    @Override
    public Class<UnnamedObjectType> getBClass() {
        return UnnamedObjectType.class;
    }

    @Override
    public DeclarationType reduce(ClassInstanceType classInstanceType, UnnamedObjectType unnamedObjectType) {
        if (unnamedObjectType.getDeclarations().keySet().stream().allMatch(key -> {
            return classInstanceType.getClazz().getPrototypeFields().containsKey(key);
        })) {
            return classInstanceType;
        } else {
            return null;
        }
    }
}
