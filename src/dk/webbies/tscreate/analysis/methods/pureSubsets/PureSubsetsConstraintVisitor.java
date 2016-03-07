package dk.webbies.tscreate.analysis.methods.pureSubsets;

import dk.webbies.tscreate.analysis.HeapValueFactory;
import dk.webbies.tscreate.analysis.NativeTypeFactory;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.analysis.methods.mixed.MixedConstraintVisitor;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.*;
import dk.webbies.tscreate.paser.ExpressionVisitor;
import dk.webbies.tscreate.paser.StatementTransverse;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.webbies.tscreate.analysis.ResolveEnvironmentVisitor.getIdentifier;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
@SuppressWarnings("Duplicates") // Largely a duplicate of MixedConstraintVisitor
public class PureSubsetsConstraintVisitor implements ExpressionVisitor<UnionNode>, StatementTransverse<UnionNode> {
    private final Snap.Obj closure;
    private final UnionFindSolver solver;
    private final Map<Identifier, UnionNode> identifierMap;
    private final FunctionNode functionNode;
    private final Map<Snap.Obj, FunctionNode> functionNodes;
    private TypeAnalysis typeAnalysis;
    private final PrimitiveNode.Factory primitiveFactory;
    private HeapValueFactory heapFactory;
    private NativeTypeFactory nativeTypeFactory;

    public PureSubsetsConstraintVisitor(
            Snap.Obj closure,
            UnionFindSolver solver,
            Map<Identifier, UnionNode> identifierMap,
            FunctionNode functionNode,
            Map<Snap.Obj, FunctionNode> functionNodes,
            HeapValueFactory heapFactory,
            TypeAnalysis typeAnalysis,
            NativeTypeFactory nativeTypeFactory) {
        this.closure = closure;
        this.solver = solver;
        this.heapFactory = heapFactory;
        this.identifierMap = identifierMap;
        this.functionNode = functionNode;
        this.functionNodes = functionNodes;
        this.typeAnalysis = typeAnalysis;
        this.nativeTypeFactory = nativeTypeFactory;
        this.primitiveFactory = heapFactory.getPrimitivesFactory();
    }

    @Override
    public ExpressionVisitor<UnionNode> getExpressionVisitor() {
        return this;
    }

    @Override
    public UnionNode visit(BinaryExpression op) {
        UnionNode lhs = op.getLhs().accept(this);
        UnionNode rhs = op.getRhs().accept(this);

        solver.union(lhs, primitiveFactory.nonVoid());
        solver.union(rhs, primitiveFactory.nonVoid());

        switch (op.getOperator()) {
            case PLUS: {
                return primitiveFactory.stringOrNumber();
            }

            case EQUAL: // = // assignment
                solver.union(lhs, new IncludeNode(solver, rhs));
                if (typeAnalysis.getOptions().unifyShortCurcuitOrsAtAssignments) {
                    if (op.getRhs() instanceof BinaryExpression && ((BinaryExpression) op.getRhs()).getOperator() == Operator.OR) {
                        BinaryExpression orExp = (BinaryExpression) op.getRhs();
                        IncludeNode rhsInclude = (IncludeNode) rhs; // I here assume that the result returns an IncludeNode
                        solver.union(rhsInclude.getNodes());
                    }
                }
                return lhs;
            case PLUS_EQUAL: // +=
                solver.union(lhs, primitiveFactory.stringOrNumber());
                return lhs;
            case NOT_EQUAL: // !=
            case EQUAL_EQUAL: // ==
            case NOT_EQUAL_EQUAL: // !==
            case EQUAL_EQUAL_EQUAL: // ===
                return primitiveFactory.bool();
            case AND: // &&
            case OR: // ||
                return new IncludeNode(solver, lhs, rhs);
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
            case BITWISE_OR_EQUAL: // |=
            case BITWISE_AND_EQUAL: // &=
            case BITWISE_XOR_EQUAL: // ^=
                return primitiveFactory.number();
            case INSTANCEOF: // instanceof
                return primitiveFactory.bool();
            case IN: // in
                return primitiveFactory.bool();
            default:
                throw new UnsupportedOperationException("Don't yet handle the operator: " + op.getOperator());
        }
    }

