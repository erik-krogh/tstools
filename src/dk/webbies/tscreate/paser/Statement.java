package dk.webbies.tscreate.paser;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;

/**
 * Created by erik1 on 01-09-2015.
 */
public abstract class Statement extends Node {

    Statement(SourceRange location) {
        super(location);
    }
}
