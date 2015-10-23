package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.CFGStatementVisitor;
import dk.webbies.tscreate.paser.StatementVisitor;

/**
 * Created by erik1 on 01-09-2015.
 */
public class Return extends Statement {
    private final Expression expression;

    Return(SourceRange location, Expression expression) {
        super(location);
        this.expression = expression;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visit(this);
    }
    @Override
    public <T> T accept(CFGStatementVisitor<T> visitor, T aux) {
        return visitor.visit(this, aux);
    }


    public Expression getExpression() {
        return expression;
    }
}
