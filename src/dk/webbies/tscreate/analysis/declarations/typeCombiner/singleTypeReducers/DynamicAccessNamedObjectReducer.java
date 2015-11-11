package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.analysis.NativeTypeFactory;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.DynamicAccessType;
import dk.webbies.tscreate.analysis.declarations.types.NamedObjectType;
import dk.webbies.tscreate.analysis.unionFind.PrimitiveNode;
import dk.webbies.tscreate.analysis.unionFind.UnionFeature;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.jsnap.Snap;

/**
 * Created by Erik Krogh Kristensen on 11-11-2015.
 */
public class DynamicAccessNamedObjectReducer implements SingleTypeReducer<DynamicAccessType, NamedObjectType> {
    private final UnionFindSolver solver;
    private final NativeTypeFactory nativeFactory;
    private final DeclarationParser.NativeClassesMap nativeClasses;

    public DynamicAccessNamedObjectReducer(Snap.Obj global, DeclarationParser.NativeClassesMap nativeClasses) {
        this.nativeClasses = nativeClasses;
        this.solver = new UnionFindSolver();
        this.nativeFactory = new NativeTypeFactory(new PrimitiveNode.Factory(solver, global), solver, nativeClasses);
    }

    @Override
    public Class<DynamicAccessType> getAClass() {
        return DynamicAccessType.class;
    }

    @Override
    public Class<NamedObjectType> getBClass() {
        return NamedObjectType.class;
    }

    @Override
    public DeclarationType reduce(DynamicAccessType dynamic, NamedObjectType named) {
        Type type = nativeClasses.typeFromName(named.getName());
        if (type == null) {
            return null;
        }
        UnionFeature feature = solver.union(nativeFactory.fromType(type)).getFeature();
        if (feature.getDynamicAccessLookupExp() == null) {
            return null;
        } else {
            // There is some dynamic access on the named type, that is good enough for me.
            return named;
        }
    }
}
