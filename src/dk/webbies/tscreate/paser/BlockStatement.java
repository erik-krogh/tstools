package dk.webbies.tscreate.paser;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;

import java.util.List;

/**
 * Created by erik1 on 01-09-2015.
 */
public class BlockStatement extends Statement{
    private final List<Statement> statements;

    public BlockStatement(SourceRange location, List<Statement> statements) {
        super(location);
        this.statements = statements;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public List<Statement> getStatements() {
        return statements;
    }
}
