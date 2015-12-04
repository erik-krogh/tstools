package dk.webbies.tscreate.paser;

import dk.webbies.tscreate.paser.AST.*;
import dk.webbies.tscreate.util.Helper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by hamid on 10/9/15.
 */
public class SSA {
    public static FunctionExpression toSSA(FunctionExpression functionExpression) {
        return functionExpression;
    }
    public static FunctionExpression toSSA_(FunctionExpression functionExpression) throws IOException {
        CFGBuilder cfgbuilder = new CFGBuilder();
        cfgbuilder.processMain(functionExpression);
        Helper.printDebug("DEFS", "");
        for (CFGDef d : CFGDef.defNodes) {
            Helper.printDebug("ID", d.getDefinition().getName());
            Helper.printDebug("AST", Helper.getText(d.getAstNode()));
            Helper.printDebug("~", "~");

        }
        Helper.printDebug("USES", "");
        for (CFGUse u : CFGUse.useNodes) {

            Helper.printDebug("AST", Helper.getText(u.getAstNode()));
            Helper.printDebug("UUUUUUUUUUUUU", u.getUses().toString());
            Helper.printDebug("~", "~");

        }

        Helper.printDebug("DEFS ");
        for (CFGNode n : CFGNode.cfgNodes) {
            Helper.printDebug(n.toString(), n.getSuccessors().size() + " ");
        }
        CFGNode rootNode = cfgbuilder.functionExpression2CFGNode.get(functionExpression);
        PrintWriter w = new PrintWriter(new File("out/graph.dot"));
        CFGBuilder.toDot(w, rootNode);
        w.close();
        return null;
    }

}