    @Override
    public UnionNode visit(UnaryExpression unOp) {
        UnionNode exp = unOp.getExpression().accept(this);
        solver.union(exp, primitiveFactory.nonVoid());

        UnionNode result;
        switch (unOp.getOperator()) {
            case PLUS:
                result = primitiveFactory.number();
                break;
            case MINUS:
            case MINUS_MINUS:
            case PLUS_PLUS:
            case BITWISE_NOT:
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
        return result;
    }

    @Override
    public UnionNode visit(ForInStatement forIn) {
        forIn.getInitializer().accept(new NodeTransverse<Void>() {
            @Override
            public Void visit(Identifier identifier) {
                solver.union(identifier.accept(PureSubsetsConstraintVisitor.this), primitiveFactory.string());
                return null;
            }
        });
        return null;
    }

    @Override
    public UnionNode visit(ThisExpression thisExpression) {
        if (typeAnalysis.getOptions().classOptions.onlyUseThisWithFieldAccesses) {
            return new EmptyNode(solver);
        } else {
            return new IncludeNode(solver, this.functionNode.thisNode);
        }
    }

    @Override
    public UnionNode visit(ConditionalExpression condExp) {
        condExp.getCondition().accept(this);

        UnionNode left = condExp.getLeft().accept(this);
        UnionNode right = condExp.getRight().accept(this);
        return new IncludeNode(solver, left, right);
    }

    @Override
    public UnionNode visit(CommaExpression commaExpression) {
        // Recursively visits every one of them, returns the result of the last.
        return commaExpression.getExpressions().stream().map(exp -> exp.accept(this)).reduce((a, b) -> b).get();
    }

    @Override
    public UnionNode visit(Return aReturn) {
        if (aReturn.getExpression() instanceof UnaryExpression && ((UnaryExpression) aReturn.getExpression()).getOperator() == Operator.VOID) {
            // This is a return;
            return new EmptyNode(solver);
        }
        UnionNode exp = aReturn.getExpression().accept(this);
        solver.union(new IncludeNode(solver, exp), functionNode.returnNode, primitiveFactory.nonVoid());
        return null;
    }

    @Override
    public UnionNode visit(StringLiteral string) {
        return primitiveFactory.string();
    }

    @Override
    public UnionNode visit(Identifier identifier) {
        if (identifier.getDeclaration() == null) {
            throw new RuntimeException("Cannot have null declarations");
        }
        if (!typeAnalysis.getOptions().unionHeapIdentifiers) {
            return getIdentifier(identifier, solver, identifierMap);
        } else {
            return solver.union(getIdentifier(identifier, solver, identifierMap), getIdentifier(identifier.getDeclaration(), solver, identifierMap));
        }
    }

    @Override
    public UnionNode visit(BooleanLiteral booleanLiteral) {
        return primitiveFactory.bool();
    }

    @Override
    public UnionNode visit(UndefinedLiteral undefined) {
        return primitiveFactory.undefined();
    }

    @Override
    public UnionNode visit(GetterExpression getter) {
        throw new UnsupportedOperationException("Should be handled by ObjectLiteral");
    }

    @Override
    public UnionNode visit(SetterExpression setter) {
        throw new UnsupportedOperationException("Should be handled by ObjectLiteral");
    }

    @Override
    public UnionNode visit(ArrayLiteral arrayLiteral) {
        UnionNode result = primitiveFactory.array();
        EmptyNode arrayType = new EmptyNode(solver);
        solver.union(result, new DynamicAccessNode(solver, arrayType, primitiveFactory.number()));

        for (Expression expression : arrayLiteral.getExpressions()) {
            UnionNode expressionNode = expression.accept(this);
            solver.union(arrayType, new IncludeNode(solver, expressionNode));
        }

        return result;
    }

    @Override
    public UnionNode visit(FunctionExpression function) {
        if (closureMatch(function, this.closure)) {
            // It is the function we are currently analyzing, special treatment.
            function.getBody().accept(this);
            List<UnionNode> arguments = function.getArguments().stream().map(arg -> solver.union(arg.accept(this), primitiveFactory.nonVoid())).collect(Collectors.toList());

            if (this.closure.function.type.equals("user")) {
                for (int i = 0; i < functionNode.arguments.size(); i++) {
                    solver.union(arguments.get(i), functionNode.arguments.get(i));
                }
            } else {
                int boundArguments = this.closure.function.arguments.size() - 1;
                for (int i = 0; i < functionNode.arguments.size(); i++) {
                    solver.union(arguments.get(i + boundArguments), functionNode.arguments.get(i));
                }
                for (int i = 0; i < boundArguments; i++) {
                    solver.union(arguments.get(i), heapFactory.fromValue(this.closure.function.arguments.get(i + 1))); // Plus 1, because the first argument is the "this" node.
                }
            }
            return null;
        } else {
            // It is some nested function
            FunctionNode result = FunctionNode.create(function, solver);
            if (function.getName() != null) {
                solver.union(function.getName().accept(this), result);
                function.getName().accept(this);
            }
            new PureSubsetsConstraintVisitor(this.closure, this.solver, this.identifierMap, result, this.functionNodes, heapFactory, typeAnalysis, this.nativeTypeFactory).visit(function.getBody());
            solver.union(result, primitiveFactory.function());
            return result;
        }
    }

    private static boolean closureMatch(FunctionExpression function, Snap.Obj closure) {
        String type = closure.function.type;
        if (type.equals("user")) {
            return function == closure.function.astNode;
        } else if (type.equals("bind")) {
            return function == closure.function.target.function.astNode;
        }
        throw new RuntimeException("Unknown type: " + type);
    }

    @Override
    public UnionNode visit(NumberLiteral number) {
        return primitiveFactory.number();
    }

    @Override
    public UnionNode visit(NullLiteral nullLiteral) {
        return primitiveFactory.nonVoid();
    }

    @Override
    public UnionNode visit(VariableNode variableNode) {
        UnionNode initNode = variableNode.getInit().accept(this);
        UnionNode identifierNode = variableNode.getlValue().accept(this);
        solver.union(identifierNode, new IncludeNode(solver, initNode));
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
                result.addField(key, new IncludeNode(solver, function.returnNode));
            } else if (value instanceof SetterExpression) {
                SetterExpression setter = (SetterExpression) value;
                FunctionNode function = FunctionNode.create(1, solver);
                solver.union(setter.asFunction().accept(this), function);
                result.addField(key, new IncludeNode(solver, function.arguments.get(0)));
            } else {
                UnionNode valueNode = value.accept(this);
                result.addField(key, new IncludeNode(solver, valueNode, primitiveFactory.nonVoid()));
            }
        }

