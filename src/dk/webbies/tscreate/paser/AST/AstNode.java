package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;

/**
 * Created by erik1 on 01-09-2015.
 */
public abstract class AstNode {
    public final SourceRange location;
    AstNode(SourceRange location) {
        this.location = location;
    }
}
