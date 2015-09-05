package dk.webbies.tscreate.paser;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;

/**
 * Created by erik1 on 01-09-2015.
 */
public class BinaryExpression extends Expression {
    private final Expression lhs;
    private final Expression rhs;
    private final Operator operation;

    public BinaryExpression(SourceRange location, Expression lhs, Expression rhs, Operator operation) {
        super(location);
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

    public Operator getOperation() {
        return operation;
    }
}
