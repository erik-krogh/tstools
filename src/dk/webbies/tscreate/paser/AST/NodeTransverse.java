package dk.webbies.tscreate.paser.AST;

import dk.webbies.tscreate.paser.ExpressionTransverse;
import dk.webbies.tscreate.paser.ExpressionVisitor;
import dk.webbies.tscreate.paser.StatementTransverse;

/**
 * A default implementation of NodeVisitor, that visits everything, but returns null for everything.
 * Except for functions, those are handled separately by everything.
 */

public interface NodeTransverse<T> extends StatementTransverse<T>, ExpressionTransverse<T> {
    @Override
    public default ExpressionVisitor<T> getExpressionVisitor() {
        return this;
    }

    @Override
    public default T visit(FunctionExpression function) {
        function.getBody().accept(this);
        if (function.getName() != null) {
            function.getName().accept(this);
        }
        function.getArguments().forEach(arg -> arg.accept(this));
        return null;
    }
}
