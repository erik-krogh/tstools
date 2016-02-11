package dk.webbies.tscreate.analysis.methods.old.analysis;

import dk.webbies.tscreate.analysis.HeapValueFactory;
import dk.webbies.tscreate.analysis.methods.unionRecursively.DumbPrimitiveFactory;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.paser.AST.Identifier;
import dk.webbies.tscreate.paser.AST.NodeTransverse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class ResolveEnvironmentVisitor implements NodeTransverse<Void> {
    private final Snap.Obj closure;
    private FunctionExpression function;
    private final UnionFindSolver solver;
    private final Map<OldTypeAnalysis.ProgramPoint, UnionNode> nodes;
    private Snap.Obj globalObject;
    private Map<String, Snap.Property> globalValues;
    private Map<String, Snap.Value> values;
    private final PrimitiveNode.Factory primitivesBuilder;
    private HeapValueFactory heapFactory;
    private Map<Snap.Obj, LibraryClass> libraryClasses;

    public ResolveEnvironmentVisitor(
            Snap.Obj closure,
            FunctionExpression function,
            UnionFindSolver solver,
            Map<OldTypeAnalysis.ProgramPoint, UnionNode> nodes,
            Map<String, Snap.Value> values,
            Map<String, Snap.Property> globalValues,
            Snap.Obj globalObject,
            HeapValueFactory heapFactory,
            Map<Snap.Obj, LibraryClass> libraryClasses) {
        this.closure = closure;
        this.function = function;
        this.solver = solver;
        this.nodes = nodes;
        this.globalObject = globalObject;
        this.heapFactory = heapFactory;
        this.libraryClasses = libraryClasses;
        this.globalValues = new HashMap<>(globalValues);
        this.values = new HashMap<>(values);
        this.primitivesBuilder = new DumbPrimitiveFactory(solver, globalObject);
        function.declarations.keySet().forEach(this.values::remove);
        function.declarations.keySet().forEach(this.globalValues::remove);

    }

    @Override
    public Void visit(FunctionExpression function) {
        if (function != this.function) {
            new ResolveEnvironmentVisitor(this.closure, function, this.solver, this.nodes, this.values, this.globalValues, globalObject, heapFactory, libraryClasses).visit(function);
            return null;
        } else {
            return NodeTransverse.super.visit(function);
        }
    }

    @Override
    public Void visit(Identifier identifier) {
        String name = identifier.getName();
        UnionNode idNode = UnionConstraintVisitor.getUnionNode(identifier, this.closure, this.nodes, solver);
        if (this.values.containsKey(name)) {
            solver.union(idNode, heapFactory.fromValue(this.values.get(name)));
        } else if (identifier.isGlobal) {
            if (name.equals("arguments")) {
                solver.union(idNode, new DynamicAccessNode(solver, primitivesBuilder.any(), primitivesBuilder.number()));
                ObjectNode obj = new ObjectNode(solver);
                obj.addField("length", primitivesBuilder.number());
                solver.union(idNode, obj);
            } else if (this.globalValues.containsKey(name)) {
                solver.union(idNode, heapFactory.fromProperty(this.globalValues.get(name)));
            } else {
                solver.union(idNode, primitivesBuilder.any());
            }
        }

        return NodeTransverse.super.visit(identifier);
    }
}
