package dk.webbies.tscreate.cleanup.heuristics;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.FunctionType;
import dk.webbies.tscreate.analysis.declarations.types.InterfaceDeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.UnnamedObjectType;
import dk.webbies.tscreate.cleanup.CollectEveryTypeVisitor;
import dk.webbies.tscreate.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by erik1 on 15-03-2016.
 */
public class FindFunctionsHeuristic implements ReplacementHeuristic{

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
                runOnObject(replacements, object, object);
            }
        }
        if (interfaces != null) {
            for (InterfaceDeclarationType anInterface : interfaces) {
                if (anInterface.getObject() != null && anInterface.getFunction() == null) {
                    runOnObject(replacements, anInterface.getObject(), anInterface);
                }
            }
        }

        return replacements;
    }

    private void runOnObject(ArrayListMultimap<DeclarationType, DeclarationType> replacements, UnnamedObjectType object, DeclarationType fromType) {
        if (object.getDeclarations().size() == 1) {
            String key = object.getDeclarations().keySet().iterator().next();
            DeclarationType fieldType = object.getDeclarations().values().iterator().next().resolve();
            if (!(fieldType instanceof FunctionType)) {
                return;
            }
            DeclarationType returnType = ((FunctionType) fieldType).getReturnType();
            if (key.equals("call")) {
                List<FunctionType.Argument> arguments = new ArrayList<>();
                List<FunctionType.Argument> callArguments = ((FunctionType) fieldType).getArguments();
                for (int i = 1; i < callArguments.size(); i++) {
                    arguments.add(callArguments.get(i));
                }
                replacements.put(fromType, new FunctionType(returnType, arguments, object.getNames()));
            } else if (key.equals("apply")) {
                replacements.put(fromType, new FunctionType(returnType, Collections.EMPTY_LIST, object.getNames()));
            }
        }
    }
}
