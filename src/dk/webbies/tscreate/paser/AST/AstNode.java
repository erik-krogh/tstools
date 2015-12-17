package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourcePosition;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;

/**
 * Created by erik1 on 01-09-2015.
 */
public abstract class AstNode {
    public final SourceRange location;
    AstNode(SourceRange location) {
        this.location = location;
    }
    public String toString() {
        AstNode astnode = this;
        StringBuilder ret = new StringBuilder();
        SourcePosition start = astnode.location.start;
        SourcePosition end = astnode.location.end;
        String contents = start.source.contents;

        for (int i = start.offset; i < end.offset; i++) {
            ret.append(contents.charAt(i));
        }
        return ret.toString();
    }

}
