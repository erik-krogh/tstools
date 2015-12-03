package dk.webbies.tscreate.paser.AST;

import dk.webbies.tscreate.hashing.ExpressionHashingStrategy;
import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.strategy.HashingStrategy;

import java.util.*;

import static dk.webbies.tscreate.Util.ord;

/**
 * Created by hamid on 10/25/15.
 * represent uses, z = 3*y + q + y --> CFGUse(y, q)
 * We are unable to use List<Identifier> for uses list because PhiNode (which is not an identifier) can be a use
 */
public class CFGUse extends CFGNode {
    public static Set<CFGUse> useNodes = new HashSet<>();

    private TCustomHashMap<Expression, Integer> uses;
    public CFGUse(AstNode astNode) {

        super(astNode);
        uses = new TCustomHashMap<>(new ExpressionHashingStrategy());
        useNodes.add(this);
    }

    public String toString() {
        if (true) {
            String ret = "use ";
            for (Map.Entry<Expression, Integer> entry : uses.entrySet()) {
                Identifier expr = (Identifier) entry.getKey();
                ret += " " + expr.getName() + "[" + entry.getValue() + "]";
            }
            ret += " @ " + getAstNode();
            return ret;
        }
        return "Use(" + uses + ") @ " + getAstNode();
    }
    public Collection<Expression> getUses() {
        return Collections.unmodifiableCollection(uses.keySet());
    }

    public void addUse(Expression use) {
        assert (!uses.keySet().contains(use));
        uses.put(use, null);
    }
    public void setSubscript(Expression use, int subscript) {
        uses.put(use, subscript);
    }
}
