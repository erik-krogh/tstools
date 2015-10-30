package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.StatementVisitor;

import java.util.List;

/**
 * Created by erik1 on 01-09-2015.
 */
public class LabeledStatement extends Statement{
    private final Statement statement;
    private final String name;

    public LabeledStatement(SourceRange location, Statement statement, String name) {
        super(location);
        this.statement = statement;
        this.name = name;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Statement getStatement() {
        return statement;
    }

    public String getName() {
        return name;
    }
}
