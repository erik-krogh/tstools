package dk.webbies.tscreate.paser;

import dk.webbies.tscreate.paser.AST.*;

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

        h.Helper.printDebug("SIZE_N: " + CFGNode.cfgNodes.size());
        for (CFGNode n : CFGNode.cfgNodes) {
            h.Helper.printDebug(n.toString(), n.getSuccessors().size() + " ");
        }
        CFGNode rootNode = cfgbuilder.functionExpression2CFGNode.get(functionExpression);
        PrintWriter w = new PrintWriter(new File("out/graph.dot"));
        CFGBuilder.toDot(w, rootNode);
        w.close();
        return null;
    }

}
