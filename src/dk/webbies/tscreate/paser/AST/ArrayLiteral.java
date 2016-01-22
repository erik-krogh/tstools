package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

import java.util.List;

/**
 * Created by erik1 on 22-01-2016.
 */
public class ArrayLiteral extends Expression {
    private final List<Expression> expressions;

    ArrayLiteral(SourceRange location, List<Expression> expression) {
        super(location);
        this.expressions = expression;
    }

    public List<Expression> getExpressions() {
        return expressions;
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
