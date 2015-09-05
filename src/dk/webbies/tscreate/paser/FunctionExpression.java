package dk.webbies.tscreate.paser;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Erik Krogh Kristensen on 01-09-2015.
 */
// TODO: Really have to look everywhere this is used. To see if the name is used correctly.
public class FunctionExpression extends Expression {
    Identifier name;
    final BlockStatement body;
    final List<Identifier> arguments;
    public HashMap<String, Identifier> declarations;

    public FunctionExpression(SourceRange location, Identifier name, BlockStatement body, List<Identifier> arguments) {
        super(location);
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

    public Identifier getName() {
        return name;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
