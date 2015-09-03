package dk.webbies.tscreate.paser;

/**
 * Created by erik1 on 01-09-2015.
 */
public class ExpressionStatement extends Statement {
    private final Expression expression;

    public ExpressionStatement(int lineNumber, Expression expression) {
        super(lineNumber);
        this.expression = expression;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Expression getExpression() {
        return expression;
    }
}
