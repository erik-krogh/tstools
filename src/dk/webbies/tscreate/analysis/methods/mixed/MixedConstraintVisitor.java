package dk.webbies.tscreate.analysis.methods.mixed;

import dk.au.cs.casa.typescript.types.Signature;
import dk.webbies.tscreate.analysis.HeapValueFactory;
import dk.webbies.tscreate.analysis.NativeTypeFactory;
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
public class MixedConstraintVisitor implements ExpressionVisitor<UnionNode>, StatementTransverse<UnionNode> {
    protected final Snap.Obj closure;
    protected final UnionFindSolver solver;
    protected final Map<Identifier, UnionNode> identifierMap;
    protected final FunctionNode functionNode;
    protected final Map<Snap.Obj, FunctionNode> functionNodes;
    protected MixedTypeAnalysis typeAnalysis;
    protected final PrimitiveNode.Factory primitiveFactory;
    protected HeapValueFactory heapFactory;
    protected NativeTypeFactory nativeTypeFactory;

    public MixedConstraintVisitor(
            Snap.Obj closure,
            UnionFindSolver solver,
            Map<Identifier, UnionNode> identifierMap,
            FunctionNode functionNode,
            Map<Snap.Obj, FunctionNode> functionNodes,
            HeapValueFactory heapFactory,
            MixedTypeAnalysis typeAnalysis,
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
                solver.union(lhs, primitiveFactory.stringOrNumber());
                solver.union(rhs, primitiveFactory.stringOrNumber());
                return new IncludeNode(solver, lhs, rhs, primitiveFactory.stringOrNumber());
            }

