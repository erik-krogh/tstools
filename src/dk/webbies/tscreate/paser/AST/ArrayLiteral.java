package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

import java.util.List;

/**
 * Created by Erik Krogh Kristensen on 07-09-2015.
 */
public class ArrayLiteral extends Expression {
    private final List<Expression> expressions;

    public ArrayLiteral(SourceRange loc, List<Expression> expressions) {
        super(loc);
        this.expressions = expressions;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
