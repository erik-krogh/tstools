package dk.webbies.tscreate.paser;

/**
 * Created by erik1 on 01-09-2015.
 */
public class Identifier extends Expression {
    private final String name;

    Identifier(String name) {
        this.name = name;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
