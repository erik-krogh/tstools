package dk.webbies.tscreate.paser;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class VariableNode extends Statement {
    private final Identifier identifier;
    private final Expression init;

    public VariableNode(int line, Identifier identifier, Expression init) {
        super(line);
        this.identifier = identifier;
        this.init = init;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Expression getInit() {
        return init;
    }
}
