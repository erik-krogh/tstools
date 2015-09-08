package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

/**
 * Created by Erik Krogh Kristensen on 01-09-2015.
 */
public class Identifier extends Expression {
    private final String name;
    public Identifier declaration = null;

    public Identifier(SourceRange location, String name) {
        super(location);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Identifier getDeclaration() {
        return declaration;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}