package dk.webbies.tscreate.paser;

/**
 * Created by erik1 on 01-09-2015.
 */
public class NullLiteral extends Expression {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
