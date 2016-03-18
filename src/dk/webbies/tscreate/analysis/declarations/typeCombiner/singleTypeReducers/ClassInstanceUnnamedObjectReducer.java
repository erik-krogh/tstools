package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.SingleTypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.ClassInstanceType;
import dk.webbies.tscreate.analysis.declarations.types.ClassType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.UnnamedObjectType;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Erik Krogh Kristensen on 27-10-2015.
 */
public class ClassInstanceUnnamedObjectReducer extends SingleTypeReducer<ClassInstanceType, UnnamedObjectType> {
    private final DeclarationParser.NativeClassesMap nativeClasses;

    public ClassInstanceUnnamedObjectReducer(DeclarationParser.NativeClassesMap nativeClasses, Map<DeclarationType, List<DeclarationType>> originals) {
        super(originals);
        this.nativeClasses = nativeClasses;
    }

    @Override
    public Class<ClassInstanceType> getAClass() {
        return ClassInstanceType.class;
    }

    @Override
    public Class<UnnamedObjectType> getBClass() {
        return UnnamedObjectType.class;
    }

    private Map<ClassType, Set<String>> classKeys = new HashMap<>();

    @Override
    public DeclarationType reduceIt(ClassInstanceType instance, UnnamedObjectType object) {
        LibraryClass libraryClass = instance.getClazz().getLibraryClass();
        if (!classKeys.containsKey(instance.getClazz())) {
            Set<String> set = ClassType.getFieldsInclSuper(instance.getClazz(), nativeClasses);
            classKeys.put(instance.getClazz(), set);
            if (libraryClass != null) {
                libraryClass.getInstances().stream().map(Snap.Obj::getPropertyMap).map(Map::keySet).forEach(set::addAll);
            } else {
                instance.getClazz().getPrototypeFields().keySet().forEach(set::add);
            }
        }

        Set<String> keySet = classKeys.get(instance.getClazz());
        if (object.getDeclarations().keySet().stream().allMatch(keySet::contains)) {
            return instance;
        } else {
            return null;
        }
    }
}