        return result;
    }

    @Override
    public UnionNode visit(DynamicAccessExpression dynamicAccessExpression) {
        dynamicAccessExpression.getLookupKey().accept(this);
        UnionNode operand = dynamicAccessExpression.getOperand().accept(this);
        UnionNode returnType = new EmptyNode(solver);

        solver.runWhenChanged(operand, new IncludesWithFieldsResolver(operand, DynamicAccessNode.LOOKUP_EXP_KEY, DynamicAccessNode.RETURN_TYPE_KEY));
        return returnType;
    }

    @Override
    public UnionNode visit(CallExpression callExpression) {
        List<UnionNode> args = callExpression.getArgs().stream().map(arg -> arg.accept(this)).collect(Collectors.toList());
        UnionNode function = callExpression.getFunction().accept(this);
        EmptyNode returnNode = new EmptyNode(solver);
        solver.runWhenChanged(function, new CallGraphResolver(this.functionNode.thisNode, function, args, returnNode, callExpression));
        return returnNode;
    }

    @Override
    public UnionNode visit(MemberExpression member) {
        return new MemberExpressionVisitor(member).invoke().getResult();
    }

    private class MemberExpressionVisitor {
        private MemberExpression member;
        private UnionNode objectExp;
        private UnionNode result;

        public MemberExpressionVisitor(MemberExpression member) {
            this.member = member;
        }

        public UnionNode getObjectExp() {
            return objectExp;
        }

        public UnionNode getResult() {
            return result;
        }

        public MemberExpressionVisitor invoke() {
            objectExp = member.getExpression().accept(PureSubsetsConstraintVisitor.this);
            ObjectNode object = new ObjectNode(solver);
            result = new EmptyNode(solver);
            object.addField(member.getProperty(), result);
            solver.union(object, objectExp);
            solver.runWhenChanged(object, new MixedConstraintVisitor.MemberResolver(member, objectExp, result, solver, heapFactory));
            solver.runWhenChanged(object, new IncludesWithFieldsResolver(object, ObjectNode.FIELD_PREFIX + member.getProperty()));

            if (typeAnalysis.getOptions().classOptions.onlyUseThisWithFieldAccesses && member.getExpression() instanceof ThisExpression) {
                solver.union(PureSubsetsConstraintVisitor.this.functionNode.thisNode, object);
            }
            return this;
        }
    }

    @Override
    public UnionNode visit(MethodCallExpression methodCall) {
        MemberExpression member = methodCall.getMemberExpression();
        MemberExpressionVisitor memberExpressionVisitor = new MemberExpressionVisitor(member).invoke();
        UnionNode objectExp = memberExpressionVisitor.getObjectExp();
        UnionNode result = memberExpressionVisitor.getResult();


        List<UnionNode> args = methodCall.getArgs().stream().map(arg -> arg.accept(this)).collect(Collectors.toList());

        EmptyNode returnNode = new EmptyNode(solver);
        solver.runWhenChanged(result, new CallGraphResolver(objectExp, result, args, returnNode, methodCall));
        return returnNode;
    }

    @Override
    public UnionNode visit(NewExpression newExp) {
        List<UnionNode> args = newExp.getArgs().stream().map(arg -> arg.accept(this)).collect(Collectors.toList());
        UnionNode function = newExp.getOperand().accept(this);
        UnionNode thisNode = new EmptyNode(solver);
        solver.runWhenChanged(function, new NewCallResolver(function, args, thisNode, newExp));
        return thisNode;
    }

    private final class IncludesWithFieldsResolver implements Runnable {
        private final UnionNode node;
        private final List<String> fields;

        public IncludesWithFieldsResolver(UnionNode node, String field, String... fields) {
            this(node, Stream.concat(Stream.of(field), Stream.of(fields)).collect(Collectors.toList()));
        }

        public IncludesWithFieldsResolver(UnionNode node, List<String> fields) {
            this.node = node;
            this.fields = fields;
        }

        @Override
        public void run() {
            if (!typeAnalysis.getOptions().resolveIncludesWithFields) {
                return;
            }
            UnionClass myClass = this.node.getUnionClass();
            Map<String, UnionNode> myFields = myClass.getFields();
            if (myFields == null) {
                return;
            }
            List<UnionClass> reachableClasses = this.node.getUnionClass().getReachable();
            for (UnionClass otherClass : reachableClasses) {
                if (otherClass == myClass) {
                    continue;
                }
                Map<String, UnionNode> otherFields = otherClass.getFields();
                if (otherFields == null) {
                    continue;
                }
                for (String key : this.fields) {
                    if (!otherFields.containsKey(key) || !myFields.containsKey(key)) {
                        continue;
                    }
                    UnionNode myField = myFields.get(key);
                    UnionNode otherField = otherFields.get(key);
                    if (myField.getUnionClass().includes == null || !myField.getUnionClass().includes.contains(otherField.findParent())) {
                        if (myField.getUnionClass() != otherField.getUnionClass()) {
                            solver.union(myField, new IncludeNode(solver, otherField));
                        }
                    }
                }
            }
        }
    }


    private final class NewCallResolver implements Runnable {
        private final UnionNode function;
        private final List<UnionNode> args;
        private final UnionNode thisNode;
        private Expression callExpression;
        private final CallGraphResolver callResolver;
        private final HashSet<Snap.Obj> seenHeap = new HashSet<>();

        public NewCallResolver(UnionNode function, List<UnionNode> args, UnionNode thisNode, Expression callExpression) {
            this.function = function;
            this.args = args;
            this.thisNode = thisNode;
            this.callExpression = callExpression;
            this.callResolver = new CallGraphResolver(new IncludeNode(solver, thisNode), function, args, new EmptyNode(solver), callExpression);
            this.callResolver.constructorCalls = true;
        }

        @Override
        public void run() {
            Collection<Snap.Obj> functionClosures = MixedConstraintVisitor.getFunctionClosures(function, seenHeap);
            for (Snap.Obj closure : functionClosures) {
                while (closure.function.type.equals("bind")) {
                    closure = closure.function.target;
                }
                switch (closure.function.type) {
                    case "native":
                        Snap.Property prototypeProp = closure.getProperty("prototype");
                        if (prototypeProp != null) {
                            solver.union(this.thisNode, new HasPrototypeNode(solver, (Snap.Obj) prototypeProp.value));
                        }
                        List<FunctionNode> signatures = MixedConstraintVisitor.createNativeSignatureNodes(closure, true, nativeTypeFactory);
                        for (FunctionNode signature : signatures) {
                            solver.union(this.thisNode, new IncludeNode(solver, signature.returnNode));
                        }
                        break;
                    case "user":
                        if (closure.getProperty("prototype").value instanceof Snap.UndefinedConstant) {
                            break;
                        }
                        LibraryClass clazz = typeAnalysis.getLibraryClasses().get((Snap.Obj) closure.getProperty("prototype").value);
                        if (clazz != null) {
                            if (typeAnalysis.getOptions().classOptions.unionThisFromConstructedObjects) {
                                solver.union(this.thisNode, clazz.getNewThisNode(solver));
                            }
                            solver.union(this.thisNode, new HasPrototypeNode(solver, clazz.prototype));

                            if (functionClosures.size() == 1) { // If it resolves to a unique closure, mark the class with this construction-site.
                                clazz.addUniqueConstructionSite(this.callExpression);
                            } else {
                                clazz.removeUniqueConstructionSite(this.callExpression);
                            }
                        }
                        break;
                    case "unknown":
                        break;
                    case "bind":
                        throw new RuntimeException();
                    default:
                        throw new UnsupportedOperationException("Do now know functions of type " + closure.function.type + " here.");
                }
            }

            this.callResolver.run();
        }
    }

    private final class CallGraphResolver implements Runnable {
        List<UnionNode> args;
        private final Expression callExpression; // Useful for debugging.
        boolean constructorCalls;
        private HashSet<Snap.Obj> seenHeap = new HashSet<>();
        private final FunctionNode functionNode;

        public CallGraphResolver(UnionNode thisNode, UnionNode function, List<UnionNode> args, UnionNode returnNode, Expression callExpression) {
            solver.runWhenChanged(function, new IncludesWithFieldsResolver(function, getFunctionFields(args.size())));

            this.args = args;
            this.callExpression = callExpression;

            functionNode = FunctionNode.create(args.size(), solver);
            solver.runWhenChanged(functionNode, new IncludesWithFieldsResolver(functionNode, getFunctionFields(args.size())));

            solver.union(new IncludeNode(solver, function), functionNode);

            Util.zip(functionNode.arguments.stream(), args.stream()).forEach(pair -> solver.union(pair.first, new IncludeNode(solver, pair.second), primitiveFactory.nonVoid()));

            solver.union(functionNode.returnNode, returnNode);
            solver.union(functionNode.thisNode, thisNode);
        }

        private List<String> getFunctionFields(int numberOfArgs) {
            ArrayList<String> result = new ArrayList<>();
            result.add(FunctionNode.FIELD_RETURN);
            result.add(FunctionNode.FIELD_THIS);
            for (int i = 0; i < numberOfArgs; i++) {
                result.add(FunctionNode.FIELD_ARGUMENT_PREFIX + i);
            }
            return result;
        }

        @Override
        public void run() {
            Collection<Snap.Obj> functions = MixedConstraintVisitor.getFunctionClosures(functionNode, seenHeap);

            for (Snap.Obj closure : functions) {
                switch (closure.function.type) {
                    case "user":
                    case "bind": {
                        assert PureSubsetsConstraintVisitor.this.functionNodes.containsKey(closure);

                        // This is my "mixed" way of doing it.
//                        solver.union(this.functionNode, new IncludeNode(solver, PureSubsetsConstraintVisitor.this.functionNodes.get(closure)));

                        // This is the traditional "points-to" way. By having the arguments flow to the parameters.
                        FunctionNode newFunction = FunctionNode.create(this.functionNode.arguments.size(), solver);
                        solver.union(newFunction, PureSubsetsConstraintVisitor.this.functionNodes.get(closure));
                        solver.union(functionNode.returnNode, new IncludeNode(solver, newFunction.returnNode));
                        solver.union(functionNode.thisNode, new IncludeNode(solver, newFunction.thisNode));
                        for (int i = 0; i < functionNode.arguments.size(); i++) {
                            solver.union(new IncludeNode(solver, functionNode.arguments.get(i)), newFunction.arguments.get(i));
                        }

                        break;
                    }
                    case "native": {
                        boolean emptyArgs = this.functionNode.arguments.isEmpty();
                        if (closure.function.id.equals("Function.prototype.call")) {
                            UnionNode thisNode = emptyArgs ? new EmptyNode(solver) : this.functionNode.arguments.get(0);
                            solver.runWhenChanged(this.functionNode.thisNode, new CallGraphResolver(thisNode, this.functionNode.thisNode, new ArrayList<>(), this.functionNode.returnNode, null));
                            break;
                        } else if (closure.function.id.equals("Function.prototype.apply")) {
                            UnionNode thisNode = emptyArgs ? new EmptyNode(solver) : this.functionNode.arguments.get(0);
                            UnionNode argsNode = this.functionNode.arguments.size() < 2 ? new EmptyNode(solver) : this.functionNode.arguments.get(1);
                            EmptyNode argumentType = new EmptyNode(solver);
                            solver.runWhenChanged(this.functionNode.thisNode, new CallGraphResolver(thisNode, this.functionNode.thisNode, new ArrayList<>(), this.functionNode.returnNode, null));
                            break;
                        } else if (closure.function.id.equals("Function.prototype.bind") && typeAnalysis.getOptions().FunctionDotBind) {
                            UnionNode thisNode = emptyArgs ? new EmptyNode(solver) : this.functionNode.arguments.get(0);
                            int boundArgs = this.functionNode.arguments.size() - 1;
                            AtomicInteger maxArgsSeen = new AtomicInteger(-1);

                            FunctionNode returnFunction = FunctionNode.create(0, solver);
                            solver.union(this.functionNode.returnNode, returnFunction);
                            solver.runWhenChanged(this.functionNode.thisNode, new CallGraphResolver(thisNode, this.functionNode.thisNode, returnFunction.arguments, returnFunction.returnNode, null));

                            solver.runWhenChanged(this.functionNode.thisNode, () -> {
                                int maxArgs = getFunctionNodes(this.functionNode.thisNode).stream().map(func -> func.arguments.size()).reduce(0, Math::max);
                                if (maxArgs > maxArgsSeen.get()) {
                                    maxArgsSeen.set(maxArgs);
                                    solver.union(returnFunction, FunctionNode.create(maxArgs - boundArgs, solver));
                                }
                            });
                        } else {
                            List<FunctionNode> signatures = MixedConstraintVisitor.createNativeSignatureNodes(closure, this.constructorCalls, nativeTypeFactory);
                            signatures.forEach(sig -> {
                                solver.union(functionNode.returnNode, new IncludeNode(solver, sig.returnNode));
                            });
                            break;
                        }
                    }
                    case "unknown":
                        break;
                    default:
                        throw new RuntimeException("What?");
                }
            }
        }
    }

    private Collection<FunctionNode> getFunctionNodes(UnionNode function) {
        Set<FunctionNode> result = new HashSet<>();
        for (UnionFeature feature : UnionFeature.getReachable(function.getFeature())) {
            UnionFeature.FunctionFeature functionFeature = feature.getFunctionFeature();
            if (functionFeature != null) {
                result.add(FunctionNode.create(functionFeature.getArguments().size(), solver));
            }
        }

        return result;
    }
}
