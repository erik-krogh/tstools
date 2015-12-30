package dk.webbies.tscreate.paser;

import dk.webbies.tscreate.paser.AST.*;
import dk.webbies.tscreate.util.Helper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by hamid on 10/9/15.
 */
public class SSA {
    private static FunctionExpression toSSA() {
        for (CFGDef cfgdef : CFGDef.defNodes) {
            cfgdef.changeDefinitionName();

        }
        return null;
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
        cfgbuilder.toDot(w);
        w.close();


        toSSA();

        Helper.printDebug("DEFS_modified", "");
        for (CFGDef d : CFGDef.defNodes) {
            Helper.printDebug("ID", d.getDefinition().getName());
            Helper.printDebug("AST", Helper.getText(d.getAstNode()));
            AstNode ast = d.getAstNode();
            Helper.printDebug("ast calss", ast.getClass().toString());
            if (ast instanceof BinaryExpression) {
                Expression lhs = ((BinaryExpression) ast).getLhs();
                if (lhs instanceof Identifier) Helper.printDebug("new_name: ", ((Identifier) lhs).getName());
            } else if (ast instanceof VariableNode) {
                Expression lhs = ((VariableNode) ast).getlValue();
                if (lhs instanceof Identifier) Helper.printDebug("new_name: ", ((Identifier) lhs).getName());

            }
            Helper.printDebug("~", "~");

        }

        System.out.println( "joinNodes !!!");

        for (CFGJoin joinNode : CFGJoin.joinNodes) {
            System.out.println("J: " + joinNode.hashCode());
            for (CFGDef n : joinNode.getDefNodes()) {
                System.out.println(n.toString() + " --");
                System.out.println(CFGBuilder.flat(n).toString() + "\n");
            }
            System.out.println(" ** \n");
        }


        return null;
    }

}
