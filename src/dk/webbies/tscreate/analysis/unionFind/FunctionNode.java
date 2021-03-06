package dk.webbies.tscreate.analysis.unionFind;

import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.paser.AST.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class FunctionNode extends UnionNodeWithFields {
    public final UnionNode returnNode;
    public final List<UnionNode> arguments = new ArrayList<>();
    public final UnionNode thisNode;
    public Snap.Obj closure = null;

    public static final String FIELD_ARGUMENT_PREFIX = "function-argument-";
    public static final String FIELD_RETURN = "function-return";
    public static final String FIELD_THIS = "function-this";

    private FunctionNode(List<String> argumentNames, UnionFindSolver solver) {
        super(solver);
        this.returnNode = new EmptyNode(solver);
        this.thisNode = new EmptyNode(solver);
        for (int i = 0; i < argumentNames.size(); i++) {
            UnionNode node = new NameNode(solver, argumentNames.get(i));
            arguments.add(node);
            addField(FIELD_ARGUMENT_PREFIX + i, node);
        }
        addField(FIELD_RETURN, returnNode);
        addField(FIELD_THIS, thisNode);
    }

    public static FunctionNode create(List<String> argumentNames, UnionFindSolver solver) {
        return new FunctionNode(argumentNames, solver);
    }

    public static FunctionNode create(int size, UnionFindSolver solver) {
        return new FunctionNode(createArgumentNames(size), solver);
    }

    private static List<String> createArgumentNames(int size) {
        List<String> argNames = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            argNames.add("arg" + i);
        }
        return argNames;
    }

    public static FunctionNode create(FunctionExpression function, UnionFindSolver solver) {
        return new FunctionNode(function.getArguments().stream().map(Identifier::getName).collect(Collectors.toList()), solver);
    }

    public static FunctionNode create(Snap.Obj closure, UnionFindSolver solver) {
        String type = closure.function.type;
        switch (type) {
            case "user": {
                FunctionNode result = create(closure.function.astNode, solver);
                result.closure = closure;
                return result;
            }
            case "unknown":
            case "native": {
                FunctionNode result = create(0, solver);
                result.closure = closure;
                return result;
            }
            case "bind": {
                int boundArguments = closure.function.arguments.size() - 1;
                List<Identifier> allArguments = closure.function.target.function.astNode.getArguments();
                List<Identifier> freeArguments = allArguments.subList(boundArguments, allArguments.size());
                FunctionNode result = create(freeArguments.stream().map(Identifier::getName).collect(Collectors.toList()), solver);
                result.closure = closure;
                return result;
            }
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public void addTo(UnionClass unionClass) {
        ArrayList<UnionNode> arguments = new ArrayList<>(this.arguments);

        UnionFeature.FunctionFeature functionFeature = new UnionFeature.FunctionFeature(this.thisNode, this.returnNode, arguments, this.closure);
        UnionFeature feature = unionClass.getFeature();
        if (feature.functionFeature == null) {
            feature.functionFeature = functionFeature;
        } else {
            feature.functionFeature.takeIn(functionFeature);
        }
    }
}
