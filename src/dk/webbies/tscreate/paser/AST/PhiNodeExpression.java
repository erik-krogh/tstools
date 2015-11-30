package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by hamid on 10/20/15.
 */
public class PhiNodeExpression extends Expression {

    public static Map<Set<CFGDef>, PhiNodeExpression> phiNodes = new HashMap<>();


    // set of CFGDefs
    private Set<CFGDef> defNodes = new HashSet<>();


    private PhiNodeExpression(CFGDef[] defNodes) {
        super(null);
        for (CFGDef d : defNodes) this.defNodes.add(d);
    }

    public static PhiNodeExpression makePhiNodeExpression(CFGDef... defs) {
        Set<CFGDef> key = new HashSet<CFGDef>();
        for (CFGDef d : defs) key.add(d);

        PhiNodeExpression r = phiNodes.get(key);;
        if (r != null) return r;
        r = new PhiNodeExpression(defs);
        phiNodes.put(key, r);
        return r;
    }
    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        throw new RuntimeException();
    }
    @Override
    public <T> T accept(CFGExpressionVisitor<T> visitor, T aux) {
        return visitor.visit(this, aux);
    }
    public boolean equals(Object other) {
        if (!(other instanceof PhiNodeExpression)) return false;
        PhiNodeExpression otherPhi = (PhiNodeExpression) other;
        return otherPhi.defNodes.equals(this.defNodes);
    }
    public int hashCode() {
        int ret = 0;
        for (CFGDef d : defNodes) ret += d.hashCode();
        return ret;
    }

    public String toString() {
        String ret = "";
        for (CFGDef d : defNodes) {
            ret += ";" + d.getShortName();
        }
        return ret;
    }
}
