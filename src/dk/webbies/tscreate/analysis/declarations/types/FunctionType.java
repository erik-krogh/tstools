package dk.webbies.tscreate.analysis.declarations.types;

import dk.webbies.tscreate.analysis.unionFind.UnionClass;
import dk.webbies.tscreate.analysis.unionFind.nodes.FunctionNode;
import dk.webbies.tscreate.analysis.unionFind.nodes.UnionNode;
import dk.webbies.tscreate.paser.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.analysis.TypeConverter.convert;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class FunctionType implements DeclarationType {
    private DeclarationType returnType;
    private List<Argument> arguments;

    public FunctionType(DeclarationType returnType, List<Argument> arguments) {
        this.returnType = returnType;
        this.arguments = arguments;
    }

    public DeclarationType getReturnType() {
        return returnType;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public static FunctionType fromNode(FunctionNode functionNode, Map<UnionNode, UnionClass> classes) {
        UnionClass returnNode = classes.get(functionNode.returnNode);
        DeclarationType returnType;
        if (returnNode != null) {
            returnType = convert(returnNode, classes);
        } else {
            returnType = PrimitiveDeclarationType.VOID;
        }

        List<DeclarationType> argumentTypes = functionNode.arguments.stream().map(unionNode -> convert(classes.get(unionNode), classes)).collect(Collectors.toList());
        ArrayList<Argument> declarations = new ArrayList<>();
        List<Identifier> argIds = functionNode.astFunction.getArguments();
        for (int i = 0; i < argumentTypes.size(); i++) {
            DeclarationType argType = argumentTypes.get(i);
            String name = argIds.get(i).getName();
            declarations.add(new Argument(name, argType));
        }
        return new FunctionType(returnType, declarations);
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public static class Argument {
        private String name;
        private DeclarationType type;

        public Argument(String name, DeclarationType type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public DeclarationType getType() {
            return type;
        }
    }
}
