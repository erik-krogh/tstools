package dk.webbies.tscreate.paser.AST;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hamid on 10/22/15.
 */
public class CFGNode {
    public AstNode astNode;
    public List<CFGNode> successors = new LinkedList<>();

    CFGNode() {
        astNode = null;
    }
    CFGNode(AstNode astNode) {
        this.astNode = astNode;
    }
}
