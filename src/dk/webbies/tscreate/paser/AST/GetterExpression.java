package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.paser.ExpressionVisitor;

import java.util.Collections;

/**
 * This can only occur as expressions in properties of objects.
 */
public class GetterExpression extends Expression {
    private final BlockStatement body;

    GetterExpression(SourceRange location, BlockStatement body) {
        super(location);
        this.body = body;
    }

    public BlockStatement getBody() {
        return body;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    FunctionExpression function = null;
    private static int getterCounter = 0;
    public FunctionExpression asFunction() {
        if (function == null) {
            Identifier identifier = new Identifier(this.location, ":getter" + getterCounter++);
            function = new FunctionExpression(this.location, identifier, body, Collections.EMPTY_LIST);
        }
        return function;
    }
}
