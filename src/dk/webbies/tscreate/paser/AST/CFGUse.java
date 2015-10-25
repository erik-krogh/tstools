package dk.webbies.tscreate.paser.AST;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by hamid on 10/25/15.
 * represent uses, z = 3*y + q + y --> CFGUse(y, q)
 * We are unable to use List<Identifier> for uses list because PhiNode (which is not an identifier) can be a use
 */
public class CFGUse extends CFGNode {
    private List<Expression> uses;
    public CFGUse(AstNode astNode) {
        super(astNode);
    }

    public Collection<Expression> getUses() {
        return Collections.unmodifiableCollection(uses);
    }

    void addUse(Expression use) {
        uses.add(use);
    }
}
