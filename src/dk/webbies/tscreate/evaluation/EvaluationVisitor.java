package dk.webbies.tscreate.evaluation;

import dk.au.cs.casa.typescript.types.*;
import dk.webbies.tscreate.util.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;

/**
 * Created by Erik Krogh Kristensen on 14-12-2015.
 */
public class EvaluationVisitor implements TypeVisitorWithArgument<Void, EvaluationVisitor.Arg> {
    private final int depth;
    private final Evaluation evaluation;
    private List<Runnable> queue;
    private Set<Type> nativeTypesInReal;
    private final NativeClassesMap realNativeClasses;
    private final NativeClassesMap myNativeClasses;
    private Set<Pair<Type, Type>> seen;

    public EvaluationVisitor(int depth, Evaluation evaluation, List<Runnable> queue, Set<Type> nativeTypesInReal, NativeClassesMap realNativeClasses, NativeClassesMap myNativeClasses, Set<Pair<Type, Type>> seen) {
        this.depth = depth;
        this.evaluation = evaluation;
        this.queue = queue;
        this.nativeTypesInReal = nativeTypesInReal;
        this.realNativeClasses = realNativeClasses;
        this.myNativeClasses = myNativeClasses;
        this.seen = seen;
    }

    static final class Arg {
        Type type;
        Runnable callback;

        public Arg(Type type, Runnable callback) {
            this.type = type;
            this.callback = callback;
        }
    }

    private static int runCounter = 0;
    private void nextDepth(Type realType, Type myType, Runnable callback) {
        if (seen.contains(new Pair<>(realType, myType))) {
            callback.run();
            return;
        }
        int counter = runCounter++;
        seen.add(new Pair<>(realType, myType));

        queue.add(() -> {
            if (realType instanceof UnionType && myType instanceof UnionType) {
                ArrayList<Pair<Type, Type>> typePairs = new ArrayList<>();

                for (Type realSubType : ((UnionType) realType).getElements()) {
                    //noinspection Convert2streamapi
                    for (Type mySubType : ((UnionType) myType).getElements()) {
                        typePairs.add(new Pair<>(realSubType, mySubType));
                    }
                }

                findBest(typePairs, (evaluation) -> {
                    EvaluationVisitor.this.evaluation.add(evaluation);
                    callback.run();
                });
            } else if (myType instanceof UnionType) {
                // TODO: Consider doing something, to punish the incorrect union-types.
                findBest(((UnionType)myType).getElements().stream().map(elem -> new Pair<>(realType, elem)).collect(Collectors.toList()),
                    (evaluation) -> {
                        EvaluationVisitor.this.evaluation.add(evaluation);
                        callback.run();
                    });
            } else if (realType instanceof UnionType) {
                findBest(((UnionType)realType).getElements().stream().map(elem -> new Pair<>(elem, myType)).collect(Collectors.toList()),
                        (evaluation) -> {
                            EvaluationVisitor.this.evaluation.add(evaluation);
                            callback.run();
                        });
            } else {
                analyzeNextDepth(realType, myType, callback);
            }
        });
    }

    private void findBest(Collection<Pair<Type, Type>> typePairs, Consumer<Evaluation> callback) {
        AtomicInteger downCounter = new AtomicInteger(typePairs.size());
        List<Evaluation> evaluations = new ArrayList<>();

        for (Pair<Type, Type> typePair : typePairs) {
            Type left = typePair.first;
            Type right = typePair.second;

            Evaluation subEvaluation = new Evaluation();
            EvaluationVisitor visitor = new EvaluationVisitor(depth, subEvaluation, queue, nativeTypesInReal, realNativeClasses, myNativeClasses, seen);

            visitor.nextDepth(left, right, () -> {
                evaluations.add(subEvaluation);
                int i = downCounter.decrementAndGet();
                if (i == 0) {
                    Evaluation bestEvaluation = Collections.max(evaluations, (o1, o2) -> {
                        for (int depth = 0; depth <= Math.max(o1.maxDepth, o2.maxDepth); depth++) {
                            if ((o1.getTruePositives(depth) == 0 || o2.getTruePositives(depth) == 0) && o1.getTruePositives(depth) != o2.getTruePositives(depth)) {
                                return Integer.compare(o1.getTruePositives(depth), o2.getTruePositives(depth));
                            }

                            double o1Prec = Math.min(o1.precision(depth), o1.recall(depth));
                            double o2Prec = Math.min(o2.precision(depth), o2.recall(depth));
                            int compared = Double.compare(o1Prec, o2Prec);
                            if (compared != 0) {
                                return compared;
                            }
                        }
                        return 0;
                    });
                    callback.accept(bestEvaluation);
                }
            });
        }

    }

