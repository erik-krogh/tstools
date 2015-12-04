package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

/**
 * Created by erik1 on 01-09-2015.
 */
public class NumberLiteral extends Expression {
    private double number;

    public NumberLiteral(SourceRange location, double number) {
        super(location);
        this.number = number;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <T> T accept(CFGExpressionVisitor<T> visitor, T aux) {
        return visitor.visit(this, aux);
    }
}
