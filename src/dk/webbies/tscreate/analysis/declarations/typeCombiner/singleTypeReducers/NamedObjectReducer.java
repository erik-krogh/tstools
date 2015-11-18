package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.NativeTypeFactory;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.NamedObjectType;
import dk.webbies.tscreate.analysis.unionFind.PrimitiveNode;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
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
            return returnSuper(one, two);
        } else {
            if (isSuperClass(one, two)) {
                return returnSuper(one, two);
            } else if (isSuperClass(two, one)) {
                return returnSuper(two, one);
            }

            Snap.Obj commonSuperClass = getCommonSuperClass(one, two);
            if (commonSuperClass != null) {
                String name = nativeClasses.nameFromPrototype(commonSuperClass);
                if (name != null && !name.equals("ObjectConstructor")) {
                    NamedObjectType result = new NamedObjectType(name);
                    result.addKnownSubTypes(one.getKnownSubTypes());
                    result.addKnownSubTypes(two.getKnownSubTypes());
                    result.addKnownSubType(one.getName());
                    result.addKnownSubType(two.getName());
                    return result;
                }
            }
            return null;
        }
    }

    private NamedObjectType returnSuper(NamedObjectType superType, NamedObjectType subType) {
        NamedObjectType result = new NamedObjectType(superType.getName());
        result.addKnownSubTypes(superType.getKnownSubTypes());
        result.addKnownSubTypes(subType.getKnownSubTypes());
        result.addKnownSubType(subType.getName());
        return result;
    }

    private Snap.Obj getCommonSuperClass(NamedObjectType one, NamedObjectType two) {
        Snap.Obj oneProto = getPrototype(one);
        Snap.Obj twoProto = getPrototype(two);

        if (twoProto == null || oneProto == null) {
            return null;
        }
        while (oneProto != null) {
            if (isSuperClass(oneProto, twoProto)) {
                return oneProto;
            }

            oneProto = oneProto.prototype;
        }

        return null;
    }

    private boolean isSuperClass(NamedObjectType superClass, NamedObjectType subClass) {
        // Special cases.
        if (superClass.getName().equals("NodeList") && subClass.getName().equals("NodeListOf")) {
            return true;
        }
        if (superClass.getName().equals("HTMLElement") && (subClass.getName().startsWith("HTML") || subClass.getName().startsWith("MSHTML"))) {
            return true;
        }


        Snap.Obj superProto = getPrototype(superClass);
        Snap.Obj subProto = getPrototype(subClass);

        //noinspection SimplifiableIfStatement
        if (subProto == null || superProto == null) {
            return false;
        }

        return isSuperClass(superProto, subProto);
    }

    private Snap.Obj getPrototype(NamedObjectType named) {
        Snap.Obj result = nativeClasses.prototypeFromName(named.getName());
        if (result != null) {
            return result;
        } else {
            Snap.Property prop = global.getProperty(named.getName());
            if (prop != null && prop.value instanceof Snap.Obj) {
                Snap.Obj obj = (Snap.Obj) prop.value;
                if (obj.getProperty("prototype") != null && obj.getProperty("prototype").value instanceof Snap.Obj) {
                    return (Snap.Obj) obj.getProperty("prototype").value;
                }
            }
        }
        return null;
    }

    private boolean isSuperClass(Snap.Obj superProto, Snap.Obj subProto) {
        while (subProto != null) {
            if (superProto == subProto) {
                return true;
            }

            subProto = subProto.prototype;
        }

        return false;
    }
}
