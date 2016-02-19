package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.StatementVisitor;

/**
 * Created by Erik Krogh Kristensen on 07-09-2015.
 */
public class ContinueStatement extends Statement {
    public ContinueStatement(SourceRange loc) {
        super(loc);
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