            case EQUAL: // = // assignment
                solver.union(lhs, rhs);
                return lhs;
            case PLUS_EQUAL: // +=
                solver.union(lhs, rhs, primitiveFactory.stringOrNumber());
                return lhs;
            case NOT_EQUAL: // !=
            case EQUAL_EQUAL: // ==
            case NOT_EQUAL_EQUAL: // !==
            case EQUAL_EQUAL_EQUAL: // ===
                solver.union(lhs, rhs);
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
                solver.union(primitiveFactory.number(), lhs);
                solver.union(primitiveFactory.number(), rhs);
                return primitiveFactory.number();
            case INSTANCEOF: // instanceof
                return primitiveFactory.bool();
            case IN: // in
                solver.union(lhs, primitiveFactory.string());
                solver.union(rhs, new ObjectNode(solver));
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
        return result;
    }

    @Override
    public UnionNode visit(ForInStatement forIn) {
        forIn.getInitializer().accept(new NodeTransverse<Void>() {
            @Override
            public Void visit(Identifier identifier) {
                solver.union(identifier.accept(MixedConstraintVisitor.this), primitiveFactory.string());
                return null;
            }
        });
        solver.union(forIn.getCollection().accept(this), new ObjectNode(solver));
        return null;
    }

    @Override
    public UnionNode visit(ThisExpression thisExpression) {
        if (typeAnalysis.options.classOptions.onlyUseThisWithFieldAccesses) {
            return new EmptyNode(solver);
        } else {
            return this.functionNode.thisNode;
        }
    }

    @Override
    public UnionNode visit(ConditionalExpression condExp) {
        condExp.getCondition().accept(this);

        UnionNode left = condExp.getLeft().accept(this);
        UnionNode right = condExp.getRight().accept(this);
        return solver.union(left, right);
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
    public UnionNode visit(SwitchStatement switchStatement) {
        solver.union(switchStatement.getExpression().accept(this), primitiveFactory.stringOrNumber());
        StatementTransverse.super.visit(switchStatement);
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
        if (!typeAnalysis.options.unionHeapIdentifiers) {
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
            solver.union(arrayType, expressionNode);
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
            new MixedConstraintVisitor(this.closure, this.solver, this.identifierMap, result, this.functionNodes, heapFactory, typeAnalysis, this.nativeTypeFactory).visit(function.getBody());
            for (int i = 0; i < function.getArguments().size(); i++) {
                UnionNode parameter = function.getArguments().get(i).accept(this);
                solver.union(new IncludeNode(solver, parameter), result.arguments.get(i), primitiveFactory.nonVoid());
            }
            solver.union(result, primitiveFactory.function());
            return result;
        }
    }

    protected static boolean closureMatch(FunctionExpression function, Snap.Obj closure) {
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
    public UnionNode visit(DynamicAccessExpression dynamicAccessExpression) {
        UnionNode lookupKey = dynamicAccessExpression.getLookupKey().accept(this);
        UnionNode operand = dynamicAccessExpression.getOperand().accept(this);
        UnionNode returnType = new EmptyNode(solver);

        solver.union(lookupKey, primitiveFactory.stringOrNumber());
        DynamicAccessNode dynamicAccessNode = new DynamicAccessNode(solver, returnType, lookupKey);
        solver.union(operand, dynamicAccessNode);
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

    protected class MemberExpressionVisitor {
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
            objectExp = member.getExpression().accept(MixedConstraintVisitor.this);
            ObjectNode object = new ObjectNode(solver);
            result = new EmptyNode(solver);
            object.addField(member.getProperty(), result);
            solver.union(object, objectExp);
            solver.union(primitiveFactory.nonVoid(), result);
            solver.runWhenChanged(object, new MemberResolver(member, objectExp, result));
            solver.runWhenChanged(object, new IncludesWithFieldsResolver(object, ObjectNode.FIELD_PREFIX + member.getProperty()));

            if (typeAnalysis.options.classOptions.onlyUseThisWithFieldAccesses && member.getExpression() instanceof ThisExpression) {
                solver.union(MixedConstraintVisitor.this.functionNode.thisNode, object);
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

    protected final class IncludesWithFieldsResolver implements Runnable {
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
            if (!typeAnalysis.options.resolveIncludesWithFields) {
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

    private final class MemberResolver implements Runnable {
        private MemberExpression member;
        private final UnionNode expressionNode;
        private final UnionNode memberNode;
        private Set<Snap.Obj> seenPrototypes = new HashSet<>();

        // expressionNode = node for "foo" in "foo.bar".
        // memberNode = node for "foo.bar" in "foo.bar".
        public MemberResolver(MemberExpression member, UnionNode expressionNode, UnionNode memberNode) {
            this.member = member;
            this.expressionNode = expressionNode;
            this.memberNode = memberNode;
        }

        @Override
        public void run() {
            List<UnionFeature> features = UnionFeature.getReachable(expressionNode.getFeature());

            Set<Snap.Obj> prototypes = new HashSet<>();
            for (UnionFeature feature : features) {
                prototypes.addAll(feature.getPrototypes());
            }

            prototypes.removeAll(seenPrototypes);
            seenPrototypes.addAll(prototypes);
            for (Snap.Obj prototype : new HashSet<>(prototypes)) {
                UnionNode propertyNode = lookupProperty(prototype, member.getProperty());
                solver.union(memberNode, propertyNode);
            }
        }

        private UnionNode lookupProperty(Snap.Value value, String name) {
            if (value == null || !(value instanceof Snap.Obj)) {
                return new EmptyNode(solver);
            }
            Snap.Obj obj = (Snap.Obj) value;
            Snap.Property property = obj.getProperty(name);
            if (property != null) {
                return heapFactory.fromProperty(property);
            }

            if (obj != obj.prototype) {
                return lookupProperty(obj.prototype, name);
            } else {
                return new EmptyNode(solver);
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
            this.callResolver = new CallGraphResolver(thisNode, function, args, new EmptyNode(solver), callExpression);
            this.callResolver.constructorCalls = true;
        }

        @Override
        public void run() {
            Collection<Snap.Obj> functionClosures = getFunctionClosures(function, seenHeap);
            for (Snap.Obj closure : functionClosures) {
                switch (closure.function.type) {
                    case "native":
                        Snap.Property prototypeProp = closure.getProperty("prototype");
                        if (prototypeProp != null) {
                            solver.union(this.thisNode, new HasPrototypeNode(solver, (Snap.Obj) prototypeProp.value));
                        }
                        List<FunctionNode> signatures = createNativeSignatureNodes(closure, true, nativeTypeFactory);
                        for (FunctionNode signature : signatures) {
                            solver.union(this.thisNode, new IncludeNode(solver, signature.returnNode));
                        }
                        break;
                    case "user":
                        if (closure.getProperty("prototype").value instanceof Snap.UndefinedConstant) {
                            break;
                        }
                        LibraryClass clazz = typeAnalysis.libraryClasses.get((Snap.Obj) closure.getProperty("prototype").value);
                        if (clazz != null) {
                            if (typeAnalysis.options.classOptions.unionThisFromConstructedObjects) {
                                solver.union(this.thisNode, clazz.getNewThisNode(solver));
                            }
                            solver.union(this.thisNode, new HasPrototypeNode(solver, clazz.prototype));

                            if (typeAnalysis.options.classOptions.useConstructorUsages) {
                                solver.union(clazz.getNewConstructorNode(solver), this.function);
                            }

                            if (functionClosures.size() == 1) { // If it resolves to a unique closure, mark the class with this construction-site.
                                clazz.addUniqueConstructionSite(this.callExpression);
                            } else {
                                clazz.removeUniqueConstructionSite(this.callExpression);
                            }
                        }
                        break;
                    case "unknown":
                    case "bind": // FIXME:
                        break; // Nothing we can do.
                    default:
                        throw new UnsupportedOperationException("Do now know functions of type " + closure.function.type + " here.");
                }
            }

            this.callResolver.run();
        }
    }

    @SuppressWarnings("Duplicates")
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
            solver.union(function, functionNode);

            Util.zip(functionNode.arguments.stream(), args.stream()).forEach(pair -> solver.union(pair.first, pair.second, primitiveFactory.nonVoid()));

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
            Collection<Snap.Obj> functions = getFunctionClosures(functionNode, seenHeap);

            for (Snap.Obj closure : functions) {
                switch (closure.function.type) {
                    case "user":
                    case "bind": {
//                        assert MixedConstraintVisitor.this.functionNodes.containsKey(closure);
                        solver.union(this.functionNode, new IncludeNode(solver, MixedConstraintVisitor.this.functionNodes.get(closure)));

                         /*// This is the traditional "points-to" way. By having the arguments flow to the parameters.
                        FunctionNode newFunction = FunctionNode.create(this.functionNode.arguments.size(), solver);
                        solver.union(newFunction, MixedConstraintVisitor.this.functionNodes.get(closure));
                        solver.union(functionNode.returnNode, new IncludeNode(solver, newFunction.returnNode));
                        solver.union(functionNode.thisNode, new IncludeNode(solver, newFunction.thisNode));
                        for (int i = 0; i < functionNode.arguments.size(); i++) {
                            solver.union(new IncludeNode(solver, functionNode.arguments.get(i)), newFunction.arguments.get(i));
                        }*/

                        break;
                    }
                    case "native": {
                        boolean emptyArgs = this.functionNode.arguments.isEmpty();
                        if (closure.function.id.equals("Function.prototype.call")) {
                            UnionNode thisNode = emptyArgs ? new EmptyNode(solver) : this.functionNode.arguments.get(0);
                            List<UnionNode> arguments = emptyArgs ? Collections.EMPTY_LIST : this.functionNode.arguments.subList(1, this.functionNode.arguments.size());
                            solver.runWhenChanged(this.functionNode.thisNode, new CallGraphResolver(thisNode, this.functionNode.thisNode, arguments, this.functionNode.returnNode, null));
                            break;
                        } else if (closure.function.id.equals("Function.prototype.apply")) {
                            UnionNode thisNode = emptyArgs ? new EmptyNode(solver) : this.functionNode.arguments.get(0);
                            UnionNode argsNode = this.functionNode.arguments.size() < 2 ? new EmptyNode(solver) : this.functionNode.arguments.get(1);
                            EmptyNode argumentType = new EmptyNode(solver);
                            solver.union(argsNode, new DynamicAccessNode(solver, argumentType, primitiveFactory.number()));
                            List<UnionNode> arguments = Arrays.asList(argumentType, argumentType, argumentType, argumentType, argumentType);
                            solver.runWhenChanged(this.functionNode.thisNode, new CallGraphResolver(thisNode, this.functionNode.thisNode, arguments, this.functionNode.returnNode, null));
                            break;
                        } else if (closure.function.id.equals("Function.prototype.bind") && typeAnalysis.options.FunctionDotBind) {
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
                            solver.union(functionNode, new IncludeNode(solver, signatures));
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

    protected static List<FunctionNode> createNativeSignatureNodes(Snap.Obj closure, boolean constructorCalls, NativeTypeFactory functionNodeFactory) {
        List<Signature> signatures;
        if (constructorCalls) {
            signatures = closure.function.constructorSignatures;
        } else {
            signatures = closure.function.callSignatures;
        }
        List<FunctionNode> result = new ArrayList<>();
        for (Signature signature : signatures) {
            result.add(functionNodeFactory.fromSignature(signature));
        }
        return result;
    }

    protected Collection<Snap.Obj> getFunctionClosures(UnionNode function, HashSet<Snap.Obj> seenHeap) {
        Set<Snap.Obj> result = new HashSet<>();
        for (UnionFeature feature : UnionFeature.getReachable(function.getFeature())) {
            if (feature.getFunctionFeature() != null) {
                for (Snap.Obj closure : feature.getFunctionFeature().getClosures()) {
                    if (closure.function == null) {
                        continue;
                    }
                    if (seenHeap.contains(closure)) {
                        continue;
                    }
                    seenHeap.add(closure);

                    result.add(closure);
                }

            }
        }

        return result;
    }

    protected Collection<FunctionNode> getFunctionNodes(UnionNode function) {
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
