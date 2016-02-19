package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

/**
 * Created by erik1 on 01-09-2015.
 */
public class BooleanLiteral extends Expression {
    private final boolean b;

    public BooleanLiteral(SourceRange location, boolean b) {
        super(location);
        this.b = b;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
