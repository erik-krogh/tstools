package dk.webbies.tscreate.paser.AST;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by hamid on 11/18/15.
 */
public class CFGJoin extends CFGNode {
    // This is used _only_ during SSA computations (See the paper Single Pass generation of SSA for structured languages).
    //private Map<String, Integer> backupValues = new HashMap<>(); // id -> subscript
    private SSAEnv backupValues;
    private Map<Identifier, CFGDef> defNodes; // every defNode has a PhiExpr (def = phi(...))

    public SSAEnv getBackupValues() { return backupValues; }
    public CFGJoin(SSAEnv backupValues) {
        this.backupValues = backupValues;
    }
    public String toString() { return "JOIN: " + this.hashCode();}

    // addDef / addPhi
}
