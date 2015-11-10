package dk.webbies.tscreate.paser;

import dk.webbies.tscreate.paser.AST.*;

/**
 * Created by hamid on 10/9/15.
 */
public class SSA {
    public static FunctionExpression toSSA(FunctionExpression functionExpression) {
        return functionExpression;
    }
    public static FunctionExpression toSSA_(FunctionExpression functionExpression) {
        CFGBuilder cfgbuilder = new CFGBuilder();
        cfgbuilder.processMain(functionExpression);
        h.Helper.printDebug("DEFS", "");
        for (CFGDef d : CFGDef.defNodes) {
            h.Helper.printDebug("ID", d.getDefinition().getName());
            h.Helper.printDebug("AST", h.Helper.getText(d.getAstNode()));
            h.Helper.printDebug("~", "~");

        }
        h.Helper.printDebug("USES", "");
        for (CFGUse u : CFGUse.useNodes) {

            h.Helper.printDebug("AST", h.Helper.getText(u.getAstNode()));
            h.Helper.printDebug("UUUUUUUUUUUUU", u.getUses().toString());
            h.Helper.printDebug("~", "~");

        }
        return null;
    }

}
