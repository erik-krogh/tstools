package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

import java.util.List;

/**
 * Created by Erik Krogh Kristensen on 07-09-2015.
 */
public class CommaExpression extends Expression {
    private final List<Expression> expressions;

    public CommaExpression(SourceRange loc, List<Expression> expressions) {
        super(loc);
        if (expressions.size() == 0) {
            throw new RuntimeException("Cannot have 0 expressions in a comman-operator");
        }
        this.expressions = expressions;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public Expression getLastExpression() {
        return expressions.get(expressions.size() - 1);
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
