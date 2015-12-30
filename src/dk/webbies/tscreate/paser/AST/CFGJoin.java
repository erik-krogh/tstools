package dk.webbies.tscreate.paser.AST;

import dk.webbies.tscreate.util.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hamid on 11/18/15.
 */
public class CFGJoin extends CFGNode {
    public static List<CFGJoin> joinNodes = new LinkedList<>();

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
    public CFGJoin(int a) {
        joinNodes.add(this);
    }
    public Collection<CFGDef> getDefNodes() {
        List<CFGDef> ret = new LinkedList<>();
        for (Pair<Integer, CFGDef> pair : ssaEnv.id2last.values()) {
            ret.add(pair.second);
        }
        return ret;
    }
    public SSAEnv ssaEnv = null;
}
