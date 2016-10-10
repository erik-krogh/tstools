package dk.webbies.tscreate.evaluation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.au.cs.casa.typescript.SpecReader;
import dk.au.cs.casa.typescript.types.*;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.evaluation.descriptions.Description;
import dk.webbies.tscreate.util.Pair;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private Set<Type> classInstanceTypes;

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
            Set<Type> classInstanceTypes) {
        this.depth = depth;
        this.evaluation = evaluation;
        this.queue = queue;
        this.nativeTypesInReal = nativeTypesInReal;
        this.realNativeClasses = realNativeClasses;
        this.myNativeClasses = myNativeClasses;
        this.emptyNativeClasses = emptyNativeClasses;
        this.seen = seen;
        this.options = options;
        this.classInstanceTypes = classInstanceTypes;
    }


    private EvaluationVisitor moreDepth(int extraDepth) {
        return new EvaluationVisitor(this.depth + extraDepth, this.evaluation, this.queue, this.nativeTypesInReal, this.realNativeClasses, this.myNativeClasses, this.emptyNativeClasses, this.seen, this.options, this.classInstanceTypes);
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
    private void nextDepth(Type realType, Type myType, Runnable callback, String prefix, int extraDepth) {
        String myTypeName = myNativeClasses.nameFromType(myType);
        String realTypeName = realNativeClasses.nameFromType(realType);
        if (myTypeName != null && realTypeName != null && myTypeName.equals(realTypeName) && emptyNativeClasses.typeFromName(myTypeName) != null) {
            addTruePositive(prefix, depth + extraDepth, Description.RightNativeType());
            callback.run();
            return;
        }

        Runnable runNextDepth = () -> {
            if (options.maxEvaluationDepth != null) {
                if (depth > options.maxEvaluationDepth) {
                    callback.run();
                    return;
                }
            } else if (seen.contains(new Pair<>(realType, myType)) && !(realType instanceof SimpleType) && !(myType instanceof SimpleType)) {
                callback.run();
                return;
            }
            if (options.maxEvaluationDepth == null) {
                seen.add(new Pair<>(realType, myType));
            }

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
                }, prefix, extraDepth);
            } else if (myType instanceof UnionType) {
                findBest(((UnionType) myType).getElements().stream().map(elem -> new Pair<>(realType, elem)).collect(Collectors.toList()),
                        (evaluation) -> {
                            EvaluationVisitor.this.evaluation.add(evaluation);
                            callback.run();
                        }, prefix, extraDepth);
            } else if (realType instanceof UnionType) {
                findBest(((UnionType) realType).getElements().stream().map(elem -> new Pair<>(elem, myType)).collect(Collectors.toList()),
                        (evaluation) -> {
                            EvaluationVisitor.this.evaluation.add(evaluation);
                            callback.run();
                        }, prefix, extraDepth);
            } else {
                analyzeNextDepth(realType, myType, callback, prefix, extraDepth);
            }
        };
        queue.add(new EvaluationQueueElement(depth + extraDepth, runNextDepth));
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

    private void findBest(Collection<Pair<Type, Type>> typePairs, Consumer<Evaluation> callback, String prefix, int extraDepth) {
        AtomicInteger downCounter = new AtomicInteger(typePairs.size());
        List<Evaluation> evaluations = new ArrayList<>();

        for (Pair<Type, Type> typePair : typePairs) {
            Type left = typePair.left;
            Type right = typePair.right;

            Evaluation subEvaluation = Evaluation.create(options.debugPrint);
            EvaluationVisitor visitor = new EvaluationVisitor(this.depth, subEvaluation, this.queue, this.nativeTypesInReal, this.realNativeClasses, this.myNativeClasses, this.emptyNativeClasses, this.seen, this.options, this.classInstanceTypes);

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
            }, prefix, extraDepth);
        }

    }

    private void analyzeNextDepth(Type realType, Type myType, Runnable callback, String prefix, int extraDepth) {
        assert !(realType instanceof UnionType);
        assert !(myType instanceof UnionType);

        if (nativeTypesInReal.contains(realType)) {
            String realTypeName = realNativeClasses.nameFromType(realType);
            assert realTypeName != null;
            String myTypeName = myNativeClasses.nameFromType(myType);

            if (realTypeName.equals(myTypeName)) {
                addTruePositive(prefix, depth + extraDepth, Description.RightNativeType());
            } else {
                addFalseNegative(prefix, depth + extraDepth, Description.WrongNativeType());
            }
            callback.run();
            return;
        }


        EvaluationVisitor visitor = new EvaluationVisitor(this.depth + extraDepth, this.evaluation, this.queue, this.nativeTypesInReal, this.realNativeClasses, this.myNativeClasses, this.emptyNativeClasses, this.seen, this.options, this.classInstanceTypes);
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
            if (((SimpleType) type).getKind() == SimpleTypeKind.Any && options.evaluationAnyAreOK) {
                addTruePositive(arg.prefix, depth, Description.WasInterface());
            } else {
                addFalseNegative(arg.prefix, depth, Description.ExpectedInterface());
            }
            arg.callback.run();
            return null;
        }

        if (type instanceof TupleType) {
            addFalseNegative(arg.prefix, depth, Description.ExpectedInterface());
            arg.callback.run();
            return null;
        }

        if (!(type instanceof InterfaceType)) {
            throw new RuntimeException();
        }
        addTruePositive(arg.prefix, depth, Description.WasInterface());
        InterfaceType my = (InterfaceType) type;

        Collection<InterfaceType> realWithBase;
        Collection<InterfaceType> myWithBase;
        if (classInstanceTypes.contains(real) || classInstanceTypes.contains(my)) {
            realWithBase = Collections.singletonList(real);
            myWithBase = Collections.singletonList(my);
        } else {
            realWithBase = DeclarationParser.getWithBaseTypes(real);
            myWithBase = DeclarationParser.getWithBaseTypes(my);
        }


        // Properties
        Multimap<String, Type> realProperties = getProperties(realWithBase);
        Multimap<String, Type> myProperties = getProperties(myWithBase);

        Set<String> properties = new HashSet<>();
        properties.addAll(realProperties.keySet());
        properties.addAll(myProperties.keySet());

        WhenAllDone whenAllDone = new WhenAllDone(new EvaluationQueueElement(depth, arg.callback), queue);
        for (String property : properties) {
            if (!myProperties.containsKey(property)) {
                addFalseNegative(arg.prefix + "." + property, depth + 1, Description.PropertyMissing(property));
            } else if (!realProperties.containsKey(property)) {
                if (!options.evaluationSkipExcessProperties) {
                    addFalsePositive(arg.prefix + "." + property, depth + 1, Description.ExcessProperty(property));
                }
            } else {
                addTruePositive(arg.prefix + "." + property, depth + 1, Description.RightProperty(property));
                Collection<Type> myTypes = myProperties.get(property);
                Collection<Type> realTypes = realProperties.get(property);
                Type realType = toPossibleUnion(realTypes);
                Type myType = toPossibleUnion(myTypes);
                nextDepth(realType, myType, whenAllDone.newSubCallback(), arg.prefix + "." + property, 1);
            }
        }

        // Functions
        evaluateFunctions(getSignatures(realWithBase, InterfaceType::getDeclaredCallSignatures), getSignatures(myWithBase, InterfaceType::getDeclaredCallSignatures), whenAllDone.newSubCallback(), false, arg.prefix, false);
        evaluateFunctions(getSignatures(realWithBase, InterfaceType::getDeclaredConstructSignatures), getSignatures(myWithBase, InterfaceType::getDeclaredConstructSignatures), whenAllDone.newSubCallback(), true, arg.prefix, false);


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

    private Type getPossible(Collection<InterfaceType> types, Function<InterfaceType, Type> getter) {
        for (InterfaceType type : types) {
            if (getter.apply(type) != null) {
                return getter.apply(type);
            }
        }
        return null;
    }

    private List<Signature> getSignatures(Collection<InterfaceType> types, Function<InterfaceType, List<Signature>> getter) {
        ArrayList<Signature> result = new ArrayList<>();
        for (InterfaceType type : types) {
            List<Signature> signatures = getter.apply(type);
            if (signatures != null) {
                result.addAll(signatures);
            }
        }
        return result;
    }

    private Multimap<String, Type> getProperties(Collection<InterfaceType> types) {
        Multimap<String, Type> result = ArrayListMultimap.create();
        for (InterfaceType type : types) {
            for (Map.Entry<String, Type> entry : type.getDeclaredProperties().entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    public void evaluateFunctions(List<Signature> realSignatures, List<Signature> mySignatures, Runnable callback, boolean isConstructor, String prefix, boolean skipReturn) {
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
            addFalsePositive(prefix, depth, Description.ShouldNotBeFunction(isConstructor));
            callback.run();
            return;
        }
        if (!realSignatures.isEmpty() && mySignatures.isEmpty()) {
            addFalseNegative(prefix, depth, Description.ShouldBeFunction(isConstructor));
            callback.run();
            return;
        }
        addTruePositive(prefix, depth, Description.WasFunction());
        if (realSignatures.size() == 1 && mySignatures.size() == 1) {
            WhenAllDone whenAllDone = new WhenAllDone(new EvaluationQueueElement(depth + 1, callback), queue);
            Signature realSignature = realSignatures.get(0);
            Signature mySignature = mySignatures.get(0);

            if (!skipReturn) {
                if (!(options.evaluationMethod == Options.EvaluationMethod.ONLY_HEAP)) {
                    int extraDepth = 1;
                    if (!isConstructor && options.evaluationPushFunctionReturnsDown) {
                        extraDepth = 1000;
                    }
                    nextDepth(realSignature.getResolvedReturnType(), mySignature.getResolvedReturnType(), whenAllDone.newSubCallback(), prefix + ".[" + description + "].[return]", extraDepth);
                }
            }

            if (realSignature.getParameters().size() != mySignature.getParameters().size()) {
                addFalseNegative(prefix + ".[" + description + "]", depth + 1, Description.WrongNumberOfArgs(realSignature.getParameters().size(), mySignature.getParameters().size()));
            }

            if (options.evaluationMethod != Options.EvaluationMethod.ONLY_HEAP && options.evaluateArguments) {
                for (int i = 0; i < Math.min(realSignature.getParameters().size(), mySignature.getParameters().size()); i++) {
                    int extraDepth = 1;
                    if (options.evaluationPushFunctionReturnsDown) {
                        extraDepth = 1000;
                    }
                    nextDepth(realSignature.getParameters().get(i).getType(), mySignature.getParameters().get(i).getType(), whenAllDone.newSubCallback(), prefix + ".[" + description + "].[arg" + i + "]", extraDepth);
                }
            }

            whenAllDone.newSubCallback().run(); // Making sure there is at least one

        } else {
            UnionType realUnion = new UnionType();
            //noinspection Duplicates
            realUnion.setElements(realSignatures.stream().map(sig -> {
                InterfaceType inter = new InterfaceType();
                inter.setDeclaredProperties(Collections.EMPTY_MAP);
                if (isConstructor) {
                    inter.setDeclaredConstructSignatures(Arrays.asList(sig));
                } else {
                    inter.setDeclaredCallSignatures(Arrays.asList(sig));
                }
                return inter;
            }).collect(Collectors.toList()));

            UnionType myUnion = new UnionType();
            //noinspection Duplicates
            myUnion.setElements(mySignatures.stream().map(sig -> {
                InterfaceType inter = new InterfaceType();
                inter.setDeclaredProperties(Collections.EMPTY_MAP);
                if (isConstructor) {
                    inter.setDeclaredConstructSignatures(Arrays.asList(sig));
                } else {
                    inter.setDeclaredCallSignatures(Arrays.asList(sig));
                }
                return inter;
            }).collect(Collectors.toList()));

            EvaluationVisitor visitor = new EvaluationVisitor(this.depth - 1, this.evaluation, this.queue, this.nativeTypesInReal, this.realNativeClasses, this.myNativeClasses, this.emptyNativeClasses, this.seen, this.options, this.classInstanceTypes);
            visitor.nextDepth(realUnion, myUnion, callback, prefix, 1);
        }
    }


    private void evaluateIndexers(Type realType, Type myType, Runnable callback, String prefix) {
        if (realType != null && myType == null) {
            addFalseNegative(prefix, depth + 1, Description.MissingIndexer());
            callback.run();
        } else if (myType != null && realType == null) {
            addFalsePositive(prefix, depth + 1, Description.ExcessIndexer());
            callback.run();
        } else if (myType != null /* && realString != null */) {
            addTruePositive(prefix, depth, Description.WasIndexer());
            nextDepth(realType, myType, callback, prefix + ".[indexer]", 1);
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
        if (options.evaluationAnyAreOK) {
            if (real.getKind() == SimpleTypeKind.Any || type instanceof SimpleType && ((SimpleType) type).getKind() == SimpleTypeKind.Any) {
                addTruePositive(arg.prefix, depth, Description.RightSimpleType(real));
                arg.callback.run();
                return null;
            }
        }
        if (!real.equals(type)) {
            addFalseNegative(arg.prefix, depth, Description.WrongSimpleType(real, arg.type));
        } else {
            addTruePositive(arg.prefix, depth, Description.RightSimpleType(real));
        }
        arg.callback.run();
        return null;
    }

    @Override
    public Void visit(TupleType real, Arg arg) {
        if (arg.type instanceof ReferenceType && myNativeClasses.nameFromType(((ReferenceType) arg.type).getTarget()).equals("Array")) {
            // A tuple is an array, and since i don't handle generics, i say that an Array is good enough.
            addTruePositive(arg.prefix, depth, Description.WasTuple());
        } else {
            addFalseNegative(arg.prefix, depth, Description.WasNotTuple());
        }
        arg.callback.run();
        return null;
    }

    @Override
    public Void visit(UnionType real, Arg arg) {
        nextDepth(real, arg.type, arg.callback, arg.prefix, 0);
        return null;
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
        if (arg.type instanceof SymbolType) {
            addTruePositive(arg.prefix, depth, Description.WasSymbolType());
        } else {
            addFalseNegative(arg.prefix, depth, Description.WasNotSymbolType());
        }
        arg.callback.run();
        return null;
    }

    private void addFalseNegative(String prefix, int depth, Description description) {
        assert description.getType() == Description.DescriptionType.FALSE_NEGATIVE;
        evaluation.addFalseNegative(depth, description, prefix);
    }

    private void addFalsePositive(String prefix, int depth, Description description) {
        assert description.getType() == Description.DescriptionType.FALSE_POSITIVE;
        evaluation.addFalsePositive(depth, description, prefix);
    }

    private void addTruePositive(String prefix, int depth, Description description) {
        assert description.getType() == Description.DescriptionType.TRUE_POSITIVE;
        evaluation.addTruePositive(depth, description, prefix);
    }


}
