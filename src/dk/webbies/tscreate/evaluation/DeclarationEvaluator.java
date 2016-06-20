package dk.webbies.tscreate.evaluation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.au.cs.casa.typescript.SpecReader;
import dk.au.cs.casa.typescript.types.*;
import dk.webbies.tscreate.BenchMark;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static dk.webbies.tscreate.declarationReader.DeclarationParser.*;

/**
 * Created by Erik Krogh Kristensen on 05-10-2015.
 */
public class DeclarationEvaluator {
    private final Evaluation evaluation;
    private Snap.Obj global;
    private Map<Snap.Obj, LibraryClass> libraryClasses;

    public DeclarationEvaluator(String resultFilePath, BenchMark benchMark, Snap.Obj global, Map<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj emptySnap) {
        this.global = global;
        this.libraryClasses = libraryClasses;
        ParsedDeclaration parsedDeclaration = new ParsedDeclaration(resultFilePath, benchMark, global, libraryClasses, emptySnap).invoke();
        InterfaceType realDeclaration = parsedDeclaration.getRealDeclaration();
        InterfaceType myDeclaration = parsedDeclaration.getMyDeclaration();
        Set<String> properties = parsedDeclaration.getProperties();
        Set<Type> nativeTypesInReal = parsedDeclaration.getNativeTypesInReal();

        // Globally variables of type "any", are really just modules, that contain nothing but interfaces (in other words, they don't exist. )
        Iterator<Type> realGlobalIterator = realDeclaration.getDeclaredProperties().values().iterator();
        while (realGlobalIterator.hasNext()) {
            Type next = realGlobalIterator.next();
            if (next instanceof SimpleType && ((SimpleType) next).getKind() == SimpleTypeKind.Any) {
                realGlobalIterator.remove();
            }
        }


        realDeclaration.getDeclaredProperties().keySet().retainAll(properties);
        myDeclaration.getDeclaredProperties().keySet().retainAll(properties);


        switch (options.evaluationMethod) {
            case EVERYTHING:
                this.evaluation = getEvaluation(options, realDeclaration, myDeclaration, nativeTypesInReal, parsedDeclaration.getRealNativeClasses(), parsedDeclaration.getMyNativeClasses(), parsedDeclaration.getEmptyNativeClasses());
                break;
            case ONLY_FUNCTIONS:
                this.evaluation = evaluateStartingFromFunctions(options, realDeclaration, myDeclaration, nativeTypesInReal, parsedDeclaration.getRealNativeClasses(), parsedDeclaration.getMyNativeClasses(), parsedDeclaration.getEmptyNativeClasses());
                break;
            case ONLY_HEAP:
                this.evaluation = getEvaluation(options, realDeclaration, myDeclaration, nativeTypesInReal, parsedDeclaration.getRealNativeClasses(), parsedDeclaration.getMyNativeClasses(), parsedDeclaration.getEmptyNativeClasses());
                break;
            default:
                throw new RuntimeException();
        }
    }

    public DeclarationEvaluator(Collection<String> resultFilePaths, BenchMark benchMark, Snap.Obj globalObject, Map<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj emptySnap) {
        assert options.evaluationMethod == Options.EvaluationMethod.ONLY_FUNCTIONS;
        this.global = globalObject;
        this.libraryClasses = libraryClasses;

        Multimap<String, Evaluation> evaluations = ArrayListMultimap.create();

        for (String resultFilePath : resultFilePaths) {
            ParsedDeclaration parsedDeclaration = new ParsedDeclaration(resultFilePath, benchMark, global, libraryClasses, emptySnap).invoke();
            InterfaceType realDeclaration = parsedDeclaration.getRealDeclaration();
            InterfaceType myDeclaration = parsedDeclaration.getMyDeclaration();
            Set<String> properties = parsedDeclaration.getProperties();
            Set<Type> nativeTypesInReal = parsedDeclaration.getNativeTypesInReal();

            // Globally variables of type "any", are really just modules, that contain nothing but interfaces (in other words, they don't exist. )
            Iterator<Type> realGlobalIterator = realDeclaration.getDeclaredProperties().values().iterator();
            while (realGlobalIterator.hasNext()) {
                Type next = realGlobalIterator.next();
                if (next instanceof SimpleType && ((SimpleType) next).getKind() == SimpleTypeKind.Any) {
                    realGlobalIterator.remove();
                }
            }


            realDeclaration.getDeclaredProperties().keySet().retainAll(properties);
            myDeclaration.getDeclaredProperties().keySet().retainAll(properties);

            Map<String, Evaluation> functionEvaluations = getFunctionEvaluations(options, realDeclaration, myDeclaration, nativeTypesInReal, parsedDeclaration.getRealNativeClasses(), parsedDeclaration.getMyNativeClasses(), parsedDeclaration.getEmptyNativeClasses());

            functionEvaluations.entrySet().stream().forEach(entry -> evaluations.put(entry.getKey(), entry.getValue()));
        }

        Evaluation evaluation = Evaluation.create(options.debugPrint);

        for (Map.Entry<String, Collection<Evaluation>> entry : evaluations.asMap().entrySet()) {
            Evaluation functionEval = entry.getValue().stream().max((a, b) -> Double.compare(a.score().fMeasure, b.score().fMeasure)).get();
            evaluation.add(functionEval);
        }

        this.evaluation = evaluation;

    }

