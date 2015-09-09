package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.analysis.unionFind.nodes.*;
import dk.webbies.tscreate.jsnapconvert.Snap;
import dk.webbies.tscreate.jsnapconvert.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.*;
import dk.webbies.tscreate.paser.ExpressionVisitor;
import dk.webbies.tscreate.paser.StatementTransverse;

import java.util.*;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.Util.cast;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class UnionConstraintVisitor implements ExpressionVisitor<UnionNode>, StatementTransverse<UnionNode> {
    private final Snap.Obj closure;
    private final UnionFindSolver solver;
    private final Map<TypeAnalysis.ProgramPoint, UnionNode> nodes;
    private final FunctionNode functionNode;
    private Map<Snap.Obj, FunctionNode> functionNodes;
    private HashMap<Snap.Obj, LibraryClass> libraryClasses;

    public UnionConstraintVisitor(Snap.Obj function, UnionFindSolver solver, Map<TypeAnalysis.ProgramPoint, UnionNode> nodes, FunctionNode functionNode, Map<Snap.Obj, FunctionNode> functionNodes, HashMap<Snap.Obj, LibraryClass> libraryClasses) {
        this.closure = function;
        this.solver = solver;
        this.nodes = nodes;
        this.functionNode = functionNode;
        this.functionNodes = functionNodes;
        this.libraryClasses = libraryClasses;
    }

    UnionNode get(AstNode node) {
        return getUnionNode(node, this.closure, this.nodes);
    }

    public static UnionNode getUnionNode(AstNode node, Snap.Obj closure, Map<TypeAnalysis.ProgramPoint, UnionNode> nodes) {
        if (node == null) {
            throw new NullPointerException("node cannot be null");
        }
        TypeAnalysis.ProgramPoint key = new TypeAnalysis.ProgramPoint(closure, node);
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
        switch (op.getOperator()) {
            case PLUS: {
                solver.add(lhs);
                solver.add(rhs);
                return new AddNode(lhs, rhs);
            }
            case EQUAL: // =
            case NOT_EQUAL: // !=
            case EQUAL_EQUAL: // ==
            case NOT_EQUAL_EQUAL: // !==
            case EQUAL_EQUAL_EQUAL: // ===
            case PLUS_EQUAL: { // +=
                solver.union(lhs, rhs);
                return lhs;
            }
            case AND: // &&
            case OR: // ||
                return PrimitiveUnionNode.bool();
            case MINUS: // -
            case MULT: // *
            case DIV: // /
            case MOD: // %
            case MINUS_EQUAL: // -=
            case MULT_EQUAL: // *=
            case DIV_EQUAL: // /=
            case MOD_EQUAL: // %=
            case LESS_THAN: // <
            case LESS_THAN_EQUAL: // <=
            case GREATER_THAN: // >
            case GREATER_THAN_EQUAL: // >=
            case BITWISE_AND: // &
            case BITWISE_OR: // |
            case BITWISE_XOR: // ^
            case LEFT_SHIFT: // <<
            case RIGHT_SHIFT: // >>
            case UNSIGNED_RIGHT_SHIFT: // >>>
                solver.union(PrimitiveUnionNode.number(), lhs);
                solver.union(PrimitiveUnionNode.number(), rhs);
                return PrimitiveUnionNode.number();
            case INSTANCEOF: // instanceof
                return PrimitiveUnionNode.bool();
            case IN: // in
                solver.union(lhs, new IndexerExpUnionNode());
                solver.union(rhs, new UnionNodeObject());
                return PrimitiveUnionNode.bool();
            default:
                throw new UnsupportedOperationException("Dont yet handle the operator: " + op.getOperator());
        }
    }

    @Override
    public UnionNode visit(ForInStatement forIn) {
        forIn.getInitializer().accept(new NodeTransverse<Void>() {
            @Override
            public Void visit(Identifier identifier) {
                solver.union(get(identifier), PrimitiveUnionNode.string());
                return null;
            }
        });
        solver.union(get(forIn.getCollection()), new UnionNodeObject());
        return null;
    }

    @Override
    public UnionNode visit(UnaryExpression unOp) {
        UnionNode exp = unOp.getExpression().accept(this);
        switch (unOp.getOperator()) {
            case MINUS:
            case PLUS:
            case MINUS_MINUS:
            case PLUS_PLUS:
            case BITWISE_NOT:
                solver.union(PrimitiveUnionNode.number(), exp);
                return PrimitiveUnionNode.number();
            case NOT:
                return PrimitiveUnionNode.bool();
            case TYPEOF:
                return PrimitiveUnionNode.string();
            case VOID:
                return PrimitiveUnionNode.undefined();
            case DELETE:
                return PrimitiveUnionNode.bool();
            default:
                throw new UnsupportedOperationException("Dont yet handle the operator: " + unOp.getOperator());
        }
    }

    @Override
    public UnionNode visit(ThisExpression thisExpression) {
        return this.functionNode.thisNode;
    }

    @Override
    public UnionNode visit(ConditionalExpression condExp) {
        UnionNode cond = condExp.getCondition().accept(this);
        solver.add(cond);
        UnionNode left = condExp.getLeft().accept(this);
        UnionNode right = condExp.getRight().accept(this);
        solver.union(left, right);
        return left;
    }

    @Override
    public UnionNode visit(CommaExpression commaExpression) {
        commaExpression.getExpressions().forEach(exp -> exp.accept(this));
        return get(commaExpression.getLastExpression());
    }

    @Override
    public UnionNode visit(ArrayLiteral array) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExpressionVisitor<UnionNode> getExpressionVisitor() {
        return this;
    }

    @Override
    public UnionNode visit(Return aReturn) {
        solver.union(aReturn.getExpression().accept(this), functionNode.returnNode);
        return null;
    }

    @Override
    public UnionNode visit(SwitchStatement switchStatement) {
        solver.union(get(switchStatement.getExpression()), new IndexerExpUnionNode());
        StatementTransverse.super.visit(switchStatement);
        return null;
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
        } else {
            UnionNode idDec = get(identifier.getDeclaration());
            solver.union(id, idDec);
        }
        return id;
    }

    @Override
    public UnionNode visit(BooleanLiteral booleanLiteral) {
        return PrimitiveUnionNode.bool();
    }

    @Override
    public UnionNode visit(UndefinedLiteral undefined) {
        return PrimitiveUnionNode.undefined();
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
            new UnionConstraintVisitor(this.closure, this.solver, this.nodes, result, this.functionNodes, libraryClasses).visit(function.getBody());
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
    public UnionNode visit(NullLiteral nullLiteral) {
        return new NonVoidNode();
    }

    @Override
    public UnionNode visit(VariableNode variableNode) {
        UnionNode initNode = variableNode.getInit().accept(this);
        UnionNode identifierNode = variableNode.getlValue().accept(this);
        solver.union(initNode, identifierNode);
        return null;
    }

    @Override
    public UnionNode visit(ObjectLiteral object) {
        UnionNodeObject result = new UnionNodeObject();
        for (Map.Entry<String, Expression> entry : object.getProperties().entrySet()) {
            String key = entry.getKey();
            Expression value = entry.getValue();
            UnionNode valueNode = value.accept(this);
            result.addField(key, valueNode);
        }

        return result;
    }

    @Override
    public UnionNode visit(MemberLookupExpression memberLookupExpression) {
        memberLookupExpression.getLookupKey().accept(this);
        memberLookupExpression.getOperand().accept(this);
        solver.union(get(memberLookupExpression.getLookupKey()), new IndexerExpUnionNode());
        solver.union(get(memberLookupExpression), new IsIndexedUnionNode(get(memberLookupExpression), get(memberLookupExpression.getLookupKey())));
        return get(memberLookupExpression);
    }

    @Override
    public UnionNode visit(CallExpression callExpression) {
        List<UnionNode> args = callExpression.getArgs().stream().map(arg -> arg.accept(this)).collect(Collectors.toList());
        UnionNode function = callExpression.getFunction().accept(this);
        solver.add(function);
        EmptyUnionNode returnNode = new EmptyUnionNode();
        solver.runWhenChanged(function, new CallGraphResolver(this.functionNode.thisNode, function, args, returnNode));
        return returnNode;
    }

    @Override
    public UnionNode visit(MethodCallExpression methodCall) {
        List<UnionNode> args = methodCall.getArgs().stream().map(arg -> arg.accept(this)).collect(Collectors.toList());
        UnionNode function = methodCall.getMemberExpression().accept(this);
        solver.add(function);
        EmptyUnionNode returnNode = new EmptyUnionNode();
        solver.runWhenChanged(function, new CallGraphResolver(get(methodCall.getMemberExpression().getExpression()), function, args, returnNode));
        return returnNode;
    }

    @Override
    public UnionNode visit(NewExpression newExp) {
        List<UnionNode> args = newExp.getArgs().stream().map(arg -> arg.accept(this)).collect(Collectors.toList());
        UnionNode function = newExp.getOperand().accept(this);
        solver.add(function);
        UnionNode thisNode = get(newExp);
        solver.runWhenChanged(function, new NewCallResolver(function, args, thisNode));
        return thisNode;
    }

    @Override
    public UnionNode visit(MemberExpression member) {
        UnionNode objectExp = member.getExpression().accept(this);
        UnionNodeObject object = new UnionNodeObject();
        UnionNode result = get(member);
        object.addField(member.getProperty(), result);
        solver.union(object, objectExp);
        solver.union(new NonVoidNode(), result);
        solver.runWhenChanged(objectExp, new PrototypeResolver(member));
        return result;
    }

    private class PrototypeResolver implements Runnable {
        private MemberExpression member;
        private Set<HasPrototypeUnionNode> seen = new HashSet<>();

        public PrototypeResolver(MemberExpression member) {
            this.member = member;
        }

        @Override
        public void run() {
            List<UnionNode> unionNodes = solver.getUnionClass(get(member.getExpression())).getNodes();

            List<HasPrototypeUnionNode> prototypes = cast(HasPrototypeUnionNode.class, unionNodes.stream().filter(node -> node instanceof HasPrototypeUnionNode).collect(Collectors.toList()));
            prototypes.removeAll(seen);
            seen.addAll(prototypes);
            for (HasPrototypeUnionNode prototype : prototypes) {
                List<UnionNode> nodes = lookupProperty(prototype.getPrototype());
                solver.union(get(member), nodes);
            }
        }

        private List<UnionNode> lookupProperty(Snap.Value value) {
            if (value == null) {
                return Collections.EMPTY_LIST;
            }
            if (!(value instanceof Snap.Obj)) {
                return Collections.EMPTY_LIST;
            }
            Snap.Obj obj = (Snap.Obj) value;
            Snap.Property property = obj.getProperty(member.getProperty());
            if (property != null) {
                return HeapValueNode.fromValue(property.value, solver, functionNodes);
            }

            return lookupProperty(obj.prototype);
        }
    }



    private class NewCallResolver implements Runnable {
        private final UnionNode function;
        private final UnionNode thisNode;
        private final CallGraphResolver callResolver;
        private final Set<LibraryClass> seen = new HashSet<>();

        public NewCallResolver(UnionNode function, List<UnionNode> args, UnionNode thisNode) {
            this.function = function;
            this.thisNode = thisNode;
            this.callResolver = new CallGraphResolver(thisNode, function, args, new EmptyUnionNode());
        }

        @Override
        public void run() {
            getFunctionNodes(function).stream().filter(node -> node.closure != null).forEach(node -> {
                @SuppressWarnings("RedundantCast")
                LibraryClass clazz = libraryClasses.get((Snap.Obj)node.closure.getProperty("prototype").value);
                if (seen.contains(clazz)) {
                    return;
                } else {
                    seen.add(clazz);
                }
                clazz.isUsedAsClass = true;
                solver.union(this.thisNode, clazz.thisNode);
                solver.union(this.thisNode, new HasPrototypeUnionNode(clazz.prototype));
            });
            this.callResolver.run();
        }
    }

    private class CallGraphResolver implements Runnable {
        UnionNode thisNode;
        UnionNode function;
        List<UnionNode> args;
        EmptyUnionNode returnNode;
        Set<FunctionNode> seen = new HashSet<>();

        public CallGraphResolver(UnionNode thisNode, UnionNode function, List<UnionNode> args, EmptyUnionNode returnNode) {
            this.thisNode = thisNode;
            this.function = function;
            this.args = args;
            this.returnNode = returnNode;
        }

        @Override
        public void run() {
            List<FunctionNode> functionNodes = getFunctionNodes(function);
            functionNodes.removeAll(seen);
            seen.addAll(functionNodes);
            for (FunctionNode functionNode : functionNodes) {
                for (int i = 0; i < functionNode.arguments.size() && i < this.args.size(); i++) {
                    UnionNode parameter = functionNode.arguments.get(i);
                    UnionNode argument = this.args.get(i);
                    solver.union(parameter, argument);
                }
                solver.union(functionNode.returnNode, this.returnNode);
                solver.union(functionNode.thisNode, this.thisNode);
            }
        }
    }

    private List<FunctionNode> getFunctionNodes(UnionNode function) {
        List<UnionNode> nodes = solver.getUnionClass(function).getNodes();
        List<FunctionNode> result = cast(FunctionNode.class, nodes.stream().filter(node -> node instanceof FunctionNode).collect(Collectors.toList()));

        List<HeapValueNode> heapValues = cast(HeapValueNode.class, nodes.stream().filter(node ->
                node instanceof HeapValueNode &&
                        ((HeapValueNode) node).value instanceof Snap.Obj &&
                        ((Snap.Obj) ((HeapValueNode) node).value).function != null).collect(Collectors.toList()));

        for (HeapValueNode heapValue : heapValues) {
            Snap.Obj closure = (Snap.Obj) heapValue.value;
            String type = closure.function.type;
            if (type.equals("user")) {
                FunctionNode functionNode = UnionConstraintVisitor.this.functionNodes.get(closure);
                result.add(functionNode);
            } else {
                throw new UnsupportedOperationException("Cannot yet handle functions of type: " + type);
            }
        }

        return result;
    }
}