    private void analyzeNextDepth(Type realType, Type myType, Runnable callback) {
        assert !(realType instanceof UnionType);
        assert !(myType instanceof UnionType);

        if (nativeTypesInReal.contains(realType)) {
            String realTypeName = realNativeClasses.nameFromType(realType);
            assert realTypeName != null;
            String myTypeName = myNativeClasses.nameFromType(myType);

            if (realTypeName.equals(myTypeName)) {
                evaluation.addTruePositive(depth + 1, "wrong native type, real:" + realTypeName + ", my:" + myTypeName);
            } else {
                evaluation.addFalseNegative(depth + 1, "wrong native type, real:" + realTypeName + ", my:" + myTypeName);
            }
            callback.run();
            return;
        }


        EvaluationVisitor visitor = new EvaluationVisitor(depth + 1, evaluation, queue, nativeTypesInReal, realNativeClasses, myNativeClasses, seen);
        realType.accept(visitor, new Arg(myType, callback));
    }

    @Override
    public Void visit(AnonymousType real, Arg arg) {
        throw new RuntimeException();
    }

    @Override
    public Void visit(ClassType real, Arg arg) {
        throw new RuntimeException();
    }

    @Override
    public Void visit(GenericType real, Arg arg) {
        return real.toInterface().accept(this, arg);
    }

    @Override
    public Void visit(InterfaceType real, Arg arg) {
        if (real.getDeclaredCallSignatures().isEmpty() && real.getDeclaredConstructSignatures().isEmpty() && real.getDeclaredProperties().isEmpty() && real.getDeclaredNumberIndexType() == null && real.getDeclaredStringIndexType() == null) {
            // Empty interface, this happens when we have a type constraint like Array<T> (where this empty interface is the T).
            // TODO: Do something special here?
        }



        Type type = arg.type;
        if (type instanceof TypeParameterType) {
            type = ((TypeParameterType) type).getConstraint();
        }
        if (type instanceof ReferenceType) {
            type = ((ReferenceType) type).getTarget();
        }
        if (type instanceof GenericType) {
            type = ((GenericType) type).toInterface();
        }

        if (type instanceof SimpleType) {
            evaluation.addFalseNegative(depth, "got simple type, expected interface");
            arg.callback.run();
            return null;
        }

        if (!(type instanceof InterfaceType)) {
            throw new RuntimeException();
        }
        evaluation.addTruePositive(depth, "this was an interface, that is right.");
        InterfaceType my = (InterfaceType) type;

        // Properties
        Map<String, Type> realProperties = real.getDeclaredProperties();
        Map<String, Type> myProperties = my.getDeclaredProperties();

        Set<String> properties = new HashSet<>();
        properties.addAll(realProperties.keySet());
        properties.addAll(myProperties.keySet());

        WhenAllDone whenAllDone = new WhenAllDone(arg.callback, queue);
        for (String property : properties) {
            if (!myProperties.containsKey(property)) {
                evaluation.addFalseNegative(depth + 1, "property missing: " + property);
            } else if (!realProperties.containsKey(property)) {
                evaluation.addFalsePositive(depth + 1, "excess property: " + property);
            } else {
                Type myType = myProperties.get(property);
                Type realType = realProperties.get(property);
                nextDepth(realType, myType, whenAllDone.newSubCallback(1));
            }
        }

        // Functions
        evaluateFunctions(real.getDeclaredCallSignatures(), ((InterfaceType) type).getDeclaredCallSignatures(), whenAllDone.newSubCallback(4));
        evaluateFunctions(real.getDeclaredConstructSignatures(), ((InterfaceType) type).getDeclaredConstructSignatures(), whenAllDone.newSubCallback(5));


        // Indexers:
        Type realNumber = real.getDeclaredNumberIndexType();
        Type myNumber = my.getDeclaredNumberIndexType();
        evaluateIndexers(realNumber, myNumber, whenAllDone.newSubCallback(2));

        Type realString = real.getDeclaredNumberIndexType();
        Type myString = my.getDeclaredNumberIndexType();
        evaluateIndexers(realString, myString, whenAllDone.newSubCallback(3));

        return null;
    }

