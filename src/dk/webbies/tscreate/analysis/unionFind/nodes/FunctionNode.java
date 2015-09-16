package dk.webbies.tscreate.analysis.unionFind.nodes;

import dk.webbies.tscreate.jsnapconvert.Snap;
import dk.webbies.tscreate.paser.AST.FunctionExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class FunctionNode extends UnionNodeWithFields {
    public final UnionNode returnNode;
    public final List<UnionNode> arguments = new ArrayList<>();
    public FunctionExpression astFunction;
    public final UnionNode thisNode;
    public Snap.Obj closure = null;

    public boolean hasAnalyzed = false; // For when analysing the functions separately.

    public FunctionNode(int numberOfArguments) {
        this.returnNode = new EmptyUnionNode();
        this.thisNode = new EmptyUnionNode();
        for (int i = 0; i < numberOfArguments; i++) {
            EmptyUnionNode node = new EmptyUnionNode();
            arguments.add(node);
            addField("function-argument-" + i, node);
        }
        addField("function-return", returnNode);
        addField("function-this", thisNode);
    }

    public FunctionNode(FunctionExpression function) {
        this(function.getArguments().size());
        this.astFunction = function;
    }

    public FunctionNode(Snap.Obj closure) {
        this(closure.function.astNode);
        this.closure = closure;
    }

    public FunctionNode(Snap.Obj closure, int numberOfArguments) {
        this(numberOfArguments);
        this.closure = closure;
    }
}
