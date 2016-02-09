package dk.webbies.tscreate.analysis.methods.old.analysis;

import dk.au.cs.casa.typescript.types.Signature;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.NativeTypeFactory;
import dk.webbies.tscreate.analysis.methods.mixed.MixedConstraintVisitor;
import dk.webbies.tscreate.analysis.methods.old.analysis.unionFind.HeapValueNode;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.*;
import dk.webbies.tscreate.paser.ExpressionVisitor;
import dk.webbies.tscreate.paser.StatementTransverse;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.util.Util.cast;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class UnionConstraintVisitor implements ExpressionVisitor<UnionNode>, StatementTransverse<UnionNode> {
    private final Snap.Obj closure;
    private final UnionFindSolver solver;
    private final Map<OldTypeAnalysis.ProgramPoint, UnionNode> nodes;
    private final FunctionNode functionNode;
    private final Map<Snap.Obj, FunctionNode> functionNodes;
    private OldTypeAnalysis typeAnalysis;
    private final PrimitiveNode.Factory primitiveFactory;
    private HeapValueNode.Factory heapFactory;
    private NativeTypeFactory functionNodeFactory;
    private Set<Snap.Obj> hasAnalysed;

    public UnionConstraintVisitor(
            Snap.Obj function,
            UnionFindSolver solver,
            Map<OldTypeAnalysis.ProgramPoint, UnionNode> nodes,
            FunctionNode functionNode,
            Map<Snap.Obj, FunctionNode> functionNodes,
            HeapValueNode.Factory heapFactory,
            OldTypeAnalysis typeAnalysis,
            Set<Snap.Obj> hasAnalysed) {
        this.closure = function;
        this.solver = solver;
        this.heapFactory = heapFactory;
        this.nodes = nodes;
        this.functionNode = functionNode;
        this.functionNodes = functionNodes;
        this.typeAnalysis = typeAnalysis;
        this.hasAnalysed = hasAnalysed;
        this.primitiveFactory = heapFactory.getPrimitivesFactory();
        this.functionNodeFactory = new NativeTypeFactory(heapFactory.getPrimitivesFactory(), solver, typeAnalysis.getNativeClasses());
    }

    UnionNode get(AstNode node) {
        return getUnionNode(node, this.closure, this.nodes, solver);
    }

    @Override
    public ExpressionVisitor<UnionNode> getExpressionVisitor() {
        return this;
    }

    public static UnionNode getUnionNode(AstNode node, Snap.Obj closure, Map<OldTypeAnalysis.ProgramPoint, UnionNode> nodes, UnionFindSolver solver) {
        if (node == null) {
            throw new NullPointerException("node cannot be null");
        }
        OldTypeAnalysis.ProgramPoint key = new OldTypeAnalysis.ProgramPoint(closure, node);
        if (nodes.containsKey(key)) {
            return nodes.get(key);
        } else {
            EmptyNode result = new EmptyNode(solver);
            nodes.put(key, result);
            return result;
        }
    }

    @Override
    public UnionNode visit(BinaryExpression op) {
        UnionNode lhs = op.getLhs().accept(this);
        UnionNode rhs = op.getRhs().accept(this);
        UnionNode result;
        switch (op.getOperator()) {
            case PLUS: {
                result = new IncludeNode(solver, lhs, rhs);
                break;
            }

            case EQUAL: // =
            case PLUS_EQUAL: // +=
                solver.union(lhs, rhs);
                result = lhs;
                break;
            case NOT_EQUAL: // !=
            case EQUAL_EQUAL: // ==
            case NOT_EQUAL_EQUAL: // !==
            case EQUAL_EQUAL_EQUAL: // ===
                solver.union(lhs, rhs);
                result = primitiveFactory.bool();
                break;
            case AND: // &&
            case OR: // ||
                result = new IncludeNode(solver, lhs, rhs);
                break;
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
            case LEFT_SHIFT_EQUAL: // <<=
            case RIGHT_SHIFT_EQUAL: // >>=
            case UNSIGNED_RIGHT_SHIFT_EQUAL: // >>>=
                solver.union(primitiveFactory.number(), lhs);
                solver.union(primitiveFactory.number(), rhs);
                result = primitiveFactory.number();
                break;
            case INSTANCEOF: // instanceof
                result = primitiveFactory.bool();
                break;
            case IN: // in
                solver.union(lhs, primitiveFactory.string());
                solver.union(rhs, new ObjectNode(solver));
                result = primitiveFactory.bool();
                break;
            default:
                throw new UnsupportedOperationException("Don't yet handle the operator: " + op.getOperator());
        }

        return solver.union(get(op), result);
    }

    @Override
    public UnionNode visit(UnaryExpression unOp) {
        UnionNode exp = unOp.getExpression().accept(this);
        UnionNode result;
        switch (unOp.getOperator()) {
            case MINUS:
            case PLUS:
            case MINUS_MINUS:
            case PLUS_PLUS:
            case BITWISE_NOT:
                solver.union(primitiveFactory.number(), exp);
                result = primitiveFactory.number();
                break;
            case NOT:
                result = primitiveFactory.bool();
                break;
            case TYPEOF:
                result = primitiveFactory.string();
                break;
            case VOID:
                result = primitiveFactory.undefined();
                break;
            case DELETE:
                result = primitiveFactory.bool();
                break;
            default:
                throw new UnsupportedOperationException("Don't yet handle the operator: " + unOp.getOperator());
        }
        return solver.union(get(unOp), result);
    }

    @Override
    public UnionNode visit(ForInStatement forIn) {
        forIn.getInitializer().accept(new NodeTransverse<Void>() {
            @Override
            public Void visit(Identifier identifier) {
                solver.union(get(identifier), primitiveFactory.string());
                return null;
            }
        });
        solver.union(get(forIn.getCollection()), new ObjectNode(solver));
        return null;
    }

    @Override
    public UnionNode visit(ThisExpression thisExpression) {
        return solver.union(get(thisExpression), this.functionNode.thisNode);
    }

    @Override
    public UnionNode visit(ConditionalExpression condExp) {
        UnionNode cond = condExp.getCondition().accept(this);
        UnionNode left = condExp.getLeft().accept(this);
        UnionNode right = condExp.getRight().accept(this);
        solver.union(left, right);
        return solver.union(get(condExp), left);
    }

    @Override
    public UnionNode visit(CommaExpression commaExpression) {
        commaExpression.getExpressions().forEach(exp -> exp.accept(this));
        return solver.union(get(commaExpression), get(commaExpression.getLastExpression()));
    }

    @Override
    public UnionNode visit(Return aReturn) {
        UnionNode exp = aReturn.getExpression().accept(this);
        solver.union(exp, functionNode.returnNode, primitiveFactory.nonVoid());
        return null;
    }

    @Override
    public UnionNode visit(StringLiteral string) {
        return solver.union(get(string), primitiveFactory.string());
    }

    @Override
    public UnionNode visit(Identifier identifier) {
        if (identifier.getDeclaration() == null) {
            throw new RuntimeException("Cannot have null declarations");
        }
        return solver.union(get(identifier), get(identifier.getDeclaration()));
    }

    @Override
    public UnionNode visit(BooleanLiteral booleanLiteral) {
        return solver.union(get(booleanLiteral), primitiveFactory.bool());
    }

    @Override
    public UnionNode visit(UndefinedLiteral undefined) {
        return solver.union(get(undefined), primitiveFactory.undefined());
    }

    @Override
    public UnionNode visit(GetterExpression getter) {
        throw new RuntimeException();
    }

    @Override
    public UnionNode visit(SetterExpression setter) {
        throw new RuntimeException();
    }

    @Override
    public UnionNode visit(ArrayLiteral arrayLiteral) {
        UnionNode result = primitiveFactory.array();
        EmptyNode arrayType = new EmptyNode(solver);
        solver.union(result, new DynamicAccessNode(solver, arrayType, primitiveFactory.number()));

        for (Expression expression : arrayLiteral.getExpressions()) {
            UnionNode expressionNode = expression.accept(this);
            solver.union(arrayType, expressionNode);
        }

        return result;
    }

    @Override
    public UnionNode visit(FunctionExpression function) {
        if (closureMatch(function, this.closure)) {
            // TODO: If a prototype-method, union this-node with libraryClass this-node.
            function.getBody().accept(this);
            function.getArguments().forEach(arg -> solver.union(arg.accept(this), primitiveFactory.nonVoid()));

            if (this.closure.function.type.equals("user")) {
                for (int i = 0; i < functionNode.arguments.size(); i++) {
                    solver.union(get(function.getArguments().get(i)), functionNode.arguments.get(i));
                }
            } else {
                int boundArguments = this.closure.function.arguments.size() - 1;
                for (int i = 0; i < functionNode.arguments.size(); i++) {
                    solver.union(get(function.getArguments().get(i + boundArguments)), functionNode.arguments.get(i));
                }
                for (int i = 0; i < boundArguments; i++) {
                    solver.union(get(function.getArguments().get(i)), heapFactory.fromValue(this.closure.function.arguments.get(i + 1))); // Plus 1, because the first argment is the "this" node.
                }
            }
            if (function.getName() != null) {
                solver.union(get(function.getName()), functionNode);
            }
            solver.union(get(function), functionNode);
            solver.union(functionNode, heapFactory.fromValue(this.closure));
            return null;
        } else {
            FunctionNode result = FunctionNode.create(function, solver);
            if (function.getName() != null) {
                solver.union(get(function.getName()), result);
                function.getName().accept(this);
            }
            new UnionConstraintVisitor(this.closure, this.solver, this.nodes, result, this.functionNodes, heapFactory, typeAnalysis, hasAnalysed).visit(function.getBody());
            for (int i = 0; i < function.getArguments().size(); i++) {
                solver.union(get(function.getArguments().get(i)), result.arguments.get(i));
                solver.union(get(function.getArguments().get(i)), primitiveFactory.nonVoid());
            }
            return solver.union(get(function), result);
        }
    }

    private static boolean closureMatch(FunctionExpression function, Snap.Obj closure) {
        String type = closure.function.type;
        if (type.equals("user")) {
            return function == closure.function.astNode;
        }
        if (type.equals("bind")) {
            return function == closure.function.target.function.astNode;
        }
        throw new RuntimeException("Unknown type: " + type);
    }

    @Override
    public UnionNode visit(NumberLiteral number) {
        return solver.union(get(number), primitiveFactory.number());
    }

    @Override
    public UnionNode visit(NullLiteral nullLiteral) {
        return solver.union(get(nullLiteral), primitiveFactory.nonVoid());
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
        ObjectNode result = new ObjectNode(solver);
        for (ObjectLiteral.Property property : object.getProperties()) {
            String key = property.name;
            Expression value = property.expression;
            if (value instanceof GetterExpression) {
                GetterExpression getter = (GetterExpression) value;
                FunctionNode function = FunctionNode.create(0, solver);
                solver.union(getter.asFunction().accept(this), function);
                result.addField(key, function.returnNode);
            } else if (value instanceof SetterExpression) {
                SetterExpression setter = (SetterExpression) value;
                FunctionNode function = FunctionNode.create(1, solver);
                solver.union(setter.asFunction().accept(this), function);
                result.addField(key, function.arguments.get(0));
            } else {
                UnionNode valueNode = value.accept(this);
                solver.union(valueNode, primitiveFactory.nonVoid());
                result.addField(key, valueNode);
            }
        }

        return result;
    }

    @Override
    public UnionNode visit(DynamicAccessExpression memberLookupExpression) {
        memberLookupExpression.getLookupKey().accept(this);
        memberLookupExpression.getOperand().accept(this);
        solver.union(get(memberLookupExpression.getLookupKey()), primitiveFactory.stringOrNumber());
        solver.union(get(memberLookupExpression), new DynamicAccessNode(solver, get(memberLookupExpression), get(memberLookupExpression.getLookupKey())));
        return get(memberLookupExpression);
    }

    @Override
    public UnionNode visit(CallExpression callExpression) {
        List<UnionNode> args = callExpression.getArgs().stream().map(arg -> arg.accept(this)).collect(Collectors.toList());
        UnionNode function = callExpression.getFunction().accept(this);
        EmptyNode returnNode = new EmptyNode(solver);
        solver.runWhenChanged(function, new CallGraphResolver(this.functionNode.thisNode, function, args, returnNode, callExpression));
        return solver.union(get(callExpression), returnNode);
    }

    @Override
    public UnionNode visit(MethodCallExpression methodCall) {
        List<UnionNode> args = methodCall.getArgs().stream().map(arg -> arg.accept(this)).collect(Collectors.toList());
        UnionNode function = methodCall.getMemberExpression().accept(this);
        EmptyNode returnNode = new EmptyNode(solver);
        solver.runWhenChanged(function, new CallGraphResolver(get(methodCall.getMemberExpression().getExpression()), function, args, returnNode, methodCall));
        return solver.union(get(methodCall), returnNode);
    }

    @Override
    public UnionNode visit(NewExpression newExp) {
        List<UnionNode> args = newExp.getArgs().stream().map(arg -> arg.accept(this)).collect(Collectors.toList());
        UnionNode function = newExp.getOperand().accept(this);
        UnionNode thisNode = get(newExp);
        solver.runWhenChanged(function, new NewCallResolver(function, args, thisNode, newExp));
        return thisNode;
    }

    @Override
    public UnionNode visit(MemberExpression member) {
        UnionNode objectExp = member.getExpression().accept(this);
        ObjectNode object = new ObjectNode(solver);
        UnionNode result = get(member);
        object.addField(member.getProperty(), result);
        solver.union(object, objectExp);
        solver.union(primitiveFactory.nonVoid(), result);
        solver.runWhenChanged(objectExp, new MixedConstraintVisitor.MemberResolver(member, objectExp, result, solver, heapFactory));
        return result;
    }


    private class NewCallResolver implements Runnable {
        private final UnionNode function;
        private final List<UnionNode> args;
        private final UnionNode thisNode;
        private final CallGraphResolver callResolver;
        private final HashSet<Snap.Obj> seenHeap = new HashSet<>();
        private final Set<FunctionNode> seenFunctions = new HashSet<>();

        public NewCallResolver(UnionNode function, List<UnionNode> args, UnionNode thisNode, Expression callExpression) {
            this.function = function;
            this.args = args;
            this.thisNode = thisNode;
            this.callResolver = new CallGraphResolver(thisNode, function, args, new EmptyNode(solver), callExpression);
            this.callResolver.constructorCalls = true;
        }

        @Override
        public void run() {
            getFunctionNodes(function, seenHeap, true, args).stream().filter(node -> node.closure != null).forEach(node -> {
                if (seenFunctions.contains(node)) {
                    return;
                }
                seenFunctions.add(node);
                switch (node.closure.function.type) {
                    case "native":
                        Snap.Property prototypeProp = node.closure.getProperty("prototype");
                        if (prototypeProp != null) {
                            solver.union(this.thisNode, new HasPrototypeNode(solver, (Snap.Obj) prototypeProp.value));
                        }
                        solver.union(node.returnNode, thisNode);
                        break;
                    case "user":
                        @SuppressWarnings("RedundantCast")
                        LibraryClass clazz = typeAnalysis.libraryClasses.get((Snap.Obj) node.closure.getProperty("prototype").value);
                        if (clazz != null) {
//                            clazz.isUsedAsClass = true; // This is useless after changing to "eager type resolution".
                            solver.union(this.thisNode, clazz.getNewThisNode(solver));
                            solver.union(this.thisNode, new HasPrototypeNode(solver, clazz.prototype));
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException("Do now know functions of type " + node.closure.function.type + " here.");
                }

            });

            this.callResolver.run();
        }
    }

    private class CallGraphResolver implements Runnable {
        UnionNode thisNode;
        UnionNode function;
        List<UnionNode> args;
        EmptyNode returnNode;
        private final Expression callExpression;
        Set<FunctionNode> seen = new HashSet<>();
        boolean constructorCalls;
        private Set<Snap.Obj> seenHeap = new HashSet<>();

        public CallGraphResolver(UnionNode thisNode, UnionNode function, List<UnionNode> args, EmptyNode returnNode, Expression callExpression) {
            this.thisNode = thisNode;
            this.function = function;
            this.args = args;
            this.returnNode = returnNode;
            this.callExpression = callExpression;

            FunctionNode functionNode = FunctionNode.create(args.size(), solver);
            solver.union(function, functionNode);
            Util.zip(functionNode.arguments.stream(), args.stream()).forEach(pair -> solver.union(pair.first, pair.second, primitiveFactory.nonVoid()));

            solver.union(functionNode.returnNode, returnNode);
            solver.union(functionNode.thisNode, thisNode);
        }

        @Override
        public void run() {
            List<FunctionNode> functionNodes = getFunctionNodes(function, seenHeap, constructorCalls, args);
            functionNodes.removeAll(seen);
            seen.addAll(functionNodes);
            List<UnionNode> asUnionNodes = cast(UnionNode.class, functionNodes);
            solver.union(function, asUnionNodes);
            for (FunctionNode functionNode : functionNodes) {
                for (int i = 0; i < functionNode.arguments.size() && i < this.args.size(); i++) {
                    UnionNode parameter = functionNode.arguments.get(i);
                    UnionNode argument = this.args.get(i);
                    solver.union(parameter, argument);
                }
                solver.union(functionNode.returnNode, this.returnNode);
                solver.union(functionNode.thisNode, this.thisNode);
            }

            // Same as "separateFunctions".
            if (typeAnalysis.options.staticMethod == Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_RECURSIVELY_RESOLVE_CALLGRAPH) {
                for (FunctionNode node : functionNodes) {
                    if (node.closure == null) {
                        continue;
                    }
                    if (UnionConstraintVisitor.this.hasAnalysed.contains(node.closure)) {
                        continue;
                    }
                    UnionConstraintVisitor.this.hasAnalysed.add(node.closure);
                    if (node.closure != null && node.closure.function != null && (node.closure.function.type.equals("user") || node.closure.function.type.equals("bind"))) {
                        typeAnalysis.analyse(node.closure, UnionConstraintVisitor.this.functionNodes, solver, node, heapFactory, hasAnalysed);
                    }
                }
            }
        }
    }

    private List<FunctionNode> getFunctionNodes(UnionNode function, Set<Snap.Obj> seenHeap, boolean constructorCalls, List<UnionNode> args) {
        Collection<Snap.Obj> closures = MixedConstraintVisitor.getFunctionClosures(function, seenHeap);

        List<FunctionNode> result = new ArrayList<>();

        for (Snap.Obj closure : closures) {
            String type = closure.function.type;
            switch (type) {
                case "user":
                case "bind":
                    FunctionNode functionNode = UnionConstraintVisitor.this.functionNodes.get(closure);
                    if (functionNode == null) {
                        // Same as not equals to separateFunctions.
                        if (typeAnalysis.options.staticMethod != Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_RECURSIVELY_RESOLVE_CALLGRAPH) {
                            throw new RuntimeException("All closures should have a functionNode at this point");
                        }
                        functionNode = FunctionNode.create(closure, solver);
                        UnionConstraintVisitor.this.functionNodes.put(closure, functionNode);
                    }
                    result.add(functionNode);
                    break;
                case "native":
                    List<Signature> signatures;
                    if (constructorCalls) {
                        signatures = closure.function.constructorSignatures;
                    } else {
                        signatures = closure.function.callSignatures;
                    }
                    result.addAll(signatures.stream().map(sig -> functionNodeFactory.fromSignature(sig)).collect(Collectors.toList()));
                    break;
                case "unknown":
                    break;
                default:
                    throw new UnsupportedOperationException("Cannot yet handle functions of type: " + type);
            }
        }



        return result;
    }
}
