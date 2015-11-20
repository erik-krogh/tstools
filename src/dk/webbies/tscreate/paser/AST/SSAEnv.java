package dk.webbies.tscreate.paser.AST;

import java.util.Map;

/**
 * Created by hamid on 11/20/15.
 */
public class SSAEnv {
    // variable  -> last value (definition) of the variable
    public Map<Identifier, Expression> lastValue;
    public CFGJoin joinNode;
    public SSAEnv(CFGJoin joinNode) {
        this.joinNode = joinNode;
    }
}
