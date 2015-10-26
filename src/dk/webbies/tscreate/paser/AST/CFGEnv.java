package dk.webbies.tscreate.paser.AST;

/**
 * Created by hamid on 10/20/15.
 */
public class CFGEnv {
    public enum CFGEnvType {In, Out}

    private CFGNode appendNode; // can be a InNode (type: In) or outNode (type: Out)
    private CFGEnvType type;

    public CFGNode getAppendNode() { return appendNode; }
    public CFGEnvType getType() { return type; }

    public void setAppendNode(CFGNode appendNode) {
        if (type != CFGEnvType.In) throw new RuntimeException("Only In_CFGEnv can do that!");
        this.appendNode = appendNode;
    }

    private CFGEnv(CFGNode appendNode) {
        this.appendNode = appendNode;
    }

    public static final CFGEnv createInCfgEnv() {
        CFGNode appendNode = new CFGNop();
        CFGEnv ret = new CFGEnv(appendNode);
        ret.type = CFGEnvType.In;
        return ret;
    }
    public static final CFGEnv createOutCfgEnv(CFGNode predNode, CFGNode outNode) {
        CFGEnv ret = new CFGEnv(outNode);
        predNode.addSuccessor(outNode);
        ret.type = CFGEnvType.Out;
        return ret;
    }

}