    public static Evaluation getEvaluation(Options options, Type realDeclaration, Type myDeclaration, Set<Type> nativeTypesInReal, NativeClassesMap realNativeClasses, NativeClassesMap myNativeClasses, NativeClassesMap emptyNativeClasses) {
        PriorityQueue<EvaluationQueueElement> queue = new PriorityQueue<>();
        AtomicBoolean hasRun = new AtomicBoolean(false);
        Runnable doneCallback = () -> {
            assert queue.isEmpty();
            hasRun.set(true);
        };

        Set<Type> classInstanceTypes = ClassFinder.getClassInstanceTypes(myDeclaration, realDeclaration);

        Evaluation evaluation = Evaluation.create(options.debugPrint);
        queue.add(new EvaluationQueueElement(0, () -> {
            EvaluationVisitor visitor = new EvaluationVisitor(0, evaluation, queue, nativeTypesInReal, realNativeClasses, myNativeClasses, emptyNativeClasses, new HashSet<>(), options, classInstanceTypes);
            realDeclaration.accept(visitor, new EvaluationVisitor.Arg(myDeclaration, doneCallback, "window"));
        }));

        while (!queue.isEmpty()) {
            EvaluationQueueElement element = queue.poll();
            element.runnable.run();
        }

        assert hasRun.get();
        return evaluation;
    }

    public static final class FunctionToEvaluate {
        final Set<Signature> myFunc;
        final Set<Signature> realFunc;
        final String path;

        public FunctionToEvaluate(Collection<Signature> myFunc, Collection<Signature> realFunc, String path) {
            this.myFunc = new HashSet<>(myFunc);
            this.realFunc = new HashSet<>(realFunc);
            this.path = path;
        }
    }

    public static Evaluation evaluateStartingFromFunctions(Options options, Type realDeclaration, Type myDeclaration, Set<Type> nativeTypesInReal, NativeClassesMap realNativeClasses, NativeClassesMap myNativeClasses, NativeClassesMap emptyNativeClasses) {
        Map<String, Evaluation> evaluations = getFunctionEvaluations(options, realDeclaration, myDeclaration, nativeTypesInReal, realNativeClasses, myNativeClasses, emptyNativeClasses);

        Evaluation evaluation = Evaluation.create(options.debugPrint);

        evaluations.values().forEach(evaluation::add);

        return evaluation;
    }

