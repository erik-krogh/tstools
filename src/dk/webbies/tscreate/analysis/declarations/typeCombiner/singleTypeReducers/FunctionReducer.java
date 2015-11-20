package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.types.CombinationType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.FunctionType;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Erik Krogh Kristensen on 18-10-2015.
 */
public class FunctionReducer implements SingleTypeReducer<FunctionType, FunctionType> {
    private final TypeReducer combiner;

    public FunctionReducer(TypeReducer combiner) {
        this.combiner = combiner;
    }

    @Override
    public Class<FunctionType> getAClass() {
        return FunctionType.class;
    }

    @Override
    public Class<FunctionType> getBClass() {
        return FunctionType.class;
    }

    @Override
    public DeclarationType reduce(FunctionType one, FunctionType two) {
        CombinationType returnType = new CombinationType(combiner);
        returnType.addType(one.getReturnType());
        returnType.addType(two.getReturnType());

        ArrayList<FunctionType.Argument> arguments = new ArrayList<>();
        for (int i = 0; i < Math.min(one.getArguments().size(), two.getArguments().size()); i++) {
            FunctionType.Argument oneArg = one.getArguments().get(i);
            FunctionType.Argument twoArg = two.getArguments().get(i);
            String name = getBestArgumentName(oneArg.getName(), twoArg.getName());

            CombinationType argType = new CombinationType(combiner);
            argType.addType(oneArg.getType());
            argType.addType(twoArg.getType());

            arguments.add(new FunctionType.Argument(name, argType));
        }

        for (int i = Math.min(one.getArguments().size(), two.getArguments().size()); i < Math.max(one.getArguments().size(), two.getArguments().size()); i++) {
            FunctionType.Argument arg = i >= one.getArguments().size() ? two.getArguments().get(i) : one.getArguments().get(i);
            arguments.add(arg);
        }

        return new FunctionType(returnType, arguments);
    }

    private static String getBestArgumentName(String one, String two) {
        if (one.startsWith("arg")) {
            return two;
        } else if (one.length() < two.length()) {
            return two;
        }
        return one;
    }

    public static String getBestArgumentName(List<String> names) {
        if (names.isEmpty()) {
            throw new RuntimeException();
        } else if (names.size() == 1) {
            return names.iterator().next();
        } else {
            String name = names.get(0);
            for (int i = 1; i < names.size(); i++) {
                name = getBestArgumentName(name, names.get(i));
            }
            return name;
        }
    }
}