    private void evaluateFunctions(List<Signature> realSignatures, List<Signature> mySignatures, Runnable callback) {
        if (realSignatures == null) {
            realSignatures = Collections.EMPTY_LIST;
        }
        if (mySignatures == null) {
            mySignatures = Collections.EMPTY_LIST;
        }
        if (realSignatures.isEmpty() && mySignatures.isEmpty()) {
            callback.run();
            return;
        }
        if (realSignatures.isEmpty() && !mySignatures.isEmpty()) {
            evaluation.addFalsePositive(depth, "shouldn't be a function");
            callback.run();
            return;
        }
        if (!realSignatures.isEmpty() && mySignatures.isEmpty()) {
            evaluation.addFalseNegative(depth, "should be a function");
            callback.run();
            return;
        }
        evaluation.addTruePositive(depth, "was a function, that was right");
        if (realSignatures.size() == 1 && mySignatures.size() == 1) {
            WhenAllDone whenAllDone = new WhenAllDone(callback, queue);
            Signature realSignature = realSignatures.get(0);
            Signature mySignature = mySignatures.get(0);
            nextDepth(realSignature.getResolvedReturnType(), mySignature.getResolvedReturnType(), whenAllDone.newSubCallback(6));

            for (int i = 0; i < Math.max(realSignature.getParameters().size(), mySignature.getParameters().size()); i++) {
                if (i >= realSignature.getParameters().size()) {
                    evaluation.addFalsePositive(depth + 1, "to many arguments for function");
                } else if (i >= mySignature.getParameters().size()) {
                    evaluation.addFalseNegative(depth + 1, "to few arguments for function");
                } else {
                    nextDepth(realSignature.getParameters().get(i).getType(), mySignature.getParameters().get(i).getType(), whenAllDone.newSubCallback(7));
                }
            }

        } else {
            UnionType realUnion = new UnionType();
            //noinspection Duplicates
            realUnion.setElements(realSignatures.stream().map(sig -> {
                InterfaceType inter = new InterfaceType();
                inter.setDeclaredProperties(Collections.EMPTY_MAP);
                inter.setDeclaredCallSignatures(Arrays.asList(sig));
                return inter;
            }).collect(Collectors.toList()));

            UnionType myUnion = new UnionType();
            //noinspection Duplicates
            myUnion.setElements(mySignatures.stream().map(sig -> {
                InterfaceType inter = new InterfaceType();
                inter.setDeclaredProperties(Collections.EMPTY_MAP);
                inter.setDeclaredCallSignatures(Arrays.asList(sig));
                return inter;
            }).collect(Collectors.toList()));

            EvaluationVisitor visitor = new EvaluationVisitor(depth - 1, evaluation, queue, nativeTypesInReal, realNativeClasses, myNativeClasses, seen);
            visitor.nextDepth(realUnion, myUnion, callback);
        }
    }


    private void evaluateIndexers(Type realType, Type myType, Runnable callback) {
        if (realType != null && myType == null) {
            evaluation.addFalseNegative(depth + 1, "missing indexer");
            callback.run();
        } else if (myType != null && realType == null) {
            evaluation.addFalsePositive(depth + 1, "excess indexer");
            callback.run();
        } else if (myType != null /* && realString != null */) {
            evaluation.addTruePositive(depth, "this was an indexer, that was right");
            nextDepth(realType, myType, callback);
        } else {
            callback.run();
        }
    }

    @Override
    public Void visit(ReferenceType real, Arg arg) {
        return real.getTarget().accept(this, arg); // Ignoring generic types for now.
    }

    @Override
    public Void visit(SimpleType real, Arg arg) {
        Type type = arg.type;
        if (!real.equals(type)) {
            evaluation.addFalseNegative(depth, "wrong simple type, " + real + " / " + arg);
        } else {
            evaluation.addTruePositive(depth, "right simple type: " + real);
        }
        arg.callback.run();
        return null;
    }

    @Override
    public Void visit(TupleType real, Arg arg) {
        if (arg.type instanceof ReferenceType && myNativeClasses.nameFromType(((ReferenceType) arg.type).getTarget()).equals("Array")) {
            // A tuple is an array, and since i don't handle generics, i say that an Array is good enough.
            evaluation.addTruePositive(depth, "tuple was array (and I'm happy with that)");
        } else {
            evaluation.addFalseNegative(depth, "tuple wasn't an array");
        }
        arg.callback.run();
        return null;
    }

    @Override
    public Void visit(UnionType real, Arg arg) {
        Type type = arg.type;
        if (type instanceof UnionType) {
            throw new RuntimeException();
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public Void visit(UnresolvedType real, Arg arg) {
        throw new RuntimeException();
    }

    @Override
    public Void visit(TypeParameterType real, Arg arg) {
        Type type = arg.type;
        if (!(type instanceof TypeParameterType)) {
            real.getConstraint().accept(this, arg);
            return null;
        }
        assert ((TypeParameterType) type).getTarget() == null;
        real.getConstraint().accept(this, new Arg(((TypeParameterType) type).getConstraint(), arg.callback));
        return null;
    }

    @Override
    public Void visit(SymbolType real, Arg arg) {
        throw new RuntimeException();
    }
}
