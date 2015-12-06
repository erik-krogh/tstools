package dk.webbies.tscreate.paser.AST;

import dk.webbies.tscreate.hashing.ExpressionHashingStrategy;
import gnu.trove.map.hash.TCustomHashMap;

import java.util.*;

/**
 * Created by hamid on 10/25/15.
 * represent uses, z = 3*y + q + y --> CFGUse(y, q)
 *
 */
public class CFGUse extends CFGNode {
    public static Set<CFGUse> useNodes = new HashSet<>();

    private TCustomHashMap<Identifier, Integer> uses;
    public CFGUse(AstNode astNode) {

        super(astNode);
        uses = new TCustomHashMap<>(new ExpressionHashingStrategy());
        useNodes.add(this);
    }

    public String toString() {
        //if (true) {
            String ret = "use ";
            for (Map.Entry<Identifier, Integer> entry : uses.entrySet()) {
                Identifier expr = (Identifier) entry.getKey();
                ret += " " + expr.getName() + "[" + entry.getValue() + "]";
            }
            ret += " @ " + getAstNode();
            return ret;
        //}
        //return "Use(" + uses + ") @ " + getAstNode();
    }
    public Collection<Identifier> getUses() {
        return Collections.unmodifiableCollection(uses.keySet());
    }

    public void addUse(Identifier use) {

        assert (!uses.keySet().contains(use));
        uses.put(use, null);
    }
    public void setSubscript(Identifier use, int subscript) {
        uses.put(use, subscript);
    }
}
