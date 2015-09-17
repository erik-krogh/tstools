package dk.webbies.tscreate.analysis;

import com.google.common.collect.Iterables;
import dk.brics.tajs.envspec.typescript.types.*;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.analysis.unionFind.nodes.*;
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
    private final HashMap<Snap.Obj, LibraryClass> libraryClasses;
    private final Options options;
    private Snap.Obj globalObject;
    private final PrimitiveUnionNode.Factory primitiveFactory;
    private HeapValueNode.Factory heapFactory;

    public UnionConstraintVisitor(Snap.Obj function, UnionFindSolver solver, Map<TypeAnalysis.ProgramPoint, UnionNode> nodes, FunctionNode functionNode, Map<Snap.Obj, FunctionNode> functionNodes, HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj globalObject, HeapValueNode.Factory heapFactory) {
        this.closure = function;
        this.solver = solver;
        this.heapFactory = heapFactory;
        this.nodes = nodes;
        this.functionNode = functionNode;
        this.functionNodes = functionNodes;
        this.libraryClasses = libraryClasses;
        this.options = options;
        this.globalObject = globalObject;
        this.primitiveFactory = new PrimitiveUnionNode.Factory(solver, globalObject);
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
                result = new AddNode(lhs, rhs);
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
                    return lhs;
                } else {
                    result = new EmptyUnionNode(); // && and || is used to often for other things.
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
        solver.union(exp, functionNode.returnNode);
        solver.union(exp, new NonVoidNode());
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
            solver.union(get(function), functionNode);
            solver.union(functionNode, heapFactory.fromValue(this.closure));
            return null;
        } else {
            FunctionNode result = new FunctionNode(function);
            if (function.getName() != null) {
                solver.union(get(function.getName()), result);
                function.getName().accept(this);
            }
            new UnionConstraintVisitor(this.closure, this.solver, this.nodes, result, this.functionNodes, libraryClasses, options, globalObject, heapFactory).visit(function.getBody());
            for (int i = 0; i < function.getArguments().size(); i++) {
                solver.union(get(function.getArguments().get(i)), result.arguments.get(i));
                solver.union(get(function.getArguments().get(i)), new NonVoidNode());
            }
            return solver.union(get(function), result);
        }
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
            List<UnionNode> unionNodes = solver.getUnionClass(get(member.getExpression())).getNodes();

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
                List<UnionNode> nodes = lookupProperty(prototype, member.getProperty());
                solver.union(get(member), nodes);
            }
        }

        private List<UnionNode> lookupProperty(Snap.Value value, String name) {
            if (value == null) {
                return Collections.EMPTY_LIST;
            }
            if (!(value instanceof Snap.Obj)) {
                return Collections.EMPTY_LIST;
            }
            Snap.Obj obj = (Snap.Obj) value;
            Snap.Property property = obj.getProperty(name);
            if (property != null) {
                return heapFactory.fromValue(property.value);
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

        public NewCallResolver(UnionNode function, List<UnionNode> args, UnionNode thisNode) {
            this.function = function;
            this.args = args;
            this.thisNode = thisNode;
            this.callResolver = new CallGraphResolver(thisNode, function, args, new EmptyUnionNode());
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
                            solver.union(this.thisNode, new HasPrototypeUnionNode((Snap.Obj) prototypeProp.value));
                        }
                        solver.union(node.returnNode, thisNode);
                        break;
                    case "user":
                        @SuppressWarnings("RedundantCast")
                        LibraryClass clazz = libraryClasses.get((Snap.Obj) node.closure.getProperty("prototype").value);
                        if (clazz != null) {
                            clazz.isUsedAsClass = true;
                            solver.union(this.thisNode, clazz.thisNode);
                            solver.union(this.thisNode, new HasPrototypeUnionNode(clazz.prototype));

                            solver.union(clazz.functionNode, this.function);
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
                                UnionConstraintVisitor.this.heapFactory).
                                visit(node.closure.function.astNode);
                    }
                }
            }
        }
    }

    private List<FunctionNode> getFunctionNodes(UnionNode function, Set<HeapValueNode> seenHeap, boolean constructorCalls, List<UnionNode> args) {
        List<UnionNode> nodes = solver.getUnionClass(function).getNodes();
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
                    FunctionNode functionNode = UnionConstraintVisitor.this.functionNodes.get(closure);
                    if (functionNode == null) {
                        if (!options.separateFunctions) {
                            throw new RuntimeException("All closures should have a functionNode at this point");
                        }
                        functionNode = new FunctionNode(closure);
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
                    result.addAll(signatures.stream().map(sig -> signatureToFunctionNode(sig, closure, args)).collect(Collectors.toList()));
                    break;
                case "unknown":
                    break;
                default:
                    throw new UnsupportedOperationException("Cannot yet handle functions of type: " + type);
            }
        }

        return result;
    }

    private Map<Signature, FunctionNode> signatureCache = new HashMap<>();
    public FunctionNode signatureToFunctionNode(Signature signature, Snap.Obj closure, List<UnionNode> args) {
        if (args == null) {
            args = Collections.EMPTY_LIST;
        }
        if (signatureCache.containsKey(signature)) {
            return signatureCache.get(signature);
        }
        List<String> argumentNames = signature.getParameters().stream().map(Parameter::getName).collect(Collectors.toList());
        for (int i = argumentNames.size(); i < args.size(); i++) {
            argumentNames.add("arg" + i);
        }
        FunctionNode functionNode = new FunctionNode(closure, argumentNames);
        signatureCache.put(signature, functionNode);

        int normalParameterCount = signature.getParameters().size();
        if (signature.isHasRestParameter()) {
            normalParameterCount--;
            Type restType = Iterables.getLast(signature.getParameters()).getType();
            List<Type> typeArguments;
            if (restType instanceof ReferenceType) {
                ReferenceType ref = (ReferenceType) restType;
                typeArguments = ref.getTypeArguments();
            } else if (restType instanceof GenericType) { // TODO: problem with DeclarationParser; RegExpExecArray.splice(start: number, deleteCount: number, ...items: string[]): string[]; Items is just an Array[AnonymousType].
                GenericType generic = (GenericType) restType;
                typeArguments = generic.getTypeArguments();
            } else {
                throw new RuntimeException();
            }

            if (typeArguments.size() != 1) {
                throw new RuntimeException();
            }

            Type typeArgument = Iterables.getLast(typeArguments);
            for (int i = normalParameterCount; i < args.size(); i++) {
                solver.union(args.get(i), typeArgument.accept(new UnionNativeTypeVisitor()));
                solver.union(functionNode.arguments.get(i), args.get(i));
            }
        }
        for (int i = 0; i < normalParameterCount; i++) {
            Type parameter = signature.getParameters().get(i).getType();
            UnionNode argument = functionNode.arguments.get(i);
            solver.union(argument, parameter.accept(new UnionNativeTypeVisitor()));
        }
        solver.union(functionNode.returnNode, signature.getResolvedReturnType().accept(new UnionNativeTypeVisitor()));

        functionNode.arguments.forEach(arg -> solver.union(arg, new NonVoidNode()));

        return functionNode;
    }

    private class UnionNativeTypeVisitor implements TypeVisitor<List<UnionNode>> {

        @Override
        public List<UnionNode> visit(AnonymousType t) {
            return Collections.EMPTY_LIST;
        }

        @Override
        public List<UnionNode> visit(ClassType t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<UnionNode> visit(GenericType t) {
            return t.toInterface().accept(this);
        }

        Map<InterfaceType, List<UnionNode>> cache = new HashMap<>();
        @Override
        public List<UnionNode> visit(InterfaceType t) {
            if (cache.containsKey(t)) {
                return cache.get(t);
            }
            ArrayList<UnionNode> result = new ArrayList<>();
            cache.put(t, result);

            ObjectUnionNode obj = new ObjectUnionNode();
            if (t.getName() != null) {
                obj.setTypeName(t.getName());
            }
            result.add(obj);

            List<Map.Entry<UnionNode, List<UnionNode>>> delayedUnions = new ArrayList<>();
            for (Map.Entry<String, Type> entry : t.getDeclaredProperties().entrySet()) {
                String key = entry.getKey();
                Type type = entry.getValue();

                EmptyUnionNode fieldNode = new EmptyUnionNode();
                obj.addField(key, fieldNode);
                delayedUnions.add(new AbstractMap.SimpleEntry<>(fieldNode, type.accept(this)));
            }

            if (!t.getDeclaredCallSignatures().isEmpty()) {
                List<FunctionNode> functionNodes = t.getDeclaredCallSignatures().stream().map(sig -> signatureToFunctionNode(sig, null, null)).collect(Collectors.toList());
                result.addAll(functionNodes);
            }

            if (!t.getDeclaredConstructSignatures().isEmpty()) {
                List<FunctionNode> functionNodes = t.getDeclaredConstructSignatures().stream().map(sig -> signatureToFunctionNode(sig, null, null)).collect(Collectors.toList());
                result.addAll(functionNodes);
            }

            for (Map.Entry<UnionNode, List<UnionNode>> entry : delayedUnions) {
                solver.union(entry.getKey(), entry.getValue());
            }

            return result;
        }

        @Override
        public List<UnionNode> visit(ReferenceType t) {
            return t.getTarget().accept(this);
        }

        @Override
        public List<UnionNode> visit(SimpleType t) {
            switch (t.getKind()) {
                case Any: return Arrays.asList(primitiveFactory.any());
                case Boolean: return Arrays.asList(primitiveFactory.bool());
                case Enum: throw new UnsupportedOperationException();
                case Number: return Arrays.asList(primitiveFactory.number());
                case String: return Arrays.asList(primitiveFactory.string());
                case Undefined: return Arrays.asList(primitiveFactory.undefined());
                case Void: return Collections.EMPTY_LIST;
                default:
                    throw new UnsupportedOperationException("Unhandled type: " + t.getKind());
            }
        }

        @Override
        public List<UnionNode> visit(TupleType t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<UnionNode> visit(UnionType t) {
//            throw new UnsupportedOperationException(); // TODO:
            return Collections.EMPTY_LIST;
        }

        @Override
        public List<UnionNode> visit(UnresolvedType t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<UnionNode> visit(TypeParameterType t) {
            // Generics, doesn't do this yet.
            return Collections.EMPTY_LIST;
        }

        @Override
        public List<UnionNode> visit(SymbolType t) {
            throw new UnsupportedOperationException();
        }
    }
}
