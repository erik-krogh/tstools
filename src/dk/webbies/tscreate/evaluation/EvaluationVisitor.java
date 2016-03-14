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
    private NativeClassesMap emptyNativeClasses;
    private Set<Pair<Type, Type>> seen;
    private Options options;
    private final boolean recordEvaluation; // If false, then the evaluation doesn't record anything. Used to only record things that are below a function-argument/return.

    public EvaluationVisitor(
            int depth,
            Evaluation evaluation,
            PriorityQueue<EvaluationQueueElement> queue,
            Set<Type> nativeTypesInReal,
            NativeClassesMap realNativeClasses,
            NativeClassesMap myNativeClasses,
            NativeClassesMap emptyNativeClasses,
            Set<Pair<Type, Type>> seen,
            Options options,
            boolean recordEvaluation) {
        this.depth = depth;
        this.evaluation = evaluation;
        this.queue = queue;
        this.nativeTypesInReal = nativeTypesInReal;
        this.realNativeClasses = realNativeClasses;
        this.myNativeClasses = myNativeClasses;
        this.emptyNativeClasses = emptyNativeClasses;
        this.seen = seen;
        this.options = options;
        this.recordEvaluation = recordEvaluation;
    }

    private EvaluationVisitor withRecordEvaluation() {
        return new EvaluationVisitor(this.depth, this.evaluation, this.queue, this.nativeTypesInReal, this.realNativeClasses, this.myNativeClasses, this.emptyNativeClasses, this.seen, this.options, true);
    }

    private EvaluationVisitor moreDepth(int extraDepth) {
        return new EvaluationVisitor(this.depth + extraDepth, this.evaluation, this.queue, this.nativeTypesInReal, this.realNativeClasses, this.myNativeClasses, this.emptyNativeClasses, this.seen, this.options, this.recordEvaluation);
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
        String myTypeName = myNativeClasses.nameFromType(myType);
        String realTypeName = realNativeClasses.nameFromType(realType);
        if (myTypeName != null && realTypeName != null && myTypeName.equals(realTypeName) && emptyNativeClasses.typeFromName(myTypeName) != null) {
            addTruePositive(prefix, depth, "was native type of " + myTypeName + " and that was correct.");
            callback.run();
            return;
        }


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
            EvaluationVisitor visitor = new EvaluationVisitor(depth, subEvaluation, queue, nativeTypesInReal, realNativeClasses, myNativeClasses, emptyNativeClasses, seen, options, this.recordEvaluation);

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
                addTruePositive(prefix, depth + 1, "wrong native type");
            } else {
                addFalseNegative(depth + 1, "wrong native type", prefix);
            }
            callback.run();
            return;
        }


        EvaluationVisitor visitor = new EvaluationVisitor(depth + 1, evaluation, queue, nativeTypesInReal, realNativeClasses, myNativeClasses, emptyNativeClasses, seen, options, this.recordEvaluation);
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
            addFalseNegative(depth, "got simple type, expected interface", arg.prefix);
            arg.callback.run();
            return null;
        }

        if (type instanceof TupleType) {
            addFalseNegative(depth, "got tuple type, expected interface", arg.prefix);
            arg.callback.run();
            return null;
        }

        if (!(type instanceof InterfaceType)) {
            throw new RuntimeException();
        }
        addTruePositive(arg.prefix, depth, "this was an interface, that is right.");
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
                addFalseNegative(depth + 1, "property missing: " + property, arg.prefix);
            } else if (!realProperties.containsKey(property)) {
                if (!options.evaluationSkipExcessProperties) {
                    addFalsePositive(arg.prefix, depth + 1, "excess property: " + property);
                }
            } else {
                addTruePositive(arg.prefix, depth + 1, "property " + property + " was there, which was correct");
                Collection<Type> myTypes = myProperties.get(property);
                Collection<Type> realTypes = realProperties.get(property);
                Type realType = toPossibleUnion(realTypes);
                Type myType = toPossibleUnion(myTypes);
                moreDepth(1).nextDepth(realType, myType, whenAllDone.newSubCallback(), arg.prefix + "." + property);
            }
        }

        // Functions
        evaluateFunctions(getSignatures(realWithBase, InterfaceType::getDeclaredCallSignatures), getSignatures(myWithBase, InterfaceType::getDeclaredCallSignatures), whenAllDone.newSubCallback(), real, arg.type, false, arg.prefix);
        evaluateFunctions(getSignatures(realWithBase, InterfaceType::getDeclaredConstructSignatures), getSignatures(myWithBase, InterfaceType::getDeclaredConstructSignatures), whenAllDone.newSubCallback(), real, arg.type, true, arg.prefix);


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

    private void evaluateFunctions(List<Signature> realSignatures, List<Signature> mySignatures, Runnable callback, Type realType, Type myType, boolean isConstructor, String prefix) {
        String description = isConstructor ? "constructor" : "function";
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
            addFalsePositive(prefix, depth, "shouldn't be a " + description);
            callback.run();
            return;
        }
        if (!realSignatures.isEmpty() && mySignatures.isEmpty()) {
            addFalseNegative(depth, "should be a " + description, prefix);
            callback.run();
            return;
        }
        addTruePositive(prefix, depth, "was a function, that was right");
        if (realSignatures.size() == 1 && mySignatures.size() == 1) {
            WhenAllDone whenAllDone = new WhenAllDone(new EvaluationQueueElement(depth + 1, callback), queue);
            Signature realSignature = realSignatures.get(0);
            Signature mySignature = mySignatures.get(0);

            if (isConstructor) {
                nextDepth(realSignature.getResolvedReturnType(), mySignature.getResolvedReturnType(), whenAllDone.newSubCallback(), prefix + ".[" + description + "].[return]");
            } else {
                withRecordEvaluation().nextDepth(realSignature.getResolvedReturnType(), mySignature.getResolvedReturnType(), whenAllDone.newSubCallback(), prefix + ".[" + description + "].[return]");
            }

            for (int i = 0; i < Math.max(realSignature.getParameters().size(), mySignature.getParameters().size()); i++) {
                if (i >= realSignature.getParameters().size()) {
                    addFalsePositive(prefix + ".[" + description + "]", depth + 1, "to many arguments for function");
                } else if (i >= mySignature.getParameters().size()) {
                    addFalseNegative(depth + 1, "to few arguments for function", prefix + ".[" + description + "]");
                } else {
                    withRecordEvaluation().nextDepth(realSignature.getParameters().get(i).getType(), mySignature.getParameters().get(i).getType(), whenAllDone.newSubCallback(), prefix + ".[" + description + "].[arg" + i + "]");
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

            EvaluationVisitor visitor = new EvaluationVisitor(depth - 1, evaluation, queue, nativeTypesInReal, realNativeClasses, myNativeClasses, emptyNativeClasses, seen, options, this.recordEvaluation);
            visitor.nextDepth(realUnion, myUnion, callback, prefix);
        }
    }


    private void evaluateIndexers(Type realType, Type myType, Runnable callback, String prefix) {
        if (realType != null && myType == null) {
            addFalseNegative(depth + 1, "missing indexer", prefix);
            callback.run();
        } else if (myType != null && realType == null) {
            addFalsePositive(prefix, depth + 1, "excess indexer");
            callback.run();
        } else if (myType != null /* && realString != null */) {
            addTruePositive(prefix, depth, "this was an indexer, that was right");
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
            addFalseNegative(depth, "wrong simple type, " + real + " / " + arg.type, arg.prefix);
        } else {
            addTruePositive(arg.prefix, depth, "right simple type: " + real);
        }
        arg.callback.run();
        return null;
    }

    @Override
    public Void visit(TupleType real, Arg arg) {
        if (arg.type instanceof ReferenceType && myNativeClasses.nameFromType(((ReferenceType) arg.type).getTarget()).equals("Array")) {
            // A tuple is an array, and since i don't handle generics, i say that an Array is good enough.
            addTruePositive(arg.prefix, depth, "tuple was array (and I'm happy with that)");
        } else {
            addFalseNegative(depth, "tuple wasn't an array", arg.prefix);
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

    private void addFalseNegative(int depth, String description, String prefix) {
        if (recordEvaluation) {
            evaluation.addFalseNegative(depth, description, prefix);
        }
    }

    private void addFalsePositive(String prefix, int depth, String description) {
        if (recordEvaluation) {
            evaluation.addFalsePositive(depth, description, prefix);
        }
    }

    private void addTruePositive(String prefix, int depth, String description) {
        if (recordEvaluation) {
            evaluation.addTruePositive(depth, description, prefix);
        }
    }
}
