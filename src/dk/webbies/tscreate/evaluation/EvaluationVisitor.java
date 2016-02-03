package dk.webbies.tscreate.evaluation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.au.cs.casa.typescript.SpecReader;
import dk.au.cs.casa.typescript.types.*;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.util.Pair;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;
import static dk.webbies.tscreate.evaluation.DeclarationEvaluator.EvaluationQueueElement;

/**
 * Created by Erik Krogh Kristensen on 14-12-2015.
 */
public class EvaluationVisitor implements TypeVisitorWithArgument<Void, EvaluationVisitor.Arg> {
    private final int depth;
    private final Evaluation evaluation;
    private PriorityQueue<DeclarationEvaluator.EvaluationQueueElement> queue;
    private Set<Type> nativeTypesInReal;
    private final NativeClassesMap realNativeClasses;
    private final NativeClassesMap myNativeClasses;
    private Set<Pair<Type, Type>> seen;
    private Options options;

    public EvaluationVisitor(
            int depth,
            Evaluation evaluation,
            PriorityQueue<DeclarationEvaluator.EvaluationQueueElement> queue,
            Set<Type> nativeTypesInReal,
            NativeClassesMap realNativeClasses,
            NativeClassesMap myNativeClasses,
            Set<Pair<Type, Type>> seen,
            Options options) {
        this.depth = depth;
        this.evaluation = evaluation;
        this.queue = queue;
        this.nativeTypesInReal = nativeTypesInReal;
        this.realNativeClasses = realNativeClasses;
        this.myNativeClasses = myNativeClasses;
        this.seen = seen;
        this.options = options;
    }

    static final class Arg {
        final Type type;
        final Runnable callback;
        final String prefix;

        public Arg(Type type, Runnable callback, String prefix) {
            this.type = type;
            this.callback = callback;
            this.prefix = prefix;
        }
    }

