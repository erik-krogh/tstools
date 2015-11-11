package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.analysis.NativeTypeFactory;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.NamedObjectType;
import dk.webbies.tscreate.analysis.declarations.types.UnnamedObjectType;
import dk.webbies.tscreate.analysis.unionFind.PrimitiveNode;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.analysis.unionFind.UnionNode;
import dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Erik Krogh Kristensen on 18-10-2015.
 */
public class NamedUnamedObjectReducer implements SingleTypeReducer<UnnamedObjectType, NamedObjectType> {
    private final Snap.Obj global;
    private final NativeTypeFactory nativeFactory;
    private NativeClassesMap nativeClasses;
    private final UnionFindSolver solver;

    public NamedUnamedObjectReducer(Snap.Obj global, NativeClassesMap nativeClasses) {
        this.global = global;
        this.nativeClasses = nativeClasses;
        this.solver = new UnionFindSolver();
        this.nativeFactory = new NativeTypeFactory(new PrimitiveNode.Factory(solver, global), solver, nativeClasses);
    }

    @Override
    public Class<UnnamedObjectType> getAClass() {
        return UnnamedObjectType.class;
    }

    @Override
    public Class<NamedObjectType> getBClass() {
        return NamedObjectType.class;
    }

    @Override
    public DeclarationType reduce(UnnamedObjectType unnamedObjectType, NamedObjectType named) {
        Snap.Property prop = global.getProperty(named.getName());
        if (prop == null) {
            // Can't say anything, so we assume that the object does contain it.
            return named;
        }
        if (objectMatchPrototype(unnamedObjectType, (Snap.Obj) prop.value, nativeClasses.typeFromName(named.getName()))) {
            return named;
        } else {
            return null;
        }
    }

    public boolean objectMatchPrototype(UnnamedObjectType object, Snap.Obj prototype, Type type) {
        Set<String> keys = getKeys(prototype);
        if (type != null) {
            Map<String, UnionNode> objectFields = this.solver.union(this.nativeFactory.fromType(type)).getFeature().getObjectFields();
            if (objectFields != null) {
                keys.addAll(objectFields.keySet());
            }
        }
        return object.getDeclarations().keySet().stream().allMatch(keys::contains);
    }

    public static Set<String> getKeys(Snap.Obj obj) {
        HashSet<String> result = new HashSet<>();
        result.addAll(obj.getPropertyMap().keySet());
        if (obj.prototype != null && obj != obj.prototype) {
            result.addAll(getKeys(obj.prototype));
        }
        return result;
    }
}
