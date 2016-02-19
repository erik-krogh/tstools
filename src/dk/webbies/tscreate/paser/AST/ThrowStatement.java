package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.StatementVisitor;

/**
 * Created by Erik Krogh Kristensen on 07-09-2015.
 */
public class ThrowStatement extends Statement {
    private final Expression expression;

    public ThrowStatement(SourceRange loc, Expression expression) {
        super(loc);
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
