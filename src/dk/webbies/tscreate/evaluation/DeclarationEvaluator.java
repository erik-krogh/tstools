package dk.webbies.tscreate.evaluation;

import dk.au.cs.casa.typescript.SpecReader;
import dk.au.cs.casa.typescript.types.InterfaceType;
import dk.au.cs.casa.typescript.types.Type;
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
    private HashMap<Snap.Obj, LibraryClass> libraryClasses;

    public DeclarationEvaluator(String resultFilePath, BenchMark benchMark, Snap.Obj global, HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options) {
        this.global = global;
        this.libraryClasses = libraryClasses;
        ParsedDeclaration parsedDeclaration = new ParsedDeclaration(resultFilePath, benchMark).invoke();
        InterfaceType realDeclaration = parsedDeclaration.getRealDeclaration();
        InterfaceType myDeclaration = parsedDeclaration.getMyDeclaration();
        Set<String> properties = parsedDeclaration.getProperties();
        Set<Type> nativeTypesInReal = parsedDeclaration.getNativeTypesInReal();


        realDeclaration.getDeclaredProperties().keySet().retainAll(properties);
        myDeclaration.getDeclaredProperties().keySet().retainAll(properties);

        AtomicBoolean hasRun = new AtomicBoolean(false);
        Runnable doneCallback = () -> {
            assert queue.isEmpty();
            hasRun.set(true);
        };
        evaluation = new Evaluation(options);
        queue.add(new EvaluationQueueElement(0, () -> {
            new EvaluationVisitor(0, evaluation, queue, nativeTypesInReal, parsedDeclaration.getRealNativeClasses(), parsedDeclaration.getMyNativeClasses(), new HashSet<>(), options, !options.onlyEvaluateUnderFunctionArgsAndReturn)
                    .visit(realDeclaration, new EvaluationVisitor.Arg(myDeclaration, doneCallback, "window"));
        }));

        while (!queue.isEmpty()) {
            EvaluationQueueElement element = queue.poll();
            element.runnable.run();
        }

        assert hasRun.get();
    }

    PriorityQueue<EvaluationQueueElement> queue = new PriorityQueue<>();

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

    private class ParsedDeclaration {
        private String resultFilePath;
        private BenchMark benchMark;
        private AtomicReference<SpecReader> realDeclaration;
        private AtomicReference<SpecReader> myDeclaration;
        private Set<String> properties;
        private Set<Type> nativeTypesInReal;
        private NativeClassesMap realNativeClasses;
        private NativeClassesMap myNativeClasses;

        public ParsedDeclaration(String resultFilePath, BenchMark benchMark) {
            this.resultFilePath = resultFilePath;
            this.benchMark = benchMark;
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

            realNativeClasses = parseNatives(global, libraryClasses, realDeclaration.get());
            NativeClassesMap emptyNativeClasses = parseNatives(global, libraryClasses, emptyDeclaration.get());
            myNativeClasses = parseNatives(global, libraryClasses, myDeclaration.get());

            nativeTypesInReal = new HashSet<>();
            for (String name : emptyNativeClasses.getNames()) {
                Type type = realNativeClasses.typeFromName(name);
                assert type != null;
                nativeTypesInReal.add(type);
            }


            return this;
        }
    }
}
