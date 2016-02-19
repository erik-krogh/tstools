package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.StatementVisitor;

/**
 * Created by erik1 on 01-09-2015.
 */
public abstract class Statement extends AstNode {

    Statement(SourceRange location) {
        super(location);
    }

    public abstract <T> T accept(StatementVisitor<T> visitor);
}
