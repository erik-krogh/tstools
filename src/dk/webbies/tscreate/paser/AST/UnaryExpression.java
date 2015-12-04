package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

/**
 * Created by Erik Krogh Kristensen on 06-09-2015.
 */
public class UnaryExpression extends Expression {
    private final Operator operator;
    private final Expression expression;

    public UnaryExpression(SourceRange loc, Operator operator, Expression expression) {
        super(loc);
        this.operator = operator;
        this.expression = expression;
    }

    public Operator getOperator() {
        return operator;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
    @Override
    public <T> T accept(CFGExpressionVisitor<T> visitor, T aux) {
        return visitor.visit(this, aux);
    }

}
