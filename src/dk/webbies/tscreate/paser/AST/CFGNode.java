package dk.webbies.tscreate.paser.AST;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hamid on 10/22/15.
 */
public abstract class CFGNode {
    private AstNode astNode;
    private List<CFGNode> successors = new LinkedList<>();

    CFGNode() {
        astNode = null;
    }
    CFGNode(AstNode astNode) {
        this.astNode = astNode;
    }
    void addSuccessor(CFGNode succ) {
        successors.add(succ);
    }
    public Collection<CFGNode> getSuccessors() {
        return Collections.unmodifiableList(successors);
        //return successors;
    }
}
