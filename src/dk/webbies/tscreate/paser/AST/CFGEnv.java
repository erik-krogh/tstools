package dk.webbies.tscreate.paser.AST;

/**
 * Created by hamid on 10/20/15.
 */
public class CFGEnv {
    public SSAEnv ssAEnv() {
        return ssaEnv;
    }

    public enum CFGEnvType {In, Out}

    private CFGNode appendNode; // can be an InNode (type: In) or outNode (type: Out)
    private CFGEnvType type;
    public SSAEnv ssaEnv;

    public CFGNode getAppendNode() { return appendNode; }
    public CFGEnvType getType() { return type; }
    public SSAEnv getCopyOfSSAEnv() { return ssaEnv.copy(); }
    public CFGEnv copy() {
        CFGEnv ret = new CFGEnv(this.appendNode);
        ret.type = this.type;
        ret.ssaEnv = this.ssaEnv.copy();
        //Helper.printDebug("src", ssaEnv.id2last.toString());
        //Helper.printDebug("copy", ret.ssaEnv.id2last.toString());
        return ret;
    }

    public void setAppendNode(CFGNode appendNode) {
        if (type != CFGEnvType.In) throw new RuntimeException("Only In_CFGEnv can do that!");
        this.appendNode = appendNode;
    }

    private CFGEnv(CFGNode appendNode) {
        this.appendNode = appendNode;
        this.ssaEnv = null;
    }
    public final void setSSAEnv(SSAEnv o) {
        this.ssaEnv = o;
    }
    public static final CFGEnv createInCfgEnv() {
        // at the moment only visit(FunctionExpr ...) is expected to call createInCfgEnv
        CFGNode appendNode = new CFGEntry();
        CFGEnv ret = new CFGEnv(appendNode);
        ret.type = CFGEnvType.In;
        ret.ssaEnv = SSAEnv.createEmptySSAEnv();
        return ret;
    }

    public static final CFGEnv createOutCfgEnv(CFGNode predNode, CFGNode outNode, SSAEnv ssaEnv) { //ssaEnv param
        return createOutCfgEnv(new CFGNode[] {predNode}, outNode, ssaEnv);
    }
    public static final CFGEnv createOutCfgEnv(CFGNode[] preds, CFGNode outNode, SSAEnv ssaEnv) {

        CFGEnv ret = new CFGEnv(outNode);
        ret.type = CFGEnvType.Out;
        for (CFGNode pred : preds) {
            pred.addSuccessor(outNode);
        }
        // SSA hook below!
        // we can safely update ssaEnv here (it's our own copy) for our own block passed via CFGEnv
        if (outNode instanceof CFGDef) {

            CFGDef cfgdef = (CFGDef) outNode;
            int subscript = ssaEnv.setNewSubscriptFor(cfgdef.getDefinition(), cfgdef);
            cfgdef.setSubscript(subscript);
        } else if (outNode instanceof CFGUse) {
            CFGUse cfguse = (CFGUse) outNode;
            for (Identifier u : cfguse.getUses()) {
                // record u -> lastSubscript(u) in outNode
                cfguse.setSubscript(u, ssaEnv.getLastSubscript((Identifier) u));
            }
        } else if (outNode instanceof CFGJoin) {


        }
        else  {
            throw new RuntimeException("unexpected outnode: " + outNode.getClass());
        }

        ret.setSSAEnv(ssaEnv);
        return ret;
    }

}
