package dk.webbies.tscreate.paser;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class VariableNode extends Statement {
    private final Node lValue; // TODO: More precise, both of them.
    private final Node init;

    public VariableNode(SourceRange location, Node lValue, Node init) {
        super(location);
        this.lValue = lValue;
        this.init = init;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Node getlValue() {
        return lValue;
    }

    public Node getInit() {
        return init;
    }
}
