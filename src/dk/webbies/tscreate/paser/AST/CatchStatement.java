package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.StatementVisitor;

/**
 * Created by Erik Krogh Kristensen on 07-09-2015.
 */
public class CatchStatement extends Statement {
    private final Identifier exception;
    private final Statement body;

    public CatchStatement(SourceRange loc, Identifier exception, Statement body) {
        super(loc);
        this.exception = exception;
        this.body = body;
    }

    public Identifier getException() {
        return exception;
    }

    public Statement getBody() {
        return body;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
