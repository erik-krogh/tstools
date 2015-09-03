package dk.webbies.tscreate.paser;

/**
 * Created by erik1 on 01-09-2015.
 */
public class StringLiteral extends Expression {
    private String string;

    public StringLiteral(String string) {
        this.string = string;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
