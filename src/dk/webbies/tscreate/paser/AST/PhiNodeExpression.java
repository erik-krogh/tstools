package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

/**
 * Created by hamid on 10/20/15.
 */
public class PhiNodeExpression extends Expression {
    PhiNodeExpression() {
        super(null);
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        throw new RuntimeException();
    }
    @Override
    public <T> T accept(CFGExpressionVisitor<T> visitor, T aux) {
        return visitor.visit(this, aux);
    }

}
