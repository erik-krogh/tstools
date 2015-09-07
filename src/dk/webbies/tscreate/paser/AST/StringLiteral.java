package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

/**
 * Created by erik1 on 01-09-2015.
 */
public class StringLiteral extends Expression {
    private String string;

    public StringLiteral(SourceRange location, String string) {
        super(location);
        this.string = string;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
