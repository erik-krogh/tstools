package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.StatementVisitor;

/**
 * Created by Erik Krogh Kristensen on 07-09-2015.
 */
public class TryStatement extends Statement {
    private final Statement tryBlock;
    private final CatchStatement catchBlock;
    private final Statement finallyBlock;

    public TryStatement(SourceRange loc, Statement tryBlock, CatchStatement catchBlock, Statement finallyBlock) {
        super(loc);
        this.tryBlock = tryBlock;
        this.catchBlock = catchBlock;
        this.finallyBlock = finallyBlock;
    }

    public Statement getTryBlock() {
        return tryBlock;
    }

    public Statement getCatchBlock() {
        return catchBlock;
    }

    public Statement getFinallyBlock() {
        return finallyBlock;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
