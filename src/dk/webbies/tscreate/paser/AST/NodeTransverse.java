package dk.webbies.tscreate.paser.AST;

import dk.webbies.tscreate.paser.ExpressionTransverse;
import dk.webbies.tscreate.paser.ExpressionVisitor;
import dk.webbies.tscreate.paser.StatementTransverse;
import dk.webbies.tscreate.paser.StatementVisitor;

/**
 * A default implementation of NodeVisitor, that visits everything, but returns null for everything.
 */

public interface NodeTransverse<T> extends StatementTransverse<T>, ExpressionTransverse<T> {
    @Override
    public default ExpressionVisitor<T> getExpressionVisitor() {
        return this;
    }

    @Override
    public default StatementVisitor<T> getStatementVisitor() {
        return this;
    }
}
