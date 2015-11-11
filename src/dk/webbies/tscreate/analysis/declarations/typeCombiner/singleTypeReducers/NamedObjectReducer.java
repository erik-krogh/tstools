package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.NativeTypeFactory;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.NamedObjectType;
import dk.webbies.tscreate.analysis.unionFind.PrimitiveNode;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;
import dk.webbies.tscreate.jsnap.Snap;

/**
 * Created by Erik Krogh Kristensen on 08-11-2015.
 */
public class NamedObjectReducer implements SingleTypeReducer<NamedObjectType, NamedObjectType> {
    private final Snap.Obj global;
    private final NativeClassesMap nativeClasses;
    private final UnionFindSolver solver;
    private final NativeTypeFactory nativeFactory;

    public NamedObjectReducer(Snap.Obj global, NativeClassesMap nativeClasses) {
        this.global = global;
        this.nativeClasses = nativeClasses;
        this.solver = new UnionFindSolver();
        this.nativeFactory = new NativeTypeFactory(new PrimitiveNode.Factory(solver, global), solver, nativeClasses);
    }

    @Override
    public Class<NamedObjectType> getAClass() {
        return NamedObjectType.class;
    }

    @Override
    public Class<NamedObjectType> getBClass() {
        return NamedObjectType.class;
    }

    @Override
    public DeclarationType reduce(NamedObjectType one, NamedObjectType two) {
        if (one.getName().equals(two.getName())) {
            return one;
        } else {
            if (isSuperClass(one, two)) {
                return one;
            } else if (isSuperClass(two, one)) {
                return two;
            }
            return null;
        }
    }

    private boolean isSuperClass(NamedObjectType superClass, NamedObjectType subClass) {
        if (superClass.getName().equals("NodeList") && subClass.getName().equals("NodeListOf")) {
            return true;
        }
        // TODO: Lookup Prototypes and that stuff.
        return false;
    }
}
