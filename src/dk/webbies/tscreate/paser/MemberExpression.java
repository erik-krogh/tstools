package dk.webbies.tscreate.paser;

/**
 * Created by erik1 on 01-09-2015.
 */
public class MemberExpression extends Expression {
    private final String property;
    private final Expression expression;

    public MemberExpression(String property, Expression expression) {
        this.property = property;
        this.expression = expression;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public String getProperty() {
        return property;
    }

    public Expression getExpression() {
        return expression;
    }
}
