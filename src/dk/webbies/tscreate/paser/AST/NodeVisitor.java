package dk.webbies.tscreate.paser.AST;

import dk.webbies.tscreate.paser.ExpressionVisitor;
import dk.webbies.tscreate.paser.StatementVisitor;

/**
 * Created by Erik Krogh Kristensen on 01-09-2015.
 */
public interface NodeVisitor<T> extends StatementVisitor<T>, ExpressionVisitor<T> {

}
