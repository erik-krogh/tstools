package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.StatementVisitor;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class VariableNode extends Statement {
    private final Expression lValue;
    private final Expression init;

    public VariableNode(SourceRange location, Expression lValue, Expression init) {
        super(location);
        this.lValue = lValue;
        this.init = init;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Expression getlValue() {
        return lValue;
    }

    public Expression getInit() {
        return init;
    }
}
