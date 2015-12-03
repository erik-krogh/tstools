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
    private int subscript; // SSA subscript

    //private PhiNodeExpression phiNodeExpression;

    public CFGDef(AstNode astNode, Identifier def) {
        super(astNode);
        this.definition = def;
        //phiNodeExpression = null;
        subscript = -1;
        defNodes.add(this);
    }

    public Identifier getDefinition() {
        return definition;
    }

    //public boolean isPhiDef() { return (phiNodeExpression != null); }
    //public PhiNodeExpression getPhiNodeExpression() {
        //return phiNodeExpression;
    //}
    /*public void setAsPhiDef(PhiNodeExpression phiNodeExpression) {

        this.phiNodeExpression = phiNodeExpression;
    }*/

    public String toString() {
        return "def(" + definition.getName() +" [" + subscript + "]) @ " + getAstNode().toString();
    }
    public String getShortName() {
        return definition.getName() + "[" + subscript +"]";
    }
    public void setSubscript(int subscript) {
        this.subscript = subscript;
    }
}
