package dk.webbies.tscreate.paser;

import java.util.List;

/**
 * Created by erik1 on 01-09-2015.
 */
public class BlockStatement extends Statement{
    private final List<Statement> statements;

    public BlockStatement(int line, List<Statement> statements) {
        super(line);
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