    private static Map<String, Evaluation> getFunctionEvaluations(Options options, Type realDeclaration, Type myDeclaration, Set<Type> nativeTypesInReal, NativeClassesMap realNativeClasses, NativeClassesMap myNativeClasses, NativeClassesMap emptyNativeClasses) {
        PriorityQueue<EvaluationQueueElement> queue = new PriorityQueue<>();
        AtomicBoolean hasRun = new AtomicBoolean(false);
        WhenAllDone whenAllDone = new WhenAllDone(new EvaluationQueueElement(0, () -> {
            assert queue.isEmpty();
            hasRun.set(true);
        }), queue);


        SignatureCollector collector = new SignatureCollector();
        realDeclaration.accept(collector, new SignatureCollector.Arg(myDeclaration, "window"));
        Set<FunctionToEvaluate> functions = collector.getFunctions();
        Set<FunctionToEvaluate> constructors = collector.getConstructors();

        Map<String, Evaluation> evaluations = new HashMap<>();

        Set<Type> classInstanceTypes = ClassFinder.getClassInstanceTypes(myDeclaration, realDeclaration);

        for (FunctionToEvaluate toEvaluate : functions) {
            Runnable callback = whenAllDone.newSubCallback();

            queue.add(new EvaluationQueueElement(0, () -> {
                Evaluation evaluation = Evaluation.create(options.debugPrint);
                assert !evaluations.containsKey(toEvaluate.path);
                evaluations.put(toEvaluate.path, evaluation);
                EvaluationVisitor visitor = new EvaluationVisitor(0, evaluation, queue, nativeTypesInReal, realNativeClasses, myNativeClasses, emptyNativeClasses, new HashSet<>(), options, classInstanceTypes);
                visitor.evaluateFunctions(new ArrayList<>(toEvaluate.myFunc), new ArrayList<>(toEvaluate.realFunc), callback, false, toEvaluate.path, false);
            }));
        }

        for (FunctionToEvaluate toEvaluate : constructors) {
            Runnable callback = whenAllDone.newSubCallback();

            queue.add(new EvaluationQueueElement(0, () -> {
                Evaluation evaluation = Evaluation.create(options.debugPrint);
                assert !evaluations.containsKey(toEvaluate.path);
                evaluations.put(toEvaluate.path, evaluation);
                EvaluationVisitor visitor = new EvaluationVisitor(0, evaluation, queue, nativeTypesInReal, realNativeClasses, myNativeClasses, emptyNativeClasses, new HashSet<>(), options, classInstanceTypes);
                visitor.evaluateFunctions(new ArrayList<>(toEvaluate.myFunc), new ArrayList<>(toEvaluate.realFunc), callback, false, toEvaluate.path, true);
            }));
        }

        whenAllDone.newSubCallback().run(); // Making sure at least one has run.

        while (!queue.isEmpty()) {
            EvaluationQueueElement element = queue.poll();
            element.runnable.run();
        }

        assert hasRun.get();
        return evaluations;
    }


    final static class EvaluationQueueElement implements Comparable<EvaluationQueueElement> {
        final Runnable runnable;
        final int depth;

        public EvaluationQueueElement(int depth, Runnable runnable) {
            this.runnable = runnable;
            this.depth = depth;
        }

        @Override
        public int compareTo(EvaluationQueueElement o) {
            return Integer.compare(this.depth, o.depth);
        }
    }


    public Evaluation getEvaluation() {
        return evaluation;
    }

    public static class ParsedDeclaration {
        private final Snap.Obj emptySnap;
        private String resultFilePath;
        private BenchMark benchMark;
        private final Snap.Obj global;
        private final Map<Snap.Obj, LibraryClass> libraryClasses;
        private AtomicReference<SpecReader> realDeclaration;
        private AtomicReference<SpecReader> myDeclaration;
        private Set<String> properties;
        private Set<Type> nativeTypesInReal;
        private NativeClassesMap realNativeClasses;
        private NativeClassesMap myNativeClasses;
        private NativeClassesMap emptyNativeClasses;

        public ParsedDeclaration(String resultFilePath, BenchMark benchMark, Snap.Obj global, Map<Snap.Obj, LibraryClass> libraryClasses, Snap.Obj emptySnap) {
            this.resultFilePath = resultFilePath;
            this.benchMark = benchMark;
            this.global = global;
            this.libraryClasses = libraryClasses;
            this.emptySnap = emptySnap;
        }

        public InterfaceType getRealDeclaration() {
            return (InterfaceType) realDeclaration.get().getGlobal();
        }

        public InterfaceType getMyDeclaration() {
            return (InterfaceType) myDeclaration.get().getGlobal();
        }

        public Set<String> getProperties() {
            return properties;
        }

        public Set<Type> getNativeTypesInReal() {
            return nativeTypesInReal;
        }

        public NativeClassesMap getRealNativeClasses() {
            return realNativeClasses;
        }

        public NativeClassesMap getMyNativeClasses() {
            return myNativeClasses;
        }

