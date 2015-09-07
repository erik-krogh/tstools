package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.StatementVisitor;

/**
 * Created by erik1 on 01-09-2015.
 */
public class ExpressionStatement extends Statement {
    private final Expression expression;

    public ExpressionStatement(SourceRange location, Expression expression) {
        super(location);
        this.expression = expression;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Expression getExpression() {
        return expression;
    }
}
