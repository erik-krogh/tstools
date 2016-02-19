package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

/**
 * Created by erik1 on 01-09-2015.
 */
public class BinaryExpression extends Expression {
    private final Expression lhs;
    private final Expression rhs;
    private final Operator operator;

    public BinaryExpression(SourceRange location, Expression lhs, Expression rhs, Operator operator) {
        super(location);
        this.lhs = lhs;
        this.rhs = rhs;
        this.operator = operator;
    }

    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }


    public Expression getLhs() {
        return lhs;
    }

    public Expression getRhs() {
        return rhs;
    }

    public Operator getOperator() {
        return operator;
    }
}
