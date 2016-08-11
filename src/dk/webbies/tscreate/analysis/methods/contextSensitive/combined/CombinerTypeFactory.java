package dk.webbies.tscreate.analysis.methods.contextSensitive.combined;

import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.analysis.TypeFactory;
import dk.webbies.tscreate.analysis.declarations.types.CombinationType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.unionFind.UnionFeature;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A weird class, that makes sure that both the result of the Mixed analysis and the Subset analysis is sent to a single typeFactory.
 */
public class CombinerTypeFactory extends TypeFactory {
    public CombinerTypeFactory(Snap.Obj globalObject, Map<Snap.Obj, LibraryClass> libraryClasses, Options options, DeclarationParser.NativeClassesMap nativeClasses, TypeAnalysis typeAnalysis) {
        super(globalObject, libraryClasses, options, nativeClasses, typeAnalysis);
    }

    private Map<Snap.Obj, List<UnionFeature>> calledRegisterFunction = new HashMap<>();
    @Override
    public void registerFunction(Snap.Obj closure, List<UnionFeature> features) {
        if (calledRegisterFunction.containsKey(closure)) {
            List<UnionFeature> result = new ArrayList<>();
            result.addAll(calledRegisterFunction.get(closure));
            result.addAll(features);
            super.registerFunction(closure, result);
            return;
        }
        calledRegisterFunction.put(closure, features);
    }


    private Map<Snap.Obj, DeclarationType> calledResolvedFunctions = new HashMap<>();
    @Override
    public void putResolvedFunctionType(Snap.Obj closure, DeclarationType type) {
        if (calledResolvedFunctions.containsKey(closure)) {
            DeclarationType existingType = calledResolvedFunctions.get(closure);
            CombinationType resultingType = new CombinationType(super.typeReducer, existingType, type);
            super.putResolvedFunctionType(closure, resultingType);
            return;
        }
        calledResolvedFunctions.put(closure, type);
    }

    boolean resolveCalledOnce = false;
    @Override
    public void resolveClassTypes() {
        if (resolveCalledOnce) {
            super.resolveClassTypes();
        }
        resolveCalledOnce = true;
    }
}
