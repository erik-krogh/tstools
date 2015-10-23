package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

import java.util.List;

/**
 * Created by Erik Krogh Kristensen on 06-09-2015.
 */
public class MethodCallExpression extends Expression {
    private final MemberExpression memberExpression;
    private final List<Expression> arguments;

    public MethodCallExpression(SourceRange loc, MemberExpression memberExpression, List<Expression> arguments) {
        super(loc);
        this.memberExpression = memberExpression;
        this.arguments = arguments;
    }

    public MemberExpression getMemberExpression() {
        return memberExpression;
    }

    public List<Expression> getArgs() {
        return arguments;
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
