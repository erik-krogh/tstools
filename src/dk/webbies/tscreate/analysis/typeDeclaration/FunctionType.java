package dk.webbies.tscreate.analysis.typeDeclaration;

import dk.webbies.tscreate.analysis.TypeConverter;
import dk.webbies.tscreate.analysis.unionFind.FunctionNode;
import dk.webbies.tscreate.analysis.unionFind.UnionNode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.webbies.tscreate.analysis.TypeConverter.convert;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class FunctionType implements DeclarationType {
    private DeclarationType returnType;
    private List<DeclarationType> arguments;

    public FunctionType(DeclarationType returnType, List<DeclarationType> arguments) {
        this.returnType = returnType;
        this.arguments = arguments;
    }

    public static FunctionType fromNode(FunctionNode functionNode, Map<UnionNode, List<UnionNode>> classes) {
        DeclarationType returnType = convert(classes.get(functionNode.returnNode));

        System.out.println("ReturnType: " + returnType);

        List<DeclarationType> arguments = functionNode.arguments.stream().map(unionNode -> convert(classes.get(unionNode))).collect(Collectors.toList());

        return new FunctionType(returnType, arguments);
    }
}
