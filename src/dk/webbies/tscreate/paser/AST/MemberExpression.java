package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

/**
 * Created by erik1 on 01-09-2015.
 */
public class MemberExpression extends Expression {
    private final String property;
    private final Expression expression;

    public MemberExpression(SourceRange location, String property, Expression expression) {
        super(location);
        this.property = property;
        this.expression = expression;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public String getProperty() {
        return property;
    }

    public Expression getExpression() {
        return expression;
    }
}
