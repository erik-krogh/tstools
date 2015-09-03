package dk.webbies.tscreate.paser;

/**
 * Created by erik1 on 01-09-2015.
 */
public abstract class Statement extends Node {
    public int line;
    Statement(int line) {
        this.line = line;
    }
}
