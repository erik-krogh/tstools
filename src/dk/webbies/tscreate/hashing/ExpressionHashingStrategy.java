package dk.webbies.tscreate.hashing;

import dk.webbies.tscreate.paser.AST.Expression;
import dk.webbies.tscreate.paser.AST.Identifier;
import gnu.trove.strategy.HashingStrategy;

import static dk.webbies.tscreate.util.Util.ord;

/**
 * Created by hamid on 11/11/15.
 */
// FIXME: Check if prettier way of doing this (overriding getHashCodeOf in a set?).
public class ExpressionHashingStrategy implements HashingStrategy<Expression> {

    @Override
    public int computeHashCode(Expression expr) {
        // TODO: handle phinode
        if (expr instanceof Identifier) {
            Identifier id = (Identifier) expr;
            return ord(id.getName());
        } // else if phinode
        throw new RuntimeException("unexpected use: " + expr.toString());
    }

    @Override
    public boolean equals(Expression o1, Expression o2) {
        if (o1 instanceof Identifier && o2 instanceof Identifier) {
            return ((Identifier) o1).getName().equals(((Identifier) o2).getName());
        }// else if phinode
        throw new RuntimeException("unexpected use: " + o1 + " or " + o2);
    }
}
