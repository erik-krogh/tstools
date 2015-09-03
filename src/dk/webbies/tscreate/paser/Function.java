package dk.webbies.tscreate.paser;

import java.util.List;

/**
 * Created by erik1 on 01-09-2015.
 */
public class Function extends Expression {
    private String name;
    private final BlockStatement body;
    private final List<Identifier> arguments;

    public Function(String name, BlockStatement body, List<Identifier> arguments) {
        this.name = name;
        this.body = body;
        this.arguments = arguments;
    }

    public BlockStatement getBody() {
        return body;
    }

    public List<Identifier> getArguments() {
        return arguments;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
