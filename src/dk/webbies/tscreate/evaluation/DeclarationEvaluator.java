package dk.webbies.tscreate.evaluation;

import dk.au.cs.casa.typescript.SpecReader;
import dk.au.cs.casa.typescript.types.InterfaceType;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.BenchMark;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.declarationReader.DeclarationParser.*;

/**
 * Created by Erik Krogh Kristensen on 05-10-2015.
 */
public class DeclarationEvaluator {
    private final Evaluation evaluation = new Evaluation();
    private Snap.Obj global;
    private HashMap<Snap.Obj, LibraryClass> libraryClasses;

    public DeclarationEvaluator(String resultFilePath, BenchMark benchMark, Snap.Obj global, HashMap<Snap.Obj, LibraryClass> libraryClasses) {
        this.global = global;
        this.libraryClasses = libraryClasses;
        ParsedDeclaration parsedDeclaration = new ParsedDeclaration(resultFilePath, benchMark).invoke();
        InterfaceType realDeclaration = parsedDeclaration.getRealDeclaration();
        InterfaceType myDeclaration = parsedDeclaration.getMyDeclaration();
        Set<String> properties = parsedDeclaration.getProperties();
        Set<Type> nativeTypesInReal = parsedDeclaration.getNativeTypesInReal();


        realDeclaration.getDeclaredProperties().keySet().retainAll(properties);
        myDeclaration.getDeclaredProperties().keySet().retainAll(properties);

        queue.add(() -> new EvaluationVisitor(0, evaluation, queue, nativeTypesInReal).visit(realDeclaration, myDeclaration));

        while (!queue.isEmpty()) {
            ArrayList<Runnable> copy = new ArrayList<>(queue);
            queue.clear();
            copy.forEach(Runnable::run);
        }
    }

    List<Runnable> queue = new ArrayList<>();


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

        public ParsedDeclaration invoke() {
            Environment env = benchMark.languageLevel.environment;
            List<String> dependencies = benchMark.dependencies.stream().map(dep -> dep.declarationPath).collect(Collectors.toList());
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

            NativeClassesMap realNativeClasses = parseNatives(global, libraryClasses, realDeclaration.get());
            NativeClassesMap emptyNativeClasses = parseNatives(global, libraryClasses, emptyDeclaration.get());

            nativeTypesInReal = new HashSet<>(); // FIXME: Make sure this makes sense.
            for (String name : emptyNativeClasses.getNames()) {
                Type type = realNativeClasses.typeFromName(name);
                assert type != null;
                nativeTypesInReal.add(type);
            }


            return this;
        }
    }
}