    private static int runCounter = 0;
    private void nextDepth(Type realType, Type myType, Runnable callback, String prefix) {
        if (options.maxEvaluationDepth != null) {
            if (depth > options.maxEvaluationDepth) {
                callback.run();
                return;
            }
        } else if (seen.contains(new Pair<>(realType, myType))) {
            callback.run();
            return;
        }
        if (options.maxEvaluationDepth == null) {
            seen.add(new Pair<>(realType, myType));
        }

        Runnable runNextDepth = () -> {
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
                }, prefix);
            } else if (myType instanceof UnionType) {
                findBest(((UnionType) myType).getElements().stream().map(elem -> new Pair<>(realType, elem)).collect(Collectors.toList()),
                        (evaluation) -> {
                            EvaluationVisitor.this.evaluation.add(evaluation);
                            callback.run();
                        }, prefix);
            } else if (realType instanceof UnionType) {
                findBest(((UnionType) realType).getElements().stream().map(elem -> new Pair<>(elem, myType)).collect(Collectors.toList()),
                        (evaluation) -> {
                            EvaluationVisitor.this.evaluation.add(evaluation);
                            callback.run();
                        }, prefix);
            } else {
                analyzeNextDepth(realType, myType, callback, prefix);
            }
        };
        queue.add(new EvaluationQueueElement(depth, runNextDepth));
    }



    private InterfaceType getCombinedInterface(UnionType realType) {
        Set<InterfaceType> withBases = realType.getElements().stream().map(DeclarationParser::getWithBaseTypes).reduce(new HashSet<>(), Util::reduceSet);
        if (withBases.isEmpty()) {
            return null;
        }
        InterfaceType result = SpecReader.makeEmptySyntheticInterfaceType();

        result.setDeclaredCallSignatures(getSignatures(withBases, InterfaceType::getDeclaredCallSignatures));
        result.setDeclaredConstructSignatures(getSignatures(withBases, InterfaceType::getDeclaredConstructSignatures));
        Multimap<String, Type> properties = getProperties(withBases);
        for (Map.Entry<String, Collection<Type>> entry : properties.asMap().entrySet()) {
            if (entry.getKey().length() == 1) {
                result.getDeclaredProperties().put(entry.getKey(), entry.getValue().iterator().next());
            } else {
                UnionType unionType = new UnionType();
                unionType.setElements(entry.getValue().stream().collect(Collectors.toList()));
                result.getDeclaredProperties().put(entry.getKey(), unionType);
            }
        }

        // Base-types are not relevant.

        for (InterfaceType type : withBases) {
            if (type.getTypeParameters() != null) {
                result.getTypeParameters().addAll(type.getTypeParameters());
            }
        }

        result.setDeclaredNumberIndexType(getPossible(withBases, InterfaceType::getDeclaredNumberIndexType));
        result.setDeclaredStringIndexType(getPossible(withBases, InterfaceType::getDeclaredStringIndexType));

        return result;
    }

    private void findBest(Collection<Pair<Type, Type>> typePairs, Consumer<Evaluation> callback, String prefix) {
        AtomicInteger downCounter = new AtomicInteger(typePairs.size());
        List<Evaluation> evaluations = new ArrayList<>();

        for (Pair<Type, Type> typePair : typePairs) {
            Type left = typePair.first;
            Type right = typePair.second;

            Evaluation subEvaluation = new Evaluation(options.debugPrint);
            EvaluationVisitor visitor = new EvaluationVisitor(depth, subEvaluation, queue, nativeTypesInReal, realNativeClasses, myNativeClasses, seen, options);

            visitor.nextDepth(left, right, () -> {
                evaluations.add(subEvaluation);
                int i = downCounter.decrementAndGet();
                if (i == 0) {
                    Evaluation bestEvaluation = Collections.max(evaluations, (o1, o2) -> {
                        for (int depth = 0; depth <= Math.max(o1.maxDepth, o2.maxDepth); depth++) {
                            if ((o1.getTruePositives(depth) == 0 || o2.getTruePositives(depth) == 0) && o1.getTruePositives(depth) != o2.getTruePositives(depth)) {
                                return Integer.compare(o1.getTruePositives(depth), o2.getTruePositives(depth));
                            }

                            double o1Prec = o1.fMeasure(depth);
                            double o2Prec = o2.fMeasure(depth);
                            int compared = Double.compare(o1Prec, o2Prec);
                            if (compared != 0) {
                                return compared;
                            }
                        }
                        return 0;
                    });
                    callback.accept(bestEvaluation);
                }
            }, prefix);
        }

    }

    private void analyzeNextDepth(Type realType, Type myType, Runnable callback, String prefix) {
        assert !(realType instanceof UnionType);
        assert !(myType instanceof UnionType);

        if (nativeTypesInReal.contains(realType)) {
            String realTypeName = realNativeClasses.nameFromType(realType);
            assert realTypeName != null;
            String myTypeName = myNativeClasses.nameFromType(myType);

            if (realTypeName.equals(myTypeName)) {
                evaluation.addTruePositive(depth + 1, "wrong native type", prefix);
            } else {
                evaluation.addFalseNegative(depth + 1, "wrong native type", prefix);
            }
            callback.run();
            return;
        }


        EvaluationVisitor visitor = new EvaluationVisitor(depth + 1, evaluation, queue, nativeTypesInReal, realNativeClasses, myNativeClasses, seen, options);
        realType.accept(visitor, new Arg(myType, callback, prefix));
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
            // We don't count this, not positively, not negatively, it is just ignored.
            arg.callback.run();
            return null;
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
            evaluation.addFalseNegative(depth, "got simple type, expected interface", arg.prefix);
            arg.callback.run();
            return null;
        }

        if (!(type instanceof InterfaceType)) {
            throw new RuntimeException();
        }
        evaluation.addTruePositive(depth, "this was an interface, that is right.", arg.prefix);
        InterfaceType my = (InterfaceType) type;

        Set<InterfaceType> realWithBase = DeclarationParser.getWithBaseTypes(real);
        Set<InterfaceType> myWithBase = DeclarationParser.getWithBaseTypes(my);

        // Properties
        Multimap<String, Type> realProperties = getProperties(realWithBase);
        Multimap<String, Type> myProperties = getProperties(myWithBase);

        Set<String> properties = new HashSet<>();
        properties.addAll(realProperties.keySet());
        properties.addAll(myProperties.keySet());

        WhenAllDone whenAllDone = new WhenAllDone(new EvaluationQueueElement(depth, arg.callback), queue);
        for (String property : properties) {
            if (!myProperties.containsKey(property)) {
                evaluation.addFalseNegative(depth + 1, "property missing: " + property, arg.prefix);
            } else if (!realProperties.containsKey(property)) {
                evaluation.addFalsePositive(depth + 1, "excess property: " + property, arg.prefix);
            } else {
                Collection<Type> myTypes = myProperties.get(property);
                Collection<Type> realTypes = realProperties.get(property);
                Type realType = toPossibleUnion(realTypes);
                Type myType = toPossibleUnion(myTypes);
                nextDepth(realType, myType, whenAllDone.newSubCallback(), arg.prefix + "." + property);
            }
        }

        // Functions
        evaluateFunctions(getSignatures(realWithBase, InterfaceType::getDeclaredCallSignatures), getSignatures(myWithBase, InterfaceType::getDeclaredCallSignatures), whenAllDone.newSubCallback(), real, arg.type, "function", arg.prefix);
        evaluateFunctions(getSignatures(realWithBase, InterfaceType::getDeclaredConstructSignatures), getSignatures(myWithBase, InterfaceType::getDeclaredConstructSignatures), whenAllDone.newSubCallback(), real, arg.type, "constructor", arg.prefix);


        // Indexers:
        Type realNumber = getPossible(realWithBase, InterfaceType::getDeclaredNumberIndexType);
        Type myNumber = getPossible(myWithBase, InterfaceType::getDeclaredNumberIndexType);
        evaluateIndexers(realNumber, myNumber, whenAllDone.newSubCallback(), arg.prefix);

        Type realString = getPossible(realWithBase, InterfaceType::getDeclaredStringIndexType);
        Type myString = getPossible(myWithBase, InterfaceType::getDeclaredStringIndexType);
        evaluateIndexers(realString, myString, whenAllDone.newSubCallback(), arg.prefix);

        return null;
    }

    private Type toPossibleUnion(Collection<Type> myTypes) {
        assert myTypes.size() != 0;
        if (myTypes.size() == 1) {
            return myTypes.iterator().next();
        } else {
            UnionType result = new UnionType();
            result.setElements(myTypes.stream().collect(Collectors.toList()));
            InterfaceType combined = getCombinedInterface(result);
            if (combined != null) {
                result.getElements().add(combined);
            }
            return result;
        }
    }

    private Type getPossible(Set<InterfaceType> types, Function<InterfaceType, Type> getter) {
        for (InterfaceType type : types) {
            if (getter.apply(type) != null) {
                return getter.apply(type);
            }
        }
        return null;
    }

    private List<Signature> getSignatures(Set<InterfaceType> types, Function<InterfaceType, List<Signature>> getter) {
        ArrayList<Signature> result = new ArrayList<>();
        for (InterfaceType type : types) {
            List<Signature> signatures = getter.apply(type);
            if (signatures != null) {
                result.addAll(signatures);
            }
        }
        return result;
    }

    private Multimap<String, Type> getProperties(Set<InterfaceType> types) {
        Multimap<String, Type> result = ArrayListMultimap.create();
        for (InterfaceType type : types) {
            for (Map.Entry<String, Type> entry : type.getDeclaredProperties().entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    private void evaluateFunctions(List<Signature> realSignatures, List<Signature> mySignatures, Runnable callback, Type realType, Type myType, String description, String prefix) {
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
            evaluation.addFalsePositive(depth + 1, "shouldn't be a " + description, prefix);
            callback.run();
            return;
        }
        if (!realSignatures.isEmpty() && mySignatures.isEmpty()) {
            evaluation.addFalseNegative(depth + 1, "should be a " + description, prefix);
            callback.run();
            return;
        }
        evaluation.addTruePositive(depth + 1, "was a function, that was right", prefix);
        if (realSignatures.size() == 1 && mySignatures.size() == 1) {
            WhenAllDone whenAllDone = new WhenAllDone(new EvaluationQueueElement(depth + 1, callback), queue);
            Signature realSignature = realSignatures.get(0);
            Signature mySignature = mySignatures.get(0);
            nextDepth(realSignature.getResolvedReturnType(), mySignature.getResolvedReturnType(), whenAllDone.newSubCallback(), prefix + ".[" + description + "].[return]");

            for (int i = 0; i < Math.max(realSignature.getParameters().size(), mySignature.getParameters().size()); i++) {
                if (i >= realSignature.getParameters().size()) {
                    evaluation.addFalsePositive(depth + 1, "to many arguments for function", prefix + ".[" + description + "]");
                } else if (i >= mySignature.getParameters().size()) {
                    evaluation.addFalseNegative(depth + 1, "to few arguments for function", prefix + ".[" + description + "]");
                } else {
                    nextDepth(realSignature.getParameters().get(i).getType(), mySignature.getParameters().get(i).getType(), whenAllDone.newSubCallback(), prefix + ".[" + description + "].[arg" + i + "]");
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

            EvaluationVisitor visitor = new EvaluationVisitor(depth - 1, evaluation, queue, nativeTypesInReal, realNativeClasses, myNativeClasses, seen, options);
            visitor.nextDepth(realUnion, myUnion, callback, prefix);
        }
    }


    private void evaluateIndexers(Type realType, Type myType, Runnable callback, String prefix) {
        if (realType != null && myType == null) {
            evaluation.addFalseNegative(depth + 1, "missing indexer", prefix);
            callback.run();
        } else if (myType != null && realType == null) {
            evaluation.addFalsePositive(depth + 1, "excess indexer", prefix);
            callback.run();
        } else if (myType != null /* && realString != null */) {
            evaluation.addTruePositive(depth, "this was an indexer, that was right", prefix);
            nextDepth(realType, myType, callback, prefix + ".[indexer]");
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
            evaluation.addFalseNegative(depth, "wrong simple type, " + real + " / " + arg.type, arg.prefix);
        } else {
            evaluation.addTruePositive(depth, "right simple type: " + real, arg.prefix);
        }
        arg.callback.run();
        return null;
    }

    @Override
    public Void visit(TupleType real, Arg arg) {
        if (arg.type instanceof ReferenceType && myNativeClasses.nameFromType(((ReferenceType) arg.type).getTarget()).equals("Array")) {
            // A tuple is an array, and since i don't handle generics, i say that an Array is good enough.
            evaluation.addTruePositive(depth, "tuple was array (and I'm happy with that)", arg.prefix);
        } else {
            evaluation.addFalseNegative(depth, "tuple wasn't an array", arg.prefix);
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
        real.getConstraint().accept(this, new Arg(((TypeParameterType) type).getConstraint(), arg.callback, arg.prefix));
        return null;
    }

    @Override
    public Void visit(SymbolType real, Arg arg) {
        throw new RuntimeException();
    }
}
