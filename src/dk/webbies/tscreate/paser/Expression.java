package dk.webbies.tscreate.paser;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;

/**
 * Created by erik1 on 01-09-2015.
 */
public abstract class Expression extends Node {
    Expression(SourceRange location) {
        super(location);
    }
}
