package dk.webbies.tscreate.paser.AST;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hamid on 10/25/15.
 * A definition instruction:
 *  x = ... --> CFGDef(definiton = x)
 *
 */
public class CFGDef extends CFGNode {

    public static  List<CFGDef> defNodes = new LinkedList<>();

    private Identifier definition;
    public CFGDef(AstNode astNode, Identifier def) {
        super(astNode);
        this.definition = def;
        defNodes.add(this);
    }
    public Identifier getDefinition() {
        return definition;
    }
}
