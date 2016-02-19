package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

import java.util.Arrays;

/**
 * This can only occur as expressions in properties of objects.
 */
public class SetterExpression extends Expression {
    private final BlockStatement body;
    private final Identifier parameter;

    SetterExpression(SourceRange location, BlockStatement body, Identifier parameter) {
        super(location);
        this.body = body;
        this.parameter = parameter;
    }

    public BlockStatement getBody() {
        return body;
    }

    public Identifier getParameter() {
        return parameter;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    FunctionExpression function = null;
    private static int setterCounter = 0;
    public FunctionExpression asFunction() {
        if (function == null) {
            Identifier identifier = new Identifier(this.location, ":setter" + setterCounter++);
            function = new FunctionExpression(this.location, identifier, body, Arrays.asList(parameter));
        }
        return function;
    }
}
