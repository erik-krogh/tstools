package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.CFGStatementVisitor;
import dk.webbies.tscreate.paser.StatementVisitor;

/**
 * Created by Erik Krogh Kristensen on 07-09-2015.
 */
public class BreakStatement extends Statement {
    public BreakStatement(SourceRange loc) {
        super(loc);
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
