package dk.webbies.tscreate.paser.AST;

/**
 * Created by hamid on 10/25/15.
 * A definition instruction:
 *  x = ... --> CFGDef(definiton = x)
 *
 */
public class CFGDef extends CFGNode {
    private Identifier definition;
    public CFGDef(AstNode astNode, Identifier def) {
        super(astNode);
        this.definition = def;
    }
    public Identifier getDefinition() {
        return definition;
    }
}
