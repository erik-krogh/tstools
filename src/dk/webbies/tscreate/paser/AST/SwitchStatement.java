package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.CFGStatementVisitor;
import dk.webbies.tscreate.paser.StatementVisitor;

import java.util.List;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 07-09-2015.
 */
public class SwitchStatement extends Statement {
    private final Expression expression;
    private final List<Map.Entry<Expression, Statement>> cases;
    private final BlockStatement defaultCase;

    public SwitchStatement(SourceRange loc, Expression expression, List<Map.Entry<Expression, Statement>> cases, BlockStatement defaultCase) {
        super(loc);
        this.expression = expression;
        this.cases = cases;
        this.defaultCase = defaultCase;
    }

    public Expression getExpression() {
        return expression;
    }

    public List<Map.Entry<Expression, Statement>> getCases() {
        return cases;
    }

    public BlockStatement getDefaultCase() {
        return defaultCase;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visit(this);
    }
    @Override
    public <T> T accept(CFGStatementVisitor<T> visitor, T aux) {
        return visitor.visit(this, aux);
    }

}
