package dk.webbies.tscreate.paser;

/**
 * Created by erik1 on 01-09-2015.
 */
public abstract class Node {
    public abstract <T> T accept(NodeVisitor<T> visitor);
}
