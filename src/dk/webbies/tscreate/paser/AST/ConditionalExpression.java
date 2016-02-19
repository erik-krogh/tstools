package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

/**
 * Created by Erik Krogh Kristensen on 07-09-2015.
 */
public class ConditionalExpression extends Expression {
    private final Expression condition;
    private final Expression left;
    private final Expression right;

    public ConditionalExpression(SourceRange loc, Expression condition, Expression left, Expression right) {
        super(loc);
        this.condition = condition;
        this.left = left;
        this.right = right;
    }

    public Expression getCondition() {
        return condition;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
