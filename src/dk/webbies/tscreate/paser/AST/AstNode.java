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
        SourcePosition start = this.location.start;
        SourcePosition end = this.location.end;
        String contents = start.source.contents;
        return contents.substring(start.offset, end.offset);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AstNode astNode = (AstNode) o;

        return location.equals(astNode.location);

    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }
}
