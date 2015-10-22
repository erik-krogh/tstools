package dk.webbies.tscreate.analysis.declarations.types.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.types.CombinationType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.FunctionType;
import dk.webbies.tscreate.analysis.declarations.types.typeCombiner.TypeReducer;

import java.util.ArrayList;

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
            String name = oneArg.getName();
            if (name.startsWith("arg")) {
                name = twoArg.getName();
            } else if (name.length() < twoArg.getName().length()) {
                name = twoArg.getName();
            }

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
}
