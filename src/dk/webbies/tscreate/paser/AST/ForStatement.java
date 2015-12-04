package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.CFGStatementVisitor;
import dk.webbies.tscreate.paser.StatementVisitor;

/**
 * Created by Erik Krogh Kristensen on 07-09-2015.
 */
public class ForStatement extends Statement {
    private final Statement initialize;
    private final Expression condition;
    private final Expression increment;
    private final Statement body;

    public ForStatement(SourceRange loc, Statement initialize, Expression condition, Expression increment, Statement body) {
        super(loc);
        this.initialize = initialize;
        this.condition = condition;
        this.increment = increment;
        this.body = body;
    }

    public Statement getInitialize() {
        return initialize;
    }

    public Expression getCondition() {
        return condition;
    }

    public Expression getIncrement() {
        return increment;
    }

    public Statement getBody() {
        return body;
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
