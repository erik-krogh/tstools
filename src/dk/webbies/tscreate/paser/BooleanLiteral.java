package dk.webbies.tscreate.paser;

/**
 * Created by erik1 on 01-09-2015.
 */
public class BooleanLiteral extends Expression {
    private boolean b;

    public BooleanLiteral(boolean b) {
        this.b = b;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
