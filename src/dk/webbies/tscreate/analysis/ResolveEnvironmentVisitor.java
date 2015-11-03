package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.paser.AST.Identifier;
import dk.webbies.tscreate.paser.AST.NodeTransverse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class ResolveEnvironmentVisitor implements NodeTransverse<Void> {
    private final Snap.Obj closure;
    private FunctionExpression function;
    private final UnionFindSolver solver;
    private final Map<TypeAnalysis.ProgramPoint, UnionNode> nodes;
    private Snap.Obj globalObject;
    private Map<String, Snap.Property> globalValues;
    private Map<String, Snap.Property> values;
    private final PrimitiveUnionNode.Factory primitivesBuilder;
    private HeapValueFactory heapFactory;
    private Map<Snap.Obj, LibraryClass> libraryClasses;

    public ResolveEnvironmentVisitor(
            Snap.Obj closure,
            FunctionExpression function,
            UnionFindSolver solver,
            Map<TypeAnalysis.ProgramPoint, UnionNode> nodes,
            Map<String, Snap.Property> values,
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
        this.primitivesBuilder = new PrimitiveUnionNode.Factory(solver, globalObject);
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
            solver.union(idNode, heapFactory.fromProperty(this.values.get(name)));
        } else if (identifier.isGlobal) {
            if (name.equals("arguments")) {
                solver.union(idNode, new DynamicAccessUnionNode(primitivesBuilder.any(), primitivesBuilder.number(), solver));
                ObjectUnionNode obj = new ObjectUnionNode(solver);
                obj.addField("length", primitivesBuilder.number());
                solver.union(idNode, obj);
            } else if (this.globalValues.containsKey(name)) {
                List<UnionNode> nodes = heapFactory.fromValue(this.globalValues.get(name).value);
                if (nodes.isEmpty()) {
                    throw new RuntimeException("Cannot have an identifier be nothing");
                }
                solver.union(idNode, nodes);
            } else {
                solver.union(idNode, primitivesBuilder.any());
            }
        }

        return NodeTransverse.super.visit(identifier);
    }
}
