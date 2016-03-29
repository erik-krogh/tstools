package dk.webbies.tscreate.cleanup.heuristics;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers.FunctionReducer;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.cleanup.CollectEveryTypeVisitor;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.cleanup.heuristics.HeuristicsUtil.hasFunction;

/**
 * Created by erik1 on 15-03-2016.
 */
public class FindFunctionsHeuristic implements ReplacementHeuristic{
    private final TypeReducer reducer;

    public FindFunctionsHeuristic(TypeReducer reducer) {
        this.reducer = reducer;
    }

    @Override
    public Multimap<DeclarationType, DeclarationType> findReplacements(CollectEveryTypeVisitor collected) {
        Set<UnnamedObjectType> objects = Util.cast(UnnamedObjectType.class, collected.getEverythingByType().get(UnnamedObjectType.class));
        Set<InterfaceDeclarationType> interfaces = Util.cast(InterfaceDeclarationType.class, collected.getEverythingByType().get(InterfaceDeclarationType.class));

        if (objects == null && interfaces == null) {
            return null;
        }
        ArrayListMultimap<DeclarationType, DeclarationType> replacements = ArrayListMultimap.create();
        if (objects != null) {
            for (UnnamedObjectType object : objects) {
                runOnObject(replacements, object);
            }
        }
        if (interfaces != null) {
            for (InterfaceDeclarationType anInterface : interfaces) {
                if (anInterface.getObject() != null) {
                    runOnInterface(replacements, anInterface);
                }
            }
        }

        return replacements;
    }

    private void runOnInterface(ArrayListMultimap<DeclarationType, DeclarationType> replacements, InterfaceDeclarationType anInterface) {
        UnnamedObjectType object = anInterface.getObject();
        if (!objectCouldBeFunction(object) && !hasFunction(anInterface)) {
            return;
        }
        if (object.getDeclarations().keySet().stream().anyMatch(key -> key.equals("call")) && object.getDeclarations().keySet().stream().anyMatch(key -> key.equals("apply"))) {
            DeclarationType callType = object.getDeclarations().get("call");
            DeclarationType applyType = object.getDeclarations().get("apply");
            if (!(callType instanceof FunctionType) || !(applyType instanceof FunctionType)) {
                return;
            }
            FunctionType callFunc = getFunctionFromCallProp(object, (FunctionType) callType);
            FunctionType applyFunc = getFunctionFromApplyProp(object, (FunctionType) callType);

            FunctionType resultFunc = reduceFunctions(callFunc, applyFunc);

            putFunctionOnInterface(replacements, anInterface, resultFunc, Util.createSet("call", "apply"));

        } else if (object.getDeclarations().keySet().stream().anyMatch(key -> key.equals("call"))) {
            DeclarationType fieldType = object.getDeclarations().get("call");
            if (!(fieldType instanceof FunctionType)) {
                return;
            }
            FunctionType function = getFunctionFromCallProp(object, (FunctionType) fieldType);

            putFunctionOnInterface(replacements, anInterface, function, "call");

        } else if (object.getDeclarations().keySet().stream().anyMatch(key -> key.equals("apply"))) {
            DeclarationType fieldType = object.getDeclarations().get("apply");
            if (!(fieldType instanceof FunctionType)) {
                return;
            }
            FunctionType function = getFunctionFromApplyProp(object, (FunctionType) fieldType);

            putFunctionOnInterface(replacements, anInterface, function, "apply");
        }
    }

    private FunctionType reduceFunctions(FunctionType one, FunctionType two) {
        return (FunctionType) new FunctionReducer(reducer, reducer.originals).reduce(one, two);
    }

    private FunctionType getFunctionFromApplyProp(UnnamedObjectType object, FunctionType fieldType) {
        DeclarationType returnType = fieldType.getReturnType();
        FunctionType result = new FunctionType(returnType, Collections.EMPTY_LIST, object.getNames());
        this.reducer.originals.put(result, Collections.singletonList(object));
        return result;
    }

    private static final Set<String> keysInFunction = new HashSet<>(Arrays.asList("length", "name", "arguments", "caller", "apply", "bind", "call", "toString", "constructor"));
    private boolean objectCouldBeFunction(UnnamedObjectType object) {
        return object.getDeclarations().keySet().stream().allMatch(keysInFunction::contains);
    }

    private void putFunctionOnInterface(ArrayListMultimap<DeclarationType, DeclarationType> replacements, InterfaceDeclarationType anInterface, FunctionType function, String keyToRemove) {
        putFunctionOnInterface(replacements, anInterface, function, Util.createSet(keyToRemove));
    }

    private void putFunctionOnInterface(ArrayListMultimap<DeclarationType, DeclarationType> replacements, InterfaceDeclarationType anInterface, FunctionType function, Set<String> keysToRemove) {
        InterfaceDeclarationType result = new InterfaceDeclarationType(anInterface.name, anInterface.getNames());
        result.setDynamicAccess(anInterface.getDynamicAccess());
        Map<String, DeclarationType> filteredDeclarations = anInterface.getObject().getDeclarations().entrySet().stream().filter(entry -> !keysToRemove.contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        UnnamedObjectType newObject = new UnnamedObjectType(filteredDeclarations, anInterface.getNames());
        this.reducer.originals.put(newObject, Collections.singletonList(anInterface.getObject()));
        result.setObject(newObject);

        if (anInterface.getFunction() != null) {
            result.setFunction(reduceFunctions(anInterface.getFunction(), function));
        } else {
            result.setFunction(function);
        }
        replacements.put(anInterface, result);
    }

    private FunctionType getFunctionFromCallProp(UnnamedObjectType object, FunctionType fieldType) {
        DeclarationType returnType = fieldType.getReturnType();
        List<FunctionType.Argument> arguments = new ArrayList<>();
        List<FunctionType.Argument> callArguments = fieldType.getArguments();
        for (int i = 1; i < callArguments.size(); i++) {
            arguments.add(callArguments.get(i));
        }
        FunctionType result = new FunctionType(returnType, arguments, object.getNames());
        this.reducer.originals.put(result, Collections.singletonList(object));
        return result;
    }

    @Override
    public String getDescription() {
        return "{apply:..., call:...} -> Function";
    }

    private void runOnObject(ArrayListMultimap<DeclarationType, DeclarationType> replacements, UnnamedObjectType object) {
        if (!objectCouldBeFunction(object)) {
            return;
        }
        if (object.getDeclarations().keySet().stream().anyMatch(key -> key.equals("call"))) {
            DeclarationType fieldType = object.getDeclarations().get("call");
            if (!(fieldType instanceof FunctionType)) {
                return;
            }
            FunctionType function = getFunctionFromCallProp(object, (FunctionType) fieldType);

            replacements.put(object, function);

        } else if (object.getDeclarations().keySet().stream().anyMatch(key -> key.equals("apply"))) {
            DeclarationType fieldType = object.getDeclarations().get("apply");
            if (!(fieldType instanceof FunctionType)) {
                return;
            }
            FunctionType function = getFunctionFromApplyProp(object, (FunctionType) fieldType);

            replacements.put(object, function);
        }
    }
}
