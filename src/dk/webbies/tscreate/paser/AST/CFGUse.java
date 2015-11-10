package dk.webbies.tscreate.paser.AST;

import java.util.*;

/**
 * Created by hamid on 10/25/15.
 * represent uses, z = 3*y + q + y --> CFGUse(y, q)
 * We are unable to use List<Identifier> for uses list because PhiNode (which is not an identifier) can be a use
 */
public class CFGUse extends CFGNode {

    public static Set<CFGUse> useNodes = new HashSet<>();

    private Set<Expression> uses;
    public CFGUse(AstNode astNode) {

        super(astNode);
        uses = new HashSet<>(8);
        useNodes.add(this);
    }

    public Collection<Expression> getUses() {
        return Collections.unmodifiableCollection(uses);
    }

    public boolean addUse(Expression use) {
        return uses.add(use);
    }
}
