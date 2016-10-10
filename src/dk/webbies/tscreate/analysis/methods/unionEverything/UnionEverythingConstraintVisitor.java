package dk.webbies.tscreate.analysis.methods.unionEverything;

import dk.webbies.tscreate.analysis.HeapValueFactory;
import dk.webbies.tscreate.analysis.NativeTypeFactory;
import dk.webbies.tscreate.analysis.methods.mixed.MixedConstraintVisitor;
import dk.webbies.tscreate.analysis.methods.mixed.MixedTypeAnalysis;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.*;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
@SuppressWarnings("Duplicates")
public class UnionEverythingConstraintVisitor extends MixedConstraintVisitor {


    public UnionEverythingConstraintVisitor(Snap.Obj closure, UnionFindSolver solver, Map<Identifier, UnionNode> identifierMap, FunctionNode functionNode, Map<Snap.Obj, FunctionNode> functionNodes, HeapValueFactory heapFactory, MixedTypeAnalysis typeAnalysis, NativeTypeFactory nativeTypeFactory, Map<AstNode, Set<Snap.Obj>> callsites) {
        super(closure, solver, identifierMap, functionNode, functionNodes, heapFactory, typeAnalysis, nativeTypeFactory, false, callsites);
    }

    @Override
    public UnionNode visit(BinaryExpression op) {
        UnionNode lhs = op.getLhs().accept(this);
        UnionNode rhs = op.getRhs().accept(this);

        solver.union(lhs, primitiveFactory.nonVoid());
        solver.union(rhs, primitiveFactory.nonVoid());

        switch (op.getOperator()) {
            case PLUS: {
                return solver.union(lhs, rhs, primitiveFactory.stringOrNumber());
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
                return solver.union(lhs, rhs);
            case MINUS: // -
            case MULT: // *
            case DIV: // /
            case MOD: // %
            case MINUS_EQUAL: // -=
            case MULT_EQUAL: // *=
            case DIV_EQUAL: // /=
            case MOD_EQUAL: // %=
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
            case LESS_THAN: // <
            case LESS_THAN_EQUAL: // <=
            case GREATER_THAN: // >
            case GREATER_THAN_EQUAL: // >=
                solver.union(primitiveFactory.number(), lhs);
                solver.union(primitiveFactory.number(), rhs);
                return primitiveFactory.bool();
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
    public UnionNode visit(DynamicAccessExpression dynamicAccessExpression) {
        UnionNode lookupKey = dynamicAccessExpression.getLookupKey().accept(this);
        UnionNode operand = dynamicAccessExpression.getOperand().accept(this);
        UnionNode returnType = primitiveFactory.nonVoid();

        solver.union(lookupKey, primitiveFactory.stringOrNumber());
        DynamicAccessNode dynamicAccessNode = new DynamicAccessNode(solver, returnType, lookupKey);
        solver.union(operand, dynamicAccessNode);
        solver.runWhenChanged(operand, new IncludesWithFieldsResolver(operand, DynamicAccessNode.LOOKUP_EXP_KEY, DynamicAccessNode.RETURN_TYPE_KEY));
        DynamicAccessResolver dynamicAccessResolver = new DynamicAccessResolver(operand, lookupKey, returnType, solver);
        solver.runWhenChanged(operand, dynamicAccessResolver);
        solver.runWhenChanged(lookupKey, dynamicAccessResolver);
        return returnType;
    }

    public static final class DynamicAccessResolver extends UnionFindCallback {
        private final UnionNode operand;
        private final UnionNode lookupKey;
        private final UnionNode returnType;
        private UnionFindSolver solver;

        public DynamicAccessResolver(UnionNode operand, UnionNode lookupKey, UnionNode returnType, UnionFindSolver solver) {
            super(operand, lookupKey, returnType);
            this.operand = operand;
            this.lookupKey = lookupKey;
            this.returnType = returnType;
            this.solver = solver;
        }

        @Override
        public void run() {
            // I simply refuse to unify every string-property of any object. It absolutely destroys the analysis.
            List<UnionNode> newIncludes = new ArrayList<>();
            UnionFeature.getReachable(operand.getFeature()).forEach(feature -> {
                feature.getObjectFields().forEach((fieldName, fieldType) -> {
                    fieldType = fieldType.findParent();
                    if (Util.isInteger(fieldName)) {
                        if (returnType.getUnionClass().includes == null || !returnType.getUnionClass().includes.contains(fieldType)) {
                            newIncludes.add(fieldType);
                        }
                    }
                });
            });
            if (!newIncludes.isEmpty()) {
                solver.union(returnType, newIncludes);
            }
        }
    }

    @Override
    public UnionNode visit(Return aReturn) {
        if (aReturn.getExpression() instanceof UnaryExpression && ((UnaryExpression) aReturn.getExpression()).getOperator() == Operator.VOID) {
            // This is a return;
            return new EmptyNode(solver);
        }
        solver.union(aReturn.getExpression().accept(this), functionNode.returnNode, primitiveFactory.nonVoid());
        return null;
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
            new UnionEverythingConstraintVisitor(this.closure, this.solver, this.identifierMap, result, this.functionNodes, heapFactory, typeAnalysis, this.nativeTypeFactory, callsites).visit(function.getBody());
            for (int i = 0; i < function.getArguments().size(); i++) {
                UnionNode parameter = function.getArguments().get(i).accept(this);
                solver.union(parameter, result.arguments.get(i), primitiveFactory.nonVoid());
            }
            solver.union(result, primitiveFactory.function());
            return result;
        }
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

    private final class NewCallResolver extends UnionFindCallback {
        private final UnionNode function;
        private final List<UnionNode> args;
        private final UnionNode thisNode;
        private Expression callExpression;
        private final CallGraphResolver callResolver;
        private final HashSet<Snap.Obj> seenHeap = new HashSet<>();

        public NewCallResolver(UnionNode function, List<UnionNode> args, UnionNode thisNode, Expression callExpression) {
            super(function, thisNode, args);
            this.function = function;
            this.args = args;
            this.thisNode = thisNode;
            this.callExpression = callExpression;
            this.callResolver = new CallGraphResolver(thisNode, function, args, new EmptyNode(solver), callExpression);
            this.callResolver.constructorCalls = true;
        }

        @Override
        public void run() {
            Collection<Snap.Obj> functionClosures = getFunctionClosures(function, seenHeap, callExpression, callsites, typeAnalysis.getOptions());
            for (Snap.Obj closure : functionClosures) {
                if (closure.function.type.equals("bind")) {
                    closure = closure.function.target;
                }
                switch (closure.function.type) {
                    case "native":
                        Snap.Property prototypeProp = closure.getProperty("prototype");
                        if (prototypeProp != null) {
                            solver.union(this.thisNode, new HasPrototypeNode(solver, (Snap.Obj) prototypeProp.value));
                        }
                        List<FunctionNode> signatures = createNativeSignatureNodes(closure, true, nativeTypeFactory);
                        for (FunctionNode signature : signatures) {
                            solver.union(this.thisNode, new IncludeNode(solver, signature.returnNode)); // Does effectively the same thing as TSCheck, just more efficiently.
                        }
                        break;
                    case "user":
                        if (closure.getProperty("prototype").value instanceof Snap.UndefinedConstant) {
                            break;
                        }
                        LibraryClass clazz = typeAnalysis.libraryClasses.get((Snap.Obj) closure.getProperty("prototype").value);
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
                    case "bind":
                        throw new RuntimeException();
                    default:
                        throw new UnsupportedOperationException("Do now know functions of type " + closure.function.type + " here.");
                }
            }

            this.callResolver.run();
        }
    }

    @SuppressWarnings("Duplicates")
    private final class CallGraphResolver extends UnionFindCallback  {
        List<UnionNode> args;
        private final Expression callExpression; // Useful for debugging.
        boolean constructorCalls;
        private HashSet<Snap.Obj> seenHeap = new HashSet<>();
        private final FunctionNode functionNode;

        public CallGraphResolver(UnionNode thisNode, UnionNode function, List<UnionNode> args, UnionNode returnNode, Expression callExpression) {
            super(thisNode, function, returnNode, args);
            MixedConstraintVisitor.DisableSomeCallFlow disableSomeCallFlow = new MixedConstraintVisitor.DisableSomeCallFlow(args, returnNode).invoke(typeAnalysis, solver);
            args = disableSomeCallFlow.getArgs();
            returnNode = disableSomeCallFlow.getReturnNode();

            solver.runWhenChanged(function, new IncludesWithFieldsResolver(function, getFunctionFields(args.size())));

            this.args = args;
            this.callExpression = callExpression;

            functionNode = FunctionNode.create(args.size(), solver);
            solver.union(function, functionNode);

            Util.zip(functionNode.arguments.stream(), args.stream()).forEach(pair -> solver.union(pair.left, pair.right, primitiveFactory.nonVoid()));

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
            Collection<Snap.Obj> functions = getFunctionClosures(functionNode, seenHeap, callExpression, callsites, typeAnalysis.getOptions());

            for (Snap.Obj closure : functions) {
                switch (closure.function.type) {
                    case "user":
                    case "bind": {
                        assert UnionEverythingConstraintVisitor.this.functionNodes.containsKey(closure);
                        // no includeNode, and it is supposed to be that way.
                        FunctionNode calledFunction = UnionEverythingConstraintVisitor.this.functionNodes.get(closure);
                        solver.union(functionNode.returnNode, calledFunction.returnNode);
//                        solver.union(functionNode.thisNode, new IncludeNode(solver, newFunction.thisNode));
                        for (int i = 0; i < Math.min(functionNode.arguments.size(), calledFunction.arguments.size()); i++) {
                            solver.union(functionNode.arguments.get(i), calledFunction.arguments.get(i));
                        }

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
                        } else if (closure.function.id.equals("Function.prototype.bind") && typeAnalysis.getOptions().FunctionDotBind) {
                            UnionNode thisNode = emptyArgs ? new EmptyNode(solver) : this.functionNode.arguments.get(0);
                            int boundArgs = this.functionNode.arguments.size() - 1;
                            AtomicInteger maxArgsSeen = new AtomicInteger(-1);

                            FunctionNode returnFunction = FunctionNode.create(0, solver);
                            solver.union(this.functionNode.returnNode, returnFunction);
                            solver.runWhenChanged(this.functionNode.thisNode, new CallGraphResolver(thisNode, this.functionNode.thisNode, returnFunction.arguments, returnFunction.returnNode, null));

                            // FIXME:
                            /*solver.runWhenChanged(this.functionNode.thisNode, () -> {
                                int maxArgs = getFunctionNodes(this.functionNode.thisNode).stream().map(func -> func.arguments.size()).reduce(0, Math::max);
                                if (maxArgs > maxArgsSeen.get()) {
                                    maxArgsSeen.set(maxArgs);
                                    solver.union(returnFunction, FunctionNode.create(maxArgs - boundArgs, solver));
                                }
                            });*/
                        } else {
                            // This is actually better than what TSCheck does, it ignores the arguments.
                            List<FunctionNode> signatures = UnionEverythingConstraintVisitor.createNativeSignatureNodes(closure, this.constructorCalls, nativeTypeFactory);
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
}