        public NativeClassesMap getEmptyNativeClasses() {
            return emptyNativeClasses;
        }

        public ParsedDeclaration invoke() {
            Environment env = benchMark.languageLevel.environment;
            List<String> dependencies = benchMark.dependencyDeclarations();
            realDeclaration = new AtomicReference<>();
            myDeclaration = new AtomicReference<>();
            AtomicReference<SpecReader> emptyDeclaration = new AtomicReference<>();
            try {
                Util.runAll(() -> {
                    realDeclaration.set(getTypeSpecification(env, dependencies, benchMark.declarationPath));
                }, () -> {
                    myDeclaration.set(getTypeSpecification(env, dependencies, resultFilePath));
                }, () -> {
                    emptyDeclaration.set(getTypeSpecification(env, dependencies));
                });
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }

            properties = new HashSet<>();

            Set<String> realProperties = ((InterfaceType)realDeclaration.get().getGlobal()).getDeclaredProperties().keySet();
            Set<String> myProperties = ((InterfaceType)myDeclaration.get().getGlobal()).getDeclaredProperties().keySet();
            properties.addAll(realProperties);
            properties.addAll(myProperties);

            Set<String> existingProperties = ((InterfaceType)emptyDeclaration.get().getGlobal()).getDeclaredProperties().keySet();
            properties.removeAll(existingProperties);

            realNativeClasses = parseNatives(global, libraryClasses, realDeclaration.get(), emptySnap);
            this.emptyNativeClasses = parseNatives(global, libraryClasses, emptyDeclaration.get(), emptySnap);
            myNativeClasses = parseNatives(global, libraryClasses, myDeclaration.get(),  emptySnap);


            nativeTypesInReal = new HashSet<>();
            for (String name : emptyNativeClasses.getNativeTypeNames()) {
                Type type = realNativeClasses.typeFromName(name);
                assert type != null;
                nativeTypesInReal.add(type);
            }


            return this;
        }
    }

    private static final class ClassFinder implements TypeVisitor<Void> {
        Set<Type> seen = new HashSet<>();
        Set<Type> classTypes = new HashSet<>();
        Set<Type> classInstanceTypes = new HashSet<>();

        public static Set<Type> getClassInstanceTypes(Type... roots) {
            ClassFinder visitor = new ClassFinder();
            for (Type root : roots) {
                root.accept(visitor);
            }

            return visitor.classInstanceTypes;
        }

        @Override
        public Void visit(AnonymousType t) {
            return null;
        }

        @Override
        public Void visit(ClassType t) {
            throw new RuntimeException();
        }

        @Override
        public Void visit(GenericType t) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);
            return t.toInterface().accept(this);
        }

        @Override
        public Void visit(InterfaceType t) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);
            t.getDeclaredProperties().values().forEach(type -> type.accept(this));
            Util.concat(t.getDeclaredCallSignatures(), t.getDeclaredConstructSignatures()).forEach(sig -> {
                sig.getResolvedReturnType().accept(this);
                sig.getParameters().forEach(par -> par.getType().accept(this));
            });

            if (t.getDeclaredConstructSignatures() != null && !t.getDeclaredConstructSignatures().isEmpty()) {
                classTypes.add(t);
                t.getDeclaredConstructSignatures().stream().map(Signature::getResolvedReturnType).forEach(classInstanceTypes::add);
            }

            return null;
        }

        @Override
        public Void visit(ReferenceType t) {
            return t.getTarget().accept(this);
        }

        @Override
        public Void visit(SimpleType t) {
            return null;
        }

        @Override
        public Void visit(TupleType t) {
            t.getElementTypes().forEach(type -> type.accept(this));
            return null;
        }

        @Override
        public Void visit(UnionType t) {
            t.getElements().forEach(type -> type.accept(this));
            return null;
        }

        @Override
        public Void visit(UnresolvedType t) {
            throw new RuntimeException();
        }

        @Override
        public Void visit(TypeParameterType t) {
            t.getConstraint().accept(this);
            if (t.getTarget() != null) {
                t.getTarget().accept(this);
            }
            return null;
        }

        @Override
        public Void visit(SymbolType t) {
            throw new RuntimeException();
        }
    }
}
