package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.Util;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.analysis.unionFind.nodes.*;
import dk.webbies.tscreate.jsnapconvert.Snap;
import dk.webbies.tscreate.paser.*;
import dk.webbies.tscreate.paser.FunctionExpression;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.Util.*;

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

    UnionNode get(Node node) {
        if (node == null) {
            throw new NullPointerException("node cannot be null");
        }
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
            case PLUS: {
                solver.add(lhs);
                solver.add(rhs);
                return new AddNode(lhs, rhs);
            }
            case ASSIGN: // =
            case EQ: // ==
                solver.union(lhs, rhs);
                return lhs;
            case MINUS:
            case MULT:
            case DIV:
            case MOD:
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
        if (identifier.getDeclaration() == null) {
            throw new RuntimeException("Cannot have null declarations");
        }
        UnionNode idDec = get(identifier.getDeclaration());
        solver.union(id, idDec);
        return id;
    }

    @Override
    public UnionNode visit(BooleanLiteral booleanLiteral) {
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
            function.getArguments().forEach(arg -> solver.union(get(arg), new NonVoidNode()));
            for (int i = 0; i < functionNode.arguments.size(); i++) {
                solver.union(get(function.getArguments().get(i)), functionNode.arguments.get(i));
            }
            if (function.getName() != null) {
                solver.union(get(function.getName()), functionNode);
            }
            return null;
        } else {
            FunctionNode result = new FunctionNode(function);
            if (function.getName() != null) {
                solver.union(get(function.getName()), result);
                function.getName().accept(this);
            }
            new AstConstraintVisitor(closure, solver, nodes, result).visit(function.getBody());
            for (int i = 0; i < function.getArguments().size(); i++) {
                solver.union(get(function.getArguments().get(i)), result.arguments.get(i));
            }
            return result;
        }
    }

    @Override
    public UnionNode visit(NumberLiteral number) {
        return PrimitiveUnionNode.number();
    }

    @Override
    public UnionNode visit(ExpressionStatement expressionStatement) {
        expressionStatement.getExpression().accept(this);
        return null;
    }

    @Override
    public UnionNode visit(NullLiteral nullLiteral) {
        return PrimitiveUnionNode.nullType();
    }

    @Override
    public UnionNode visit(VariableNode variableNode) {
        UnionNode initNode = variableNode.getInit().accept(this);
        UnionNode identifierNode = variableNode.getlValue().accept(this);
        solver.union(initNode, identifierNode);
        return null;
    }

    @Override
    public UnionNode visit(CallExpression callExpression) {
        List<UnionNode> args = callExpression.getArgs().stream().map(arg -> arg.accept(this)).collect(Collectors.toList());
        UnionNode function = callExpression.getFunction().accept(this);
        EmptyUnionNode returnNode = new EmptyUnionNode();
        solver.runWhenChanged(function, new CallGraphResolver(function, args, returnNode));
        return returnNode;
    }

    @Override
    public UnionNode visit(IfStatement ifStatement) {
        ifStatement.getCondition().accept(this);
        ifStatement.getIfBranch().accept(this);
        ifStatement.getElseBranch().accept(this);
        return null;
    }

    private class CallGraphResolver implements Runnable {
        UnionNode function;
        private List<UnionNode> args;
        private EmptyUnionNode returnNode;
        Set<FunctionNode> seen = new HashSet<>();
        public CallGraphResolver(UnionNode function, List<UnionNode> args, EmptyUnionNode returnNode) {
            this.function = function;
            this.args = args;
            this.returnNode = returnNode;
            this.run();
        }

        @Override
        public void run() {
            List<FunctionNode> functionNodes = cast(FunctionNode.class, solver.getUnionClass(function).getNodes().stream().filter(node -> node instanceof FunctionNode).collect(Collectors.toList()));
            functionNodes.removeAll(seen);
            seen.addAll(functionNodes);
            for (FunctionNode functionNode : functionNodes) {
                for (int i = 0; i < functionNode.arguments.size() && i < this.args.size(); i++) {
                    UnionNode parameter = functionNode.arguments.get(i);
                    UnionNode argument = this.args.get(i);
                    solver.union(parameter, argument);
                }
                solver.union(functionNode.returnNode, this.returnNode);
            }

            // TODO: If not visited?
        }
    }
}
