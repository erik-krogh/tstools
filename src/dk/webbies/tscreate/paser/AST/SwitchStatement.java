package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.StatementVisitor;

import java.util.List;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 07-09-2015.
 */
public class SwitchStatement extends Statement {
    private final Expression expression;
    private final List<Map.Entry<Expression, Statement>> cases;

    public SwitchStatement(SourceRange loc, Expression expression, List<Map.Entry<Expression, Statement>> cases) {
        super(loc);
        this.expression = expression;
        this.cases = cases;
    }

    public Expression getExpression() {
        return expression;
    }

    public List<Map.Entry<Expression, Statement>> getCases() {
        return cases;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
