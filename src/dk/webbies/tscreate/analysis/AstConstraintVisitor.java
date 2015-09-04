package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.analysis.unionFind.nodes.*;
import dk.webbies.tscreate.jsnapconvert.Snap;
import dk.webbies.tscreate.paser.*;
import dk.webbies.tscreate.paser.FunctionExpression;

import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class AstConstraintVisitor implements NodeVisitor<UnionNode> {
    private final Snap.Obj closure;
    private final UnionFindSolver solver;
    private final Map<TypeAnalysis.ProgramPoint, UnionNode> nodes;
    private final FunctionNode functionNode;

    public AstConstraintVisitor(Snap.Obj function, UnionFindSolver solver, Map<TypeAnalysis.ProgramPoint, UnionNode> nodes, FunctionNode functionNode) {
        this.closure = function;
        this.solver = solver;
        this.nodes = nodes;
        this.functionNode = functionNode;
    }

    public FunctionNode getFunctionNode() {
        return functionNode;
    }

    UnionNode get(Node node) {
        TypeAnalysis.ProgramPoint key = new TypeAnalysis.ProgramPoint(this.closure, node);
        if (nodes.containsKey(key)) {
            return nodes.get(key);
        } else {
            EmptyUnionNode result = new EmptyUnionNode();
            nodes.put(key, result);
            return result;
        }
    }

    @Override
    public UnionNode visit(BinaryExpression op) {
        UnionNode lhs = op.getLhs().accept(this);
        UnionNode rhs = op.getRhs().accept(this);
        switch (op.getOperation()) {
            case ADD: {
                return new AddNode(lhs, rhs);
            }
            case ASSIGN: // =
            case EQ: // ==
                solver.union(lhs, rhs);
                return lhs;
            case SUB:
            case MULT:
                solver.union(PrimitiveUnionNode.number(), lhs);
                solver.union(PrimitiveUnionNode.number(), rhs);
                return PrimitiveUnionNode.number();
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public UnionNode visit(BlockStatement blockStatement) {
        blockStatement.getStatements().forEach(statement -> statement.accept(this));
        return null;
    }

    @Override
    public UnionNode visit(Return aReturn) {
        UnionNode returnExp = get(aReturn.getExpression());
        solver.union(returnExp, functionNode.returnNode);
        solver.union(aReturn.getExpression().accept(this), returnExp);
        return null;
    }
    
    @Override
    public UnionNode visit(MemberExpression memberExpression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UnionNode visit(StringLiteral string) {
        return PrimitiveUnionNode.string();
    }

    @Override
    public UnionNode visit(Identifier identifier) {
        UnionNode id = get(identifier);
        UnionNode idDec = get(identifier.getDeclaration());
        solver.union(id, idDec);
        return id;
    }

    @Override
    public UnionNode visit(BooleanLiteral booleanLitteral) {
        return PrimitiveUnionNode.bool();
    }

    @Override
    public UnionNode visit(UndefinedLiteral undefined) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UnionNode visit(FunctionExpression function) {
        if (function == this.closure.function.astNode) {
            function.getBody().accept(this);
            function.getArguments().forEach(arg -> arg.accept(this));
            for (int i = 0; i < functionNode.arguments.size(); i++) {
                solver.union(get(function.getArguments().get(i)), functionNode.arguments.get(i));
            }
            return null;
        }
        // Make sure not to visit the same thing twice.
        throw new UnsupportedOperationException();
    }

    @Override
    public UnionNode visit(NumberLiteral number) {
        return PrimitiveUnionNode.number();
    }

    @Override
    public UnionNode visit(ExpressionStatement expressionStatement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UnionNode visit(NullLiteral nullLiteral) {
        return PrimitiveUnionNode.nullType();
    }

    @Override
    public UnionNode visit(VariableNode variableNode) {
        UnionNode initNode = variableNode.getInit().accept(this);
        UnionNode identifierNode = variableNode.getIdentifier().accept(this);
        solver.union(initNode, identifierNode);
        return null;
    }
}
