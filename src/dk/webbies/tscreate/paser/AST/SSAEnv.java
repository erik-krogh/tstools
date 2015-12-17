package dk.webbies.tscreate.paser.AST;

import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.util.Helper;
import dk.webbies.tscreate.util.Pair;

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

        //Integer r = id2last.get(id.getName()).first;
        Pair<Integer, CFGDef> pair = id2last.get(id.getName());
        if (pair == null) throw new RuntimeException(id.getName());
        Integer r = pair.first;
        if (r==null) throw new RuntimeException("No previous definition for " + id.getName());
        return r;
    }
    public int setNewSubscriptFor(Identifier id, CFGDef cfgDef) {
        Pair<Integer, CFGDef> r = id2last.get(id.getName());
        if (r == null) {
            id2last.put(id.getName(), new Pair<>(0, cfgDef));
            return 0;
        }
        int subscript = r.first + 1;
        id2last.put(id.getName(), new Pair<>(subscript, cfgDef));
        return subscript;
    }

    public SSAEnv copy() {
        SSAEnv ret = new SSAEnv();
        ret.id2last.putAll(this.id2last);
        return ret;
    }

    public static SSAEnv MergeSSAEnvs_(SSAEnv... inEnvs) {
        Helper.printDebug("merge_method");
        final int nBranches = inEnvs.length;
        Map<String, Set<Pair<Integer, CFGDef>>> id2subscripts = new HashMap<>();
        for (SSAEnv env : inEnvs) {
           Helper.printDebug("in_env: ", env.id2last.toString());
           for (Map.Entry<String, Pair<Integer, CFGDef>> entry : env.id2last.entrySet()) {
               Set<Pair<Integer,CFGDef>> subscripts = id2subscripts.get(entry.getKey());
               if (subscripts == null) {
                   subscripts = new HashSet<>();
                   id2subscripts.put(entry.getKey(), subscripts);
               }
               subscripts.add(entry.getValue());
           }
        }
        //Helper.printDebug("phis ", id2subscripts.toString());

        SSAEnv ret = createEmptySSAEnv();
        for (Map.Entry<String, Set<Pair<Integer, CFGDef>>> entry : id2subscripts.entrySet()) {
            if (entry.getValue().size() == 1) {
                Pair<Integer, CFGDef> pair = entry.getValue().iterator().next();
                ret.id2last.put(entry.getKey(), pair);
            } else {
                PhiNodeExpression phiExpr = PhiNodeExpression.makePhiNodeExpression(extractCFGDefs(entry.getValue()));
                CFGDef defPhi = new CFGDef(phiExpr, new Identifier(null, entry.getKey()));
                int subscript = getMaximumSubscript(entry.getValue()) + 1;
                defPhi.setSubscript(subscript);
                ret.id2last.put(entry.getKey(), new Pair(subscript, defPhi));
            }
        }
        Helper.printDebug("-----");
        Helper.printDebug("merged_env: ", ret.id2last.entrySet().toString());
        Helper.printDebug("end_merge_method");
        return ret;
    }
    public static SSAEnv MergeSSAEnvs(SSAEnv branchEnv, SSAEnv... inEnvs) {
        Helper.printDebug("merge_method");
        final int nBranches = inEnvs.length;
        Map<String, Set<Pair<Integer, CFGDef>>> id2subscripts = new HashMap<>();
        for (SSAEnv env : inEnvs) {
            Helper.printDebug("in_env: ", env.id2last.toString());
            for (Map.Entry<String, Pair<Integer, CFGDef>> entry : env.id2last.entrySet()) {
                Set<Pair<Integer,CFGDef>> subscripts = id2subscripts.get(entry.getKey());
                if (subscripts == null) {
                    subscripts = new HashSet<>();
                    id2subscripts.put(entry.getKey(), subscripts);
                }
                subscripts.add(entry.getValue());
            }
        }
        Helper.printDebug("raw id2subscripts ", id2subscripts.toString());
        // Fix id2subscripts in case that branchEnv should be reachable (available) in the mergedEnvironment
        for (Map.Entry<String, Set<Pair<Integer, CFGDef>>> entry : id2subscripts.entrySet()) {
            Set<Pair<Integer, CFGDef>> subscripts = entry.getValue();
            if (subscripts.size() < nBranches) {
                Pair<Integer, CFGDef> branchSubscript = branchEnv.id2last.get(entry.getKey());
                if (branchSubscript != null) {
                    subscripts.add(branchSubscript);
                }
            }
        }
        Helper.printDebug("fixed id2subscripts ", id2subscripts.toString());

        SSAEnv ret = createEmptySSAEnv();
        for (Map.Entry<String, Set<Pair<Integer, CFGDef>>> entry : id2subscripts.entrySet()) {
            if (entry.getValue().size() == 1) {
                Pair<Integer, CFGDef> pair = entry.getValue().iterator().next();
                ret.id2last.put(entry.getKey(), pair);
            } else {
                PhiNodeExpression phiExpr = PhiNodeExpression.makePhiNodeExpression(extractCFGDefs(entry.getValue()));
                CFGDef defPhi = new CFGDef(phiExpr, new Identifier(null, entry.getKey()));
                int subscript = getMaximumSubscript(entry.getValue()) + 1;
                defPhi.setSubscript(subscript);
                ret.id2last.put(entry.getKey(), new Pair(subscript, defPhi));
            }
        }
        Helper.printDebug("-----");
        Helper.printDebug("merged_env: ", ret.id2last.entrySet().toString());
        Helper.printDebug("end_merge_method");
        return ret;
    }

    private static List<Pair<Integer, CFGDef>> removeDuplicates(List<Pair<Integer, CFGDef>> subscripts) {
        List<Pair<Integer,CFGDef>> ret = new LinkedList<>();
        //Map<Integer, >
        return ret;
    }
    private static CFGDef[] extractCFGDefs(Collection<Pair<Integer, CFGDef>> pairs) {
        CFGDef[] ret = new CFGDef[pairs.size()];
        int i = 0;
        for (Pair<Integer,CFGDef> pair : pairs) ret[i++]=pair.second;
        return ret;
    }
    private static int getMaximumSubscript(Collection<Pair<Integer, CFGDef>> pairs) {
        int max = 0;
        for (Pair<Integer,CFGDef> pair : pairs) {
            if (pair.first > max) max = pair.first;
        }
        return max;
    }
}
