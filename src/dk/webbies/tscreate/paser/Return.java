package dk.webbies.tscreate.paser;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;

/**
 * Created by erik1 on 01-09-2015.
 */
public class Return extends Statement {
    private final Expression expression;

    Return(SourceRange location, Expression expression) {
        super(location);
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
