package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

/**
 * Created by Erik Krogh Kristensen on 07-09-2015.
 */
public class ThisExpression extends Expression {
    public ThisExpression(SourceRange loc) {
        super(loc);
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
