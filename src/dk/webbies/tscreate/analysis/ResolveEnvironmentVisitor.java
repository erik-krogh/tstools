package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.analysis.unionFind.nodes.FunctionNode;
import dk.webbies.tscreate.analysis.unionFind.nodes.HeapValueNode;
import dk.webbies.tscreate.analysis.unionFind.nodes.UnionNode;
import dk.webbies.tscreate.jsnapconvert.Snap;
import dk.webbies.tscreate.paser.FunctionExpression;
import dk.webbies.tscreate.paser.Identifier;
import dk.webbies.tscreate.paser.NodeTransverse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class ResolveEnvironmentVisitor extends NodeTransverse {
    private final Snap.Obj function;
    private final UnionFindSolver solver;
    private final Map<TypeAnalysis.ProgramPoint, UnionNode> nodes;
    private final Map<Snap.Obj, FunctionNode> functionNodes;
    private Map<String, Snap.Value> values;

    public ResolveEnvironmentVisitor(Snap.Obj function, UnionFindSolver solver, Map<TypeAnalysis.ProgramPoint, UnionNode> nodes, Map<String, Snap.Value> values, Map<Snap.Obj, FunctionNode> functionNodes) {
        this.function = function;
        this.solver = solver;
        this.nodes = nodes;
        this.functionNodes = functionNodes;
        this.values = new HashMap<>(values);
        this.function.function.astNode.declarations.keySet().forEach(this.values::remove);

    }

    @Override
    public Void visit(FunctionExpression function) {
        if (function != this.function.function.astNode) {
            new ResolveEnvironmentVisitor(this.function, this.solver, this.nodes, this.values, this.functionNodes);
        }
        return super.visit(function);
    }

    @Override
    public Void visit(Identifier identifier) {
        String name = identifier.getName();
        if (this.values.containsKey(name)) {
            UnionNode idNode = UnionConstraintVisitor.getUnionNode(identifier, this.function, this.nodes);
            Snap.Value value = this.values.get(name);
            solver.union(idNode, HeapValueNode.fromValue(value, this.solver, this.functionNodes));
        }
        return super.visit(identifier);
    }
}
