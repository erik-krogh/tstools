package dk.webbies.tscreate.paser;

import java.util.Collection;

/**
 * Created by erik1 on 01-09-2015.
 */
public class BinaryExpression extends Expression {
    private final Expression lhs;
    private final Expression rhs;
    private final Operation operation;

    public BinaryExpression(Expression lhs, Expression rhs, Operation operation) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.operation = operation;
    }

    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Expression getLhs() {
        return lhs;
    }

    public Expression getRhs() {
        return rhs;
    }

    public Operation getOperation() {
        return operation;
    }
}
