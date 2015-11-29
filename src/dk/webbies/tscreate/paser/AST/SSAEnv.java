package dk.webbies.tscreate.paser.AST;

import h.Helper;

import java.util.*;

/**
 * Created by hamid on 11/20/15.
 */
public class SSAEnv {
    // identifier_name  -> subscript where subscript indicates last value (definition) of the variable
    public Map<String, Integer> id2last;

    private SSAEnv() {
        id2last = new HashMap<>();
        //this.joinNode = joinNode;
    }

    public static SSAEnv createEmptySSAEnv() { return new SSAEnv(); }
    //public CFGJoin getJoinNode() { return joinNode; }
    public int getLastSubscript(Identifier id) {
        Integer r = id2last.get(id.getName());
        if (r==null) throw new RuntimeException("No previous definition for " + id .getName());
        return r;
    }
    public int setNewSubscriptFor(Identifier id) {
        Integer r = id2last.get(id.getName());
        if (r == null) {
            id2last.put(id.getName(), 0);
            return 0;
        }
        r += 1;
        id2last.put(id.getName(), r);
        return r;
    }

    public SSAEnv copy() {
        SSAEnv ret = new SSAEnv();
        ret.id2last.putAll(this.id2last);
        return ret;
    }

    public static SSAEnv MergeSSAEnv(SSAEnv... inEnvs) {
        SSAEnv ret = createEmptySSAEnv();
        Map<String, Set<Integer>> id2subscripts = new HashMap<>();
        for (SSAEnv env : inEnvs) {
           for (Map.Entry<String, Integer> entry : env.id2last.entrySet()) {
               Set<Integer> subscripts = id2subscripts.get(entry.getKey());
               if (subscripts == null) {
                   subscripts = new HashSet<>();
                   id2subscripts.put(entry.getKey(), subscripts);
               }
               subscripts.add(entry.getValue());
           }
        }
        Helper.printDebug("phis ", id2subscripts.toString());

        return ret;
    }
}
