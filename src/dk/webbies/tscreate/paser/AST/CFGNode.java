package dk.webbies.tscreate.paser.AST;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hamid on 10/22/15.
 */
public abstract class CFGNode {

    public static  List<CFGNode> cfgNodes = new LinkedList<>();
    private AstNode astNode;
    private List<CFGNode> successors = new LinkedList<>();

    CFGNode() {
        cfgNodes.add(this);
        astNode = null;
    }
    CFGNode(AstNode astNode) {
        cfgNodes.add(this);
        this.astNode = astNode;
    }

    void addSuccessor(CFGNode succ) {
        successors.add(succ);
    }

    public AstNode getAstNode() {
        return astNode;
    }
    public Collection<CFGNode> getSuccessors() {
        return Collections.unmodifiableList(successors);
        //return successors;
    }
}
