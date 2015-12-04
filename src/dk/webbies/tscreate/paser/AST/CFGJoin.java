package dk.webbies.tscreate.paser.AST;

import dk.webbies.tscreate.util.Pair;

/**
 * Created by hamid on 11/18/15.
 */
public class CFGJoin extends CFGNode {
    // This is used _only_ during SSA computations (See the paper Single Pass generation of SSA for structured languages).
    //private Map<String, Integer> backupValues = new HashMap<>(); // id -> subscript
    //private SSAEnv backupValues;
    /*private Map<Identifier, CFGDef> defNodes; // every defNode has a PhiExpr (def = phi(...))

    public SSAEnv getBackupValues() { return backupValues; }
    public CFGJoin(SSAEnv backupValues) {
        this.backupValues = backupValues;
    }*/
    public String toString_() { return "JOIN: " + this.hashCode();}
    public String toString() {
        StringBuilder ret = new StringBuilder();
        if (ssaEnv == null) throw new RuntimeException();
        for (Pair<Integer, CFGDef> pair : ssaEnv.id2last.values()) {
            ret.append(pair.second.toString());
            ret.append("\n");
        }
        return ret.toString();
    }
    public CFGJoin(int a) {}
    // addDef / addPhi

    public SSAEnv ssaEnv = null;
}
