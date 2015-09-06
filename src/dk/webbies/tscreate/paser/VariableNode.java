package dk.webbies.tscreate.paser;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class VariableNode extends Statement {
    private final AstNode lValue; // TODO: More precise, both of them.
    private final AstNode init;

    public VariableNode(SourceRange location, AstNode lValue, AstNode init) {
        super(location);
        this.lValue = lValue;
        this.init = init;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public AstNode getlValue() {
        return lValue;
    }

    public AstNode getInit() {
        return init;
    }
}
