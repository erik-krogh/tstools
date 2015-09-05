package dk.webbies.tscreate.paser;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;

/**
 * Created by erik1 on 01-09-2015.
 */
public abstract class Node {
    public final SourceRange location;
    Node(SourceRange location) {
        this.location = location;
    }

    public abstract <T> T accept(NodeVisitor<T> visitor);
}
