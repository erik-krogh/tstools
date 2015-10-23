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
        return null;
    }

}
