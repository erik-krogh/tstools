package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

import java.util.List;

/**
 * Created by Erik Krogh Kristensen on 07-09-2015.
 */
public class NewExpression extends Expression {
    private final Expression operand;
    private final List<Expression> arguments;

    public NewExpression(SourceRange loc, Expression operand, List<Expression> arguments) {
        super(loc);
        this.operand = operand;
        this.arguments = arguments;
    }

    public Expression getOperand() {
        return operand;
    }

    public List<Expression> getArgs() {
        return arguments;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
