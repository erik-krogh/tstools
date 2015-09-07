package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.StatementVisitor;

/**
 * Created by erik1 on 05-09-2015.
 */
public class IfStatement extends Statement {
    private final Expression condition;
    private final Statement ifBranch;
    private final Statement elseBranch;

    public IfStatement(SourceRange loc, Expression condition, Statement ifBranch, Statement elseBranch) {
        super(loc);
        this.condition = condition;
        this.ifBranch = ifBranch;
        this.elseBranch = elseBranch;
    }

    public Expression getCondition() {
        return condition;
    }

    public Statement getIfBranch() {
        return ifBranch;
    }

    public Statement getElseBranch() {
        return elseBranch;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
