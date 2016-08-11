package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.SameTypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.CombinationType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.FunctionType;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 18-10-2015.
 */
public class FunctionReducer extends SameTypeReducer<FunctionType> {
    private final TypeReducer combiner;

    public FunctionReducer(TypeReducer combiner, Map<DeclarationType, List<DeclarationType>> originals) {
        super(originals);
        this.combiner = combiner;
    }

    private static String getBestArgumentName(String one, String two) {
        if (one.startsWith("arg")) {
            return two;
        } else if (one.length() < two.length()) {
            return two;
        }
        return one;
    }

    public static String getBestArgumentName(Collection<String> names) {
        if (names.isEmpty()) {
            throw new RuntimeException();
        } else if (names.size() == 1) {
            return names.iterator().next();
        } else {
            String name = null;
            boolean first = true;
            for (String newName : names) {
                if (first) {
                    name = newName;
                    first = false;
                } else {
                    assert name != null;
                    name = getBestArgumentName(name, newName);
                }
            }
            return name;
        }
    }

    @Override
    public Class<FunctionType> getTheClass() {
        return FunctionType.class;
    }

    @Override
    protected FunctionType reduceIt(FunctionType one, FunctionType two) {
        if (isEmptyFunction(one)) {
            return two;
        }
        if (isEmptyFunction(two)) {
            return one;
        }
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

        List<FunctionExpression> astNodes = new ArrayList<>();
        astNodes.addAll(one.getAstNodes());
        astNodes.addAll(two.getAstNodes());
        FunctionType result = new FunctionType(returnType, arguments, Util.concatSet(one.getNames(), two.getNames()), astNodes);

        result.minArgs = Math.min(one.minArgs, two.minArgs);

        return result;
    }

    private boolean isEmptyFunction(FunctionType function) {
        return function.getArguments().isEmpty() && function.getReturnType() instanceof PrimitiveDeclarationType && ((PrimitiveDeclarationType) function.getReturnType()).getType() == PrimitiveDeclarationType.Type.VOID;
    }
}
