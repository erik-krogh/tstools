package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.CFGStatementVisitor;
import dk.webbies.tscreate.paser.StatementVisitor;

/**
 * Created by Erik Krogh Kristensen on 07-09-2015.
 */
public class ForInStatement extends Statement {
    private final Statement initializer;
    private final Expression collection;
    private final Statement body;

    public ForInStatement(SourceRange loc, Statement initializer, Expression collection, Statement body) {
        super(loc);
        this.initializer = initializer;
        this.collection = collection;
        this.body = body;
    }

    public Statement getInitializer() {
        return initializer;
    }

    public Expression getCollection() {
        return collection;
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
