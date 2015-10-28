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

    private final List<String> argumentNames;

    private FunctionNode(List<String> argumentNames) {
        this.argumentNames = argumentNames;
        this.returnNode = new EmptyUnionNode();
        this.thisNode = new EmptyUnionNode();
        for (int i = 0; i < argumentNames.size(); i++) {
            EmptyUnionNode node = new EmptyUnionNode();
            arguments.add(node);
            addField("function-argument-" + i, node);
        }
        addField("function-return", returnNode);
        addField("function-this", thisNode);
    }

    public static  FunctionNode create(List<String> argumentNames) {
        return new FunctionNode(argumentNames);
    }

    public static FunctionNode create(int size) {
        return new FunctionNode(createArgumentNames(size));
    }

    private static List<String> createArgumentNames(int size) {
        List<String> argNames = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            argNames.add("arg" + i);
        }
        return argNames;
    }

    public static FunctionNode create(FunctionExpression function) {
        FunctionNode result = new FunctionNode(function.getArguments().stream().map(Identifier::getName).collect(Collectors.toList()));
        return result;
    }

    public static FunctionNode create(Snap.Obj closure) {
        String type = closure.function.type;
        if (type.equals("user")) {
            FunctionNode result = create(closure.function.astNode);
            result.closure = closure;
            return result;
        } else if (type.equals("unknown") || type.equals("native")) {
            FunctionNode result = create(0);
            result.closure = closure;
            return result;
        } else if (type.equals("bind")) {
            int boundArguments = closure.function.arguments.size() - 1;
            List<Identifier> allArguments = closure.function.target.function.astNode.getArguments();
            List<Identifier> freeArguments = allArguments.subList(boundArguments, allArguments.size());
            FunctionNode result = create(freeArguments.stream().map(Identifier::getName).collect(Collectors.toList()));
            result.closure = closure;
            return result;
        }
        throw new RuntimeException();
    }

    public static FunctionNode create(Snap.Obj closure, List<String> argumentNames) {
        FunctionNode result = create(argumentNames);
        result.closure = closure;
        return result;
    }

    @Override
    public void addTo(UnionClass unionClass) {
        ArrayList<UnionFeature.FunctionFeature.Argument> arguments = new ArrayList<>();
        for (int i = 0; i < this.arguments.size(); i++) {
            UnionNode node = this.arguments.get(i);
            String name = this.argumentNames.get(i);
            arguments.add(new UnionFeature.FunctionFeature.Argument(name, node));
        }

        UnionFeature.FunctionFeature functionFeature = new UnionFeature.FunctionFeature(this.thisNode, this.returnNode, arguments, this.closure);
        UnionFeature feature = unionClass.getFeature();
        if (feature.functionFeature == null) {
            feature.functionFeature = functionFeature;
        } else {
            feature.functionFeature.takeIn(functionFeature);
        }
    }
}
