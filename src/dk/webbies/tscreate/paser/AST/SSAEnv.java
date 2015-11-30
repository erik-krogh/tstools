package dk.webbies.tscreate.paser.AST;

import dk.webbies.tscreate.Pair;
import h.Helper;

import java.util.*;

/**
 * Created by hamid on 11/20/15.
 */
public class SSAEnv {
    // identifier_name  -> subscript where subscript indicates last value (definition) of the variable
    public Map<String, Pair<Integer, CFGDef>> id2last;

    private SSAEnv() {
        id2last = new HashMap<>();
    }

    public static SSAEnv createEmptySSAEnv() { return new SSAEnv(); }
    //public CFGJoin getJoinNode() { return joinNode; }
    public int getLastSubscript(Identifier id) {
        Integer r = id2last.get(id.getName()).getO1();
        if (r==null) throw new RuntimeException("No previous definition for " + id.getName());
        return r;
    }
    public int setNewSubscriptFor(Identifier id, CFGDef cfgDef) {
        Pair<Integer, CFGDef> r = id2last.get(id.getName());
        if (r == null) {
            id2last.put(id.getName(), new Pair<>(0, cfgDef));
            return 0;
        }
        int subscript = r.getO1() + 1;
        id2last.put(id.getName(), new Pair<>(subscript, cfgDef));
        return subscript;
    }

    public SSAEnv copy() {
        SSAEnv ret = new SSAEnv();
        ret.id2last.putAll(this.id2last);
        return ret;
    }

    public static SSAEnv MergeSSAEnvs(SSAEnv... inEnvs) {

        Map<String, Set<Pair<Integer, CFGDef>>> id2subscripts = new HashMap<>();
        for (SSAEnv env : inEnvs) {
           for (Map.Entry<String, Pair<Integer, CFGDef>> entry : env.id2last.entrySet()) {
               Set<Pair<Integer,CFGDef>> subscripts = id2subscripts.get(entry.getKey());
               if (subscripts == null) {
                   subscripts = new HashSet<>();
                   id2subscripts.put(entry.getKey(), subscripts);
               }
               subscripts.add(entry.getValue());
           }
        }
        Helper.printDebug("phis ", id2subscripts.toString());

        SSAEnv ret = createEmptySSAEnv();
        for (Map.Entry<String, Set<Pair<Integer, CFGDef>>> entry : id2subscripts.entrySet()) {
            PhiNodeExpression phiExpr = PhiNodeExpression.makePhiNodeExpression(extractCFGDefs(entry.getValue()));
            CFGDef defPhi = new CFGDef(phiExpr, new Identifier(null, entry.getKey()));
            int subscript = getMaximumSubscript(entry.getValue()) + 1;
            defPhi.setSubscript(subscript);
            ret.id2last.put(entry.getKey(), new Pair(subscript, defPhi));
        }
        h.Helper.printDebug("Merged", ret.id2last.entrySet().toString());
        return ret;
    }
    private static CFGDef[] extractCFGDefs(Collection<Pair<Integer, CFGDef>> pairs) {
        CFGDef[] ret = new CFGDef[pairs.size()];
        int i = 0;
        for (Pair<Integer,CFGDef> pair : pairs) ret[i++]=pair.getO2();
        return ret;
    }
    private static int getMaximumSubscript(Collection<Pair<Integer, CFGDef>> pairs) {
        int max = 0;
        for (Pair<Integer,CFGDef> pair : pairs) {
            if (pair.getO1() > max) max = pair.getO1();
        }
        return max;
    }
}
