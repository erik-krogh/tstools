package dk.webbies.tscreate.analysis;

import dk.au.cs.casa.typescript.types.Signature;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.Util;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
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
    private final Map<Snap.Obj, FunctionNode> functionNodes;
    private final Map<Snap.Obj, LibraryClass> libraryClasses;
    private final Options options;
    private Snap.Obj globalObject;
    private Map<Type, String> typeNames;
    private TypeAnalysis typeAnalysis;
    private final PrimitiveUnionNode.Factory primitiveFactory;
    private HeapValueNode.Factory heapFactory;
    private FunctionNodeFactory functionNodeFactory;

    public UnionConstraintVisitor(
            Snap.Obj function,
            UnionFindSolver solver,
            Map<TypeAnalysis.ProgramPoint, UnionNode> nodes,
            FunctionNode functionNode,
            Map<Snap.Obj, FunctionNode> functionNodes,
            Map<Snap.Obj, LibraryClass> libraryClasses,
            Options options,
            Snap.Obj globalObject,
            HeapValueNode.Factory heapFactory,
            Map<Type, String> typeNames,
            TypeAnalysis typeAnalysis) {
        this.closure = function;
        this.solver = solver;
        this.heapFactory = heapFactory;
        this.nodes = nodes;
        this.functionNode = functionNode;
        this.functionNodes = functionNodes;
        this.libraryClasses = libraryClasses;
        this.options = options;
        this.globalObject = globalObject;
        this.typeNames = typeNames;
        this.typeAnalysis = typeAnalysis;
        this.primitiveFactory = new PrimitiveUnionNode.Factory(solver, globalObject, libraryClasses);
        this.functionNodeFactory = new FunctionNodeFactory(primitiveFactory, this.solver, typeNames);
    }

    UnionNode get(AstNode node) {
        return getUnionNode(node, this.closure, this.nodes);
    }

    @Override
    public ExpressionVisitor<UnionNode> getExpressionVisitor() {
        return this;
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
        UnionNode result;
        switch (op.getOperator()) {
            case PLUS: {
                solver.add(lhs);
                solver.add(rhs);
                result = new GreatestCommonOfUnionNode(lhs, rhs);
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
                if (options.unionShortCircuitLogic) {
                    solver.union(lhs, rhs);
                    result = lhs;
                } else {
                    result = new GreatestCommonOfUnionNode(lhs, rhs);
                }
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
                solver.union(rhs, new ObjectUnionNode());
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
        solver.union(get(forIn.getCollection()), new ObjectUnionNode());
        return null;
    }

    @Override
    public UnionNode visit(ThisExpression thisExpression) {
        return solver.union(get(thisExpression), this.functionNode.thisNode);
    }

    @Override
    public UnionNode visit(ConditionalExpression condExp) {
        UnionNode cond = condExp.getCondition().accept(this);
        solver.add(cond);
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
        solver.union(exp, functionNode.returnNode, new NonVoidNode());
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
    public UnionNode visit(FunctionExpression function) {
        if (closureMatch(function, this.closure)) {
            // TODO: If a prototype-method, union this-node with libraryClass this-node.
            function.getBody().accept(this);
            function.getArguments().forEach(arg -> solver.union(arg.accept(this), new NonVoidNode()));

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
            FunctionNode result = FunctionNode.create(function);
            if (function.getName() != null) {
                solver.union(get(function.getName()), result);
                function.getName().accept(this);
            }
            new UnionConstraintVisitor(this.closure, this.solver, this.nodes, result, this.functionNodes, libraryClasses, options, globalObject, heapFactory, typeNames, typeAnalysis).visit(function.getBody());
            for (int i = 0; i < function.getArguments().size(); i++) {
                solver.union(get(function.getArguments().get(i)), result.arguments.get(i));
                solver.union(get(function.getArguments().get(i)), new NonVoidNode());
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
        return solver.union(get(nullLiteral), new NonVoidNode());
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
        ObjectUnionNode result = new ObjectUnionNode();
        for (Map.Entry<String, Expression> entry : object.getProperties().entrySet()) {
            String key = entry.getKey();
            Expression value = entry.getValue();
            UnionNode valueNode = value.accept(this);
            result.addField(key, valueNode);
        }

        return solver.union(get(object), result);
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
        return solver.union(get(callExpression), returnNode);
    }

    @Override
    public UnionNode visit(MethodCallExpression methodCall) {
        List<UnionNode> args = methodCall.getArgs().stream().map(arg -> arg.accept(this)).collect(Collectors.toList());
        UnionNode function = methodCall.getMemberExpression().accept(this);
        solver.add(function);
        EmptyUnionNode returnNode = new EmptyUnionNode();
        solver.runWhenChanged(function, new CallGraphResolver(get(methodCall.getMemberExpression().getExpression()), function, args, returnNode));
        return solver.union(get(methodCall), returnNode);
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
        ObjectUnionNode object = new ObjectUnionNode();
        UnionNode result = get(member);
        object.addField(member.getProperty(), result);
        solver.union(object, objectExp);
        solver.union(new NonVoidNode(), result);
        solver.runWhenChanged(objectExp, new MemberResolver(member));
        return result;
    }

    private class MemberResolver implements Runnable {
        private MemberExpression member;
        private Set<Snap.Obj> seenPrototypes = new HashSet<>();
        private Set<ObjectUnionNode> seenObjects = new HashSet<>();
        private String name;

        public MemberResolver(MemberExpression member) {
            this.member = member;
            this.name = member.getProperty();
        }

        @Override
        public void run() {
            List<UnionNode> unionNodes = get(member.getExpression()).getUnionClass().getNodes();

            Set<ObjectUnionNode> objects = unionNodes.stream().filter(node -> node instanceof ObjectUnionNode).map(obj -> ((ObjectUnionNode) obj)).filter(obj -> !seenObjects.contains(obj)).collect(Collectors.toSet());
            seenObjects.addAll(objects);

            for (ObjectUnionNode object : objects) {
                Map<String, UnionNode> fields = object.getObjectFields();
                if (fields.containsKey(name)) {
                    solver.union(get(member), fields.get(name));
                }
            }

            Set<Snap.Obj> prototypes = unionNodes.stream().filter(node -> node instanceof HasPrototypeUnionNode).map(hasProto -> ((HasPrototypeUnionNode) hasProto).getPrototype()).filter(proto -> !seenPrototypes.contains(proto)).collect(Collectors.toSet());
            seenPrototypes.addAll(prototypes);
            for (Snap.Obj prototype : prototypes) {
                UnionNode propertyNode = lookupProperty(prototype, member.getProperty());
                solver.union(get(member), propertyNode);
            }
        }

        private UnionNode lookupProperty(Snap.Value value, String name) {
            if (value == null || !(value instanceof Snap.Obj)) {
                return new EmptyUnionNode();
            }
            Snap.Obj obj = (Snap.Obj) value;
            Snap.Property property = obj.getProperty(name);
            if (property != null) {
                return heapFactory.fromProperty(property);
            }

            return lookupProperty(obj.prototype, name);
        }
    }



    private class NewCallResolver implements Runnable {
        private final UnionNode function;
        private final List<UnionNode> args;
        private final UnionNode thisNode;
        private final CallGraphResolver callResolver;
        private final HashSet<HeapValueNode> seenHeap = new HashSet<>();
        private final Set<FunctionNode> seenFunctions = new HashSet<>();
        private final HasPrototypeUnionNode.Factory hasProtoFactory;

        public NewCallResolver(UnionNode function, List<UnionNode> args, UnionNode thisNode) {
            this.function = function;
            this.args = args;
            this.thisNode = thisNode;
            this.callResolver = new CallGraphResolver(thisNode, function, args, new EmptyUnionNode());
            this.callResolver.constructorCalls = true;
            this.hasProtoFactory = new HasPrototypeUnionNode.Factory(UnionConstraintVisitor.this.libraryClasses);
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
                            solver.union(this.thisNode, hasProtoFactory.create((Snap.Obj) prototypeProp.value));
                        }
                        solver.union(node.returnNode, thisNode);
                        break;
                    case "user":
                        @SuppressWarnings("RedundantCast")
                        LibraryClass clazz = libraryClasses.get((Snap.Obj) node.closure.getProperty("prototype").value);
                        if (clazz != null) {
//                            clazz.isUsedAsClass = true; // This is useless after changing to "eager type resolution".
                            solver.union(this.thisNode, clazz.getNewThisNode());
                            solver.union(this.thisNode, hasProtoFactory.create(clazz.prototype));

                            solver.union(clazz.getNewConstructorNode(), this.function);
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
        EmptyUnionNode returnNode;
        Set<FunctionNode> seen = new HashSet<>();
        boolean constructorCalls;
        private HashSet<HeapValueNode> seenHeap = new HashSet<>();

        public CallGraphResolver(UnionNode thisNode, UnionNode function, List<UnionNode> args, EmptyUnionNode returnNode) {
            this.thisNode = thisNode;
            this.function = function;
            this.args = args;
            this.returnNode = returnNode;

            FunctionNode functionNode = FunctionNode.create(args.size());
            solver.union(function, functionNode);
            Util.zip(functionNode.arguments.stream(), args.stream()).forEach(pair -> solver.union(pair.left, pair.right, new NonVoidNode()));

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

            if (options.separateFunctions) {
                for (FunctionNode node : functionNodes) {
                    if (node.hasAnalyzed) {
                        continue;
                    }
                    node.hasAnalyzed = true;
                    if (node.closure != null && node.closure.function != null && node.closure.function.astNode != null) {
                        new UnionConstraintVisitor(
                                node.closure,
                                UnionConstraintVisitor.this.solver,
                                UnionConstraintVisitor.this.nodes,
                                node,
                                UnionConstraintVisitor.this.functionNodes,
                                UnionConstraintVisitor.this.libraryClasses,
                                UnionConstraintVisitor.this.options,
                                UnionConstraintVisitor.this.globalObject,
                                UnionConstraintVisitor.this.heapFactory, typeNames, typeAnalysis).
                                visit(node.closure.function.astNode);
                    }
                }
            }
        }
    }

    private List<FunctionNode> getFunctionNodes(UnionNode function, Set<HeapValueNode> seenHeap, boolean constructorCalls, List<UnionNode> args) {
        List<UnionNode> nodes = function.getUnionClass().getNodes();
        List<FunctionNode> result = cast(FunctionNode.class, nodes.stream().filter(node -> node instanceof FunctionNode).collect(Collectors.toList()));

        List<HeapValueNode> heapValues = cast(HeapValueNode.class, nodes.stream().filter(node ->
                node instanceof HeapValueNode &&
                        ((HeapValueNode) node).value instanceof Snap.Obj &&
                        ((Snap.Obj) ((HeapValueNode) node).value).function != null).collect(Collectors.toList()));

        for (HeapValueNode heapValue : heapValues) {
            if (seenHeap.contains(heapValue)) {
                continue;
            }
            seenHeap.add(heapValue);
            Snap.Obj closure = (Snap.Obj) heapValue.value;
            String type = closure.function.type;
            switch (type) {
                case "user":
                case "bind":
                    FunctionNode functionNode = UnionConstraintVisitor.this.functionNodes.get(closure);
                    if (functionNode == null) {
                        if (!options.separateFunctions) {
                            throw new RuntimeException("All closures should have a functionNode at this point");
                        }
                        functionNode = FunctionNode.create(closure);
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
                    result.addAll(signatures.stream().map(sig -> functionNodeFactory.fromSignature(sig, closure, args)).collect(Collectors.toList()));
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
