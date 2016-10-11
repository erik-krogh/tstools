package dk.webbies.tscreate.main;

import com.google.javascript.jscomp.parsing.parser.Parser;
import dk.webbies.tscreate.*;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.analysis.declarations.*;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.methods.NoTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.combined.CombinedTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.contextSensitive.combined.CombinedContextSensitiveTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.contextSensitive.mixed.MixedContextSensitiveTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.contextSensitive.pureSubsets.PureSubsetsContextSensitiveTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.mixed.MixedTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.old.analysis.OldTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.pureSubsets.PureSubsetsTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.unionEverything.UnionEverythingTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.unionRecursively.UnionRecursivelyTypeAnalysis;
import dk.webbies.tscreate.cleanup.RedundantInterfaceCleaner;
import dk.webbies.tscreate.evaluation.*;
import dk.webbies.tscreate.jsnap.*;
import dk.webbies.tscreate.jsnap.classes.*;
import dk.webbies.tscreate.main.patch.*;
import dk.webbies.tscreate.paser.AST.*;
import dk.webbies.tscreate.paser.JavaScriptParser;
import dk.webbies.tscreate.util.Util;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.BenchMark.*;
import static dk.webbies.tscreate.declarationReader.DeclarationParser.*;
import static dk.webbies.tscreate.util.Util.toFixed;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        try {
            // All the currently configured Benchmarks can be seen in the BenchMark.java file, there is a lot, and some of them were never used for the paper.
            // The ones used in the paper are:
            List<BenchMark> paperBenchmarks = Arrays.asList(ace, angular, async142, backbone, D3, ember1, FabricJS15, hammer, handlebars30, jQuery, knockout, leaflet, moment, vue, jasmine22, PIXI, react15, polymer16, three, underscore17);

            // Analyzing some benchmarks can be very memory intensive, if you have under 16GB of ram available, some benchmarks might run very slow (PixiJS and Ember are the worst offenders).
            // The below benchmarks should only use up to 3GB of RAM, even when running TSEvolve.
            List<BenchMark> fastBenchmarks = Arrays.asList(async142, backbone, hammer, handlebars30, jasmine22, knockout, moment, underscore17);

//            To run TSInfer on a benchmark, uncomment the below line, and TSInfer will run on the given benchmark. The declaration will both be printed to a file (name of which is printed in the beginning of the run), and to stdout.
//            runTSInfer(PIXI_4_0);

//            You can modify the options for a benchmark (see the options in Options.java, some of them are explained).
//            PIXI_4_0.getOptions().staticMethod = NONE;
//            PIXI_4_0.getOptions().createInstances = false;
//            runTSInfer(PIXI_4_0);

//            To run TSEvolve, use the runTSEvolve method (as seen below).
//            The first argument is the old JavaScript implementation, the second argument is the new implementation, and the last argument is the path were the resulting .json file should be saved.
//            The diff can be viewed by copying the .json you want to view to "diffViewer/diff.json", and then running the "View TSEvolve diff" configuration (dropdown in top-right corder of IntelliJ). (or view the index.html in the diffViewer folder using some webserver).

//            The TSEvolve runs used in the paper, uncomment to run
//            runTSEvolve(ember1, ember20, "diffViewer/ember-1-2.json");
//            runTSEvolve(ember20, ember27, "diffViewer/ember-20-27.json");
//            runTSEvolve(backbone, backbone133, "diffViewer/backbone-10-13.json");
//            runTSEvolve(async142, async201, "diffViewer/async-1-2.json");
//            runTSEvolve(handlebars30, handlebars4, "diffViewer/handlebars-3-4.json");
//            runTSEvolve(moment, moment_214, "diffViewer/moment-to-214.json");
//            runTSEvolve(PIXI, PIXI_4_0, "diffViewer/pixi-3-4.json"); // VERY memory intensive! Allocates up to 26GB, but should run on machines with 16GB of RAM that has an SSD.

//            Other TSEvolve runs.
//            runTSEvolve(jasmine22, jasmine25, "diffViewer/jasmine-22-25.json");
//            runTSEvolve(react014, react15, "diffViewer/react-014-15.json");
//            runTSEvolve(underscore17, underscore18, "diffViewer/underscore-17-18.json");
//            runTSEvolve(polymer11, polymer16, "diffViewer/polymer-11-16.json");
//            runTSEvolve(jasmine22, jasmine25, "diffViewer/jasmine-22-25.json");
//            runTSEvolve(FabricJS15, FabricJS16, "diffViewer/fabric-15-16.json");

        } catch (Throwable e) {
            System.err.println("Crashed: ");

            e.printStackTrace(System.err);
        } finally {
            long end = System.currentTimeMillis();
            System.out.println("Ran in " + toFixed((end - start) / 1000.0, 1) + "s");

            System.exit(0);
        }
    }

    public static void runTSEvolve(BenchMark oldVersion, BenchMark newVersion, String jsonPath) throws Throwable {
        PatchFile patchFile = PatchFileFactory.fromImplementation(oldVersion, newVersion);
        savePatchFile(patchFile, jsonPath);
    }

    public static void savePatchFile(PatchFile patchFile, String path) throws IOException {
        String printed = patchFile.toJSON().toString();

        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)));
        writer.write(printed);
        writer.close();
    }

    public static Score makeSuperEval(BenchMark benchMark) throws IOException {
        if (benchMark.declarationPath == null) {
            throw new RuntimeException();
        }

        Set<String> resultingDeclarationFilePaths = new HashSet<>();
        for (Options.StaticAnalysisMethod method : Options.StaticAnalysisMethod.values()) {
            benchMark.getOptions().staticMethod = method;

            String resultDeclarationFilePath = getResultingDeclarationPath(benchMark);

            if (new File(resultDeclarationFilePath).exists()) {
                resultingDeclarationFilePaths.add(resultDeclarationFilePath);
            }
        }

        System.out.println("Getting a \"best\" possible evaluation from " + resultingDeclarationFilePaths.size() + " evaluations");

        FunctionExpression AST = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, getScript(benchMark)).toTSCreateAST();

        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.getOptions(), benchMark.dependencyScripts(), benchMark.testFiles, benchMark.getOptions().asyncTest), AST);
        Snap.Obj emptySnap = JSNAPUtil.getEmptyJSnap(benchMark.getOptions(), benchMark.dependencyScripts(), AST); // Not empty, just the one without the library we are analyzing.

        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObject, benchMark.getOptions()).extract();

        Evaluation evaluation = new DeclarationEvaluator(resultingDeclarationFilePaths, benchMark, globalObject, libraryClasses, benchMark.getOptions(), emptySnap).getEvaluation();

        System.out.println(evaluation);
        System.out.println("best        : " + Util.toFixed(evaluation.score().fMeasure, 4) + " - " + Util.toFixed(evaluation.score().recall, 4) + " - " + Util.toFixed(evaluation.score().precision, 4));

        return null;

    }

    public static Evaluation runTSInfer(BenchMark benchMark) throws IOException {
        String resultDeclarationFilePath = getResultingDeclarationPath(benchMark);

        System.out.println("Analysing " + benchMark.name + " - output: " + resultDeclarationFilePath);

        FunctionExpression AST = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, getScript(benchMark)).toTSCreateAST();

        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.getOptions(), benchMark.dependencyScripts(), benchMark.testFiles, benchMark.getOptions().asyncTest), AST);
        Snap.Obj emptySnap = JSNAPUtil.getEmptyJSnap(benchMark.getOptions(), benchMark.dependencyScripts(), AST); // Not empty, just the one without the library we are analyzing.

        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObject, benchMark.getOptions()).extract();

        NativeClassesMap nativeClasses = parseNatives(globalObject, benchMark.languageLevel.environment, benchMark.dependencyDeclarations(), libraryClasses, emptySnap);

        Map<String, DeclarationType> declaration = createDeclaration(benchMark, AST, globalObject, emptySnap, libraryClasses, nativeClasses);

        System.out.println("Printing declaration");
        String printedDeclaration = new DeclarationPrinter(declaration, nativeClasses, benchMark.getOptions()).print();
        System.out.println(printedDeclaration);

        Util.writeFile(resultDeclarationFilePath, printedDeclaration);

        Evaluation evaluation = null;
        if (benchMark.declarationPath != null) {
            evaluation = new DeclarationEvaluator(resultDeclarationFilePath, benchMark, globalObject, libraryClasses, benchMark.getOptions(), emptySnap).getEvaluation();

//            System.out.println(evaluation);
        }

        if (benchMark.getOptions().tsCheck) {
            System.out.println("TSCheck: ");
            System.out.println(Util.tsCheck(benchMark.scriptPath, resultDeclarationFilePath));
            System.out.println("----------");
        }

        if (evaluation == null) {
            return null;
        } else {
            String evaluationString = "\n\n/*\n" + evaluation.toString() + "\n*/\n";
            if (benchMark.getOptions().debugPrint) {
                evaluationString = "\n\n/*\n" + ((DebugEvaluation)evaluation).debugPrint() + "\n\n*/\n" + evaluationString;
            }
            Util.writeFile(resultDeclarationFilePath, printedDeclaration + evaluationString);
            return evaluation;
        }
    }

    public static Map<String, DeclarationType> createDeclaration(BenchMark benchMark) throws IOException {
        String resultDeclarationFilePath = getResultingDeclarationPath(benchMark);

        System.out.println("Analysing " + benchMark.name + " - output: " + resultDeclarationFilePath);

        FunctionExpression AST = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, getScript(benchMark)).toTSCreateAST();

        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.getOptions(), benchMark.dependencyScripts(), benchMark.testFiles, benchMark.getOptions().asyncTest), AST);
        Snap.Obj emptySnap = JSNAPUtil.getEmptyJSnap(benchMark.getOptions(), benchMark.dependencyScripts(), AST); // Not empty, just the one without the library we are analyzing.

        Map<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObject, benchMark.getOptions()).extract();

        NativeClassesMap nativeClasses = parseNatives(globalObject, benchMark.languageLevel.environment, benchMark.dependencyDeclarations(), libraryClasses, emptySnap);

        return createDeclaration(benchMark, AST, globalObject, emptySnap, libraryClasses, nativeClasses);
    }

    public static long staticAnalysisTime = 0; // ugly ugly ugly, i know.

    public static Map<String, DeclarationType> createDeclaration(BenchMark benchMark, FunctionExpression AST, Snap.Obj globalObject, Snap.Obj emptySnap, Map<Snap.Obj, LibraryClass> libraryClasses, NativeClassesMap nativeClasses) {
        Map<AstNode, Set<Snap.Obj>> callsites = Collections.EMPTY_MAP;
        if (benchMark.getOptions().recordCalls && globalObject.getProperty("__jsnap__callsitesToClosures") != null && benchMark.getOptions().useCallsiteInformation) {
            callsites = JSNAPUtil.getCallsitesToClosures((Snap.Obj) globalObject.getProperty("__jsnap__callsitesToClosures").value, AST);
        }

        long startTime = System.currentTimeMillis();

        TypeAnalysis typeAnalysis = createTypeAnalysis(benchMark, globalObject, libraryClasses, nativeClasses, callsites);
        typeAnalysis.analyseFunctions();

        Map<String, DeclarationType> declaration = new DeclarationBuilder(emptySnap, globalObject, typeAnalysis.getTypeFactory()).buildDeclaration();

        if (benchMark.getOptions().combineInterfacesAfterAnalysis) {
            System.out.println("Comnbining types");
            new RedundantInterfaceCleaner(declaration, nativeClasses, typeAnalysis.getTypeFactory().typeReducer).runHeuristics();
        } else {
            System.out.println("Cleaning up types");
            new RedundantInterfaceCleaner(declaration, nativeClasses, typeAnalysis.getTypeFactory().typeReducer).cleanDeclarations();
        }

        Main.staticAnalysisTime = System.currentTimeMillis() - startTime;

        typeAnalysis.getTypeFactory().typeReducer.originals.clear();
        typeAnalysis.getTypeFactory().typeReducer.combinationTypeCache.clear();

        return declaration;
    }

    public static Score getScore(BenchMark benchMark, long timeout) throws IOException {
        String resultDeclarationFilePath = getResultingDeclarationPath(benchMark);

        Score readFromFile = readScoreFromDeclaration(resultDeclarationFilePath);
        if (readFromFile != null) {
            return readFromFile;
        }

        Evaluation evaluation = getEvaluation(benchMark, getResultingDeclarationPath(benchMark), timeout);

        if (evaluation == null) {
            throw new RuntimeException();
        } else {
            return evaluation.score();
        }
    }

    static Evaluation getEvaluation(BenchMark benchMark, String generatedDeclarationPath, long timeout) throws IOException {

        System.out.println("Get evaluation of " + benchMark.name + " - from: " + generatedDeclarationPath);

        if (!new File(generatedDeclarationPath).exists()) {
            if (timeout > 0) {
                return runAnalysisWithTimeout(benchMark, timeout);
            } else {
                return runTSInfer(benchMark);
            }
        }

        FunctionExpression AST = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, getScript(benchMark)).toTSCreateAST();
        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.getOptions(), benchMark.dependencyScripts(), benchMark.testFiles, benchMark.getOptions().asyncTest), AST);
        Snap.Obj emptySnap = JSNAPUtil.getEmptyJSnap(benchMark.getOptions(), benchMark.dependencyScripts(), AST); // Not empty, just the one without the library we are analyzing.

        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObject, benchMark.getOptions()).extract();

        Evaluation evaluation = null;
        if (benchMark.declarationPath != null) {
            evaluation = new DeclarationEvaluator(generatedDeclarationPath, benchMark, globalObject, libraryClasses, benchMark.getOptions(), emptySnap).getEvaluation();
        }
        return evaluation;
    }

    private static Score readScoreFromDeclaration(String filePath) throws FileNotFoundException {
        if (!new File(filePath).exists()) {
            return null;
        }
        List<String> lines = new BufferedReader(new FileReader(new File(filePath))).lines().collect(Collectors.toList());
        Collections.reverse(lines);

        Double fMeasure = null;
        Double precision = null;
        Double recall = null;

        int counter = 0;
        for (String line : lines) {
            if (counter++ > 10) {
                break;
            }
            if (line.startsWith("Score: ")) {
                fMeasure = Double.parseDouble(line.substring("Score: ".length(), line.length()));
            }
            if (line.startsWith("Precision: ")) {
                precision = Double.parseDouble(line.substring("Precision: ".length(), line.length()));
            }
            if (line.startsWith("Recall: ")) {
                recall = Double.parseDouble(line.substring("Recall: ".length(), line.length()));
            }
        }
        if (fMeasure != null && precision != null && recall != null) {
            return new Score(fMeasure, precision, recall);
        } else {
            return null;
        }
    }

    public static String getResultingDeclarationPath(BenchMark benchMark) {
        Options options = benchMark.getOptions();
        String fileSuffix = options.staticMethod.fileSuffix;
        if (options.combineInterfacesAfterAnalysis) {
            fileSuffix += "_smaller";
        }
        switch (options.evaluationMethod) {
            case ONLY_FUNCTIONS:
                fileSuffix += "_evalFunc";
                break;
            case ONLY_HEAP:
                fileSuffix += "_evalHeap";
                break;
            case EVERYTHING: break;
            default:
                throw new RuntimeException();

        }
        if (options.disableFlowFromArgsToParams) {
            fileSuffix += "_noArgPar";
        }
        if (options.disableFlowFromParamsToArgs) {
            fileSuffix += "_noParArg";
        }
        if (options.disableFlowFromCallsiteToReturn) {
            fileSuffix += "_noCalRet";
        }
        if (options.disableFlowFromReturnToCallsite) {
            fileSuffix += "_noRetCal";
        }
        if (options.useJSDoc) {
            fileSuffix += "_jsDoc";
        }
        return benchMark.scriptPath + "." + fileSuffix + ".gen.d.ts";
    }

    private static TypeAnalysis createTypeAnalysis(BenchMark benchMark, Snap.Obj globalObject, Map<Snap.Obj, LibraryClass> libraryClasses, NativeClassesMap nativeClasses, Map<AstNode, Set<Snap.Obj>> callsites) {
        switch (benchMark.getOptions().staticMethod) {
            case MIXED:
                return new MixedTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, false, callsites);
            case UPPER:
                return new MixedTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, true, callsites);
            case ANDERSON:
                return new PureSubsetsTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, callsites);
            case UNIFICATION:
                return new UnionEverythingTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, callsites);
            case UNIFICATION_CONTEXT_SENSITIVE:
                return new UnionRecursivelyTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, callsites);
            case OLD_UNIFICATION_CONTEXT_SENSITIVE:
                return new OldTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses);
            case OLD_UNIFICATION:
                return new OldTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses);
            case COMBINED:
                return new CombinedTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, false, callsites);
            case UPPER_LOWER:
                return new CombinedTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, true, callsites);
            case MIXED_CONTEXT_SENSITIVE:
                return new MixedContextSensitiveTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, false, callsites);
            case LOWER_CONTEXT_SENSITIVE:
                return new MixedContextSensitiveTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, true, callsites);
            case ANDERSON_CONTEXT_SENSITIVE:
                return new PureSubsetsContextSensitiveTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, callsites);
            case COMBINED_CONTEXT_SENSITIVE:
                return new CombinedContextSensitiveTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, false, callsites);
            case UPPER_LOWER_CONTEXT_SENSITIVE:
                return new CombinedContextSensitiveTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, true, callsites);
            case NONE:
                return new NoTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, false, callsites);
            default:
                throw new RuntimeException("I don't even know this static analysis method. ");
        }
    }

    public static String getScript(BenchMark benchMark) throws IOException {
        StringBuilder result = new StringBuilder();
        result.append(Util.readFile(benchMark.scriptPath)).append("\n");
        for (String testFile : benchMark.testFiles) {
            result.append(Util.readFile(testFile)).append("\n");
        }
        return result.toString();
    }

    private static void generateDeclarations(List<BenchMark> benchMarks) throws IOException {
        for (BenchMark benchmark : benchMarks) {
            benchmark.declarationPath = null;
            runTSInfer(benchmark);
        }

        System.out.println("Generated all declarations");
    }

    public static Evaluation runAnalysisWithTimeout(BenchMark benchMark, long timeout) {
        AtomicReference<Evaluation> result = new AtomicReference<>(null);
        Thread benchThread = new Thread(() -> {
            try {
                result.set(runTSInfer(benchMark));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
        benchThread.start();
        Thread killThread = new Thread(() -> {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                return;
            }
            if (result.get() != null) {
                return;
            }
            if (benchThread.isAlive()) {
                System.err.println("Stopping benchmark because of timeout.");
                //noinspection deprecation
                benchThread.stop(); // <- Deprecated, and i know it.
            }
        });
        killThread.start();
        try {
            benchThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        killThread.interrupt();
        return result.get();
    }

    @SuppressWarnings("Duplicates")
    public static Map<String, DeclarationType> createDeclarationWithTimeout(BenchMark benchMark, long timeout) {
        AtomicReference<Map<String, DeclarationType>> result = new AtomicReference<>(null);
        Thread benchThread = new Thread(() -> {
            try {
                result.set(createDeclaration(benchMark));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
        benchThread.start();
        Thread killThread = new Thread(() -> {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                return;
            }
            if (result.get() != null) {
                return;
            }
            if (benchThread.isAlive()) {
                System.err.println("Stopping benchmark because of timeout.");
                benchThread.interrupt();
                //noinspection deprecation
                benchThread.stop(); // <- Deprecated, and i know it.
            }
        });
        killThread.start();
        try {
            benchThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        killThread.interrupt();
        return result.get();
    }

    public enum LanguageLevel {
        ES5(Parser.Config.Mode.ES5, Environment.ES5DOM),
        ES6(Parser.Config.Mode.ES6, Environment.ES6DOM);

        public final Parser.Config.Mode closureCompilerMode;
        public final Environment environment;

        LanguageLevel(Parser.Config.Mode closure, Environment declaration) {
            this.closureCompilerMode = closure;
            this.environment = declaration;
        }
    }
}
