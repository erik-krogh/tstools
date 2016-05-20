package dk.webbies.tscreate.main;

import com.google.javascript.jscomp.parsing.parser.Parser;
import dk.au.cs.casa.typescript.types.InterfaceType;
import dk.webbies.tscreate.BenchMark;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.Score;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.analysis.declarations.DeclarationBuilder;
import dk.webbies.tscreate.analysis.declarations.DeclarationPrinter;
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
import dk.webbies.tscreate.evaluation.DeclarationEvaluator;
import dk.webbies.tscreate.evaluation.Evaluation;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.ClassHierarchyExtractor;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.paser.JavaScriptParser;
import dk.webbies.tscreate.util.Util;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.BenchMark.*;
import static dk.webbies.tscreate.Options.StaticAnalysisMethod.*;
import static dk.webbies.tscreate.declarationReader.DeclarationParser.*;
import static dk.webbies.tscreate.util.Util.toFixed;
import static java.util.Arrays.asList;

/**
 * Created by Erik Krogh Kristensen on 01-09-2015.
 */
public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        // TODO: Write section of lower fixpoint != better result.

        // Benchmarks, where I can run ALL the static analysis methods.
        List<BenchMark> stableBenches = asList(async, require, knockout, backbone, hammer, moment, handlebars, underscore, please, path, p2, mathjax, materialize, photoswipe, peer, createjs, yui);
        List<Options.StaticAnalysisMethod> allMethods = asList(NONE, ANDERSON, MIXED, COMBINED, UPPER, UPPER_LOWER);//, ANDERSON_CONTEXT_SENSITIVE, MIXED_CONTEXT_SENSITIVE, COMBINED_CONTEXT_SENSITIVE, LOWER_CONTEXT_SENSITIVE, UPPER_LOWER_CONTEXT_SENSITIVE, UNIFICATION, UNIFICATION_CONTEXT_SENSITIVE);


        long start = System.currentTimeMillis();
        try {

//            knockout.getOptions().staticMethod = MIXED;
//            runAnalysis(knockout);

//            allBenchmarks.remove(test);
//            CompareMethods.compareConfigs(stableBenches, genUpperLowerCuts(UPPER_LOWER), 20 * 60 * 1000);
//            CompareMethods.compareConfigs(stableBenches, genUpperLowerCuts(UNIFICATION), 20 * 60 * 1000);
//            CompareMethods.compareConfigs(allBenchmarks, genUpperLowerCuts(UPPER_LOWER), 20 * 60 * 1000);
//            CompareMethods.compareConfigs(allBenchmarks, genUpperLowerCuts(UNIFICATION), 20 * 60 * 1000);
//            allBenchmarks.remove(ember);
//            CompareMethods.compareConfigs(allBenchmarks, genCompareMethods(), 20 * 60 * 1000);

            for (BenchMark benchmark : allBenchmarks) {
                benchmark.getOptions().useJSDoc = true;
                benchmark.getOptions().staticMethod = ANDERSON;
                runAnalysis(benchmark);
            }


//            jQuery.getOptions().staticMethod = UPPER_LOWER;
//            runAnalysis(jQuery);


//            CompareMethods.compareMethods(asList(test), asList(COMBINED, UPPER), 20 * 60 * 1000);



//            test.getOptions().staticMethod = MIXED;
//            runAnalysis(test);

//            test.getOptions().staticMethod = COMBINED;
//            leaflet.getOptions().combineInterfacesAfterAnalysis = false;
//            runAnalysis(test);
//
            /*allBenchmarks.forEach(bench -> {
                bench.getOptions().combineInterfacesAfterAnalysis = false;
                bench.getOptions().evaluationMethod = Options.EvaluationMethod.ONLY_FUNCTIONS;
            });
            CompareMethods.compareMethods(allBenchmarks, allMethods, 20 * 60 * 1000, false);

            allBenchmarks.remove(sugar);
            allBenchmarks.forEach(bench -> {
                bench.getOptions().combineInterfacesAfterAnalysis = false;
                bench.getOptions().evaluationMethod = Options.EvaluationMethod.ONLY_HEAP;
            });
            CompareMethods.compareMethods(allBenchmarks, allMethods, 20 * 60 * 1000, false);

            allBenchmarks.remove(sugar);
            allBenchmarks.forEach(bench -> {
                bench.getOptions().combineInterfacesAfterAnalysis = false;
                bench.getOptions().evaluationMethod = Options.EvaluationMethod.EVERYTHING;
            });
            CompareMethods.compareMethods(allBenchmarks, allMethods, 20 * 60 * 1000, false);*/


        } catch (Throwable e) {
            System.err.println("Crashed: ");

            e.printStackTrace(System.err);
        } finally {
            long end = System.currentTimeMillis();
            System.out.println("Ran in " + toFixed((end - start) / 1000.0, 1) + "s");

            System.exit(0);
        }
    }

    private static Collection<CompareMethods.Config> genCompareMethods() {
        return asList(new CompareMethods.Config("lower", (options) -> {
                    options.staticMethod = ANDERSON;
                }),
                new CompareMethods.Config("mixed", (options) -> {
                    options.staticMethod = MIXED;
                }),
                new CompareMethods.Config("combined", (options) -> {
                    options.staticMethod = COMBINED;
                }),
                new CompareMethods.Config("upper", (options) -> {
                    options.staticMethod = UPPER;
                }),
                new CompareMethods.Config("lower_upper", (options) -> {
                    options.staticMethod = UPPER_LOWER;
                }),
                new CompareMethods.Config("lower_upper_cut", (options) -> {
                    options.staticMethod = UPPER_LOWER;
                    options.disableFlowFromCallsiteToReturn = true;
                }));
    }

    private static List<CompareMethods.Config> genUpperLowerCuts(Options.StaticAnalysisMethod method) {
        return asList(
                new CompareMethods.Config("clean", (options) -> {
                    options.staticMethod = method;
                }),
                new CompareMethods.Config("cut_Param->Arg", (options) -> {
                    options.staticMethod = method;
                    options.disableFlowFromParamsToArgs = true;
                }),
                new CompareMethods.Config("cut_Return->Callsite", (options) -> {
                    options.staticMethod = method;
                    options.disableFlowFromReturnToCallsite = true;
                }),
                new CompareMethods.Config("cut_Callsite->Return", (options) -> {
                    options.staticMethod = method;
                    options.disableFlowFromCallsiteToReturn = true;
                }),
                new CompareMethods.Config("cut_Args->Param", (options) -> {
                    options.staticMethod = method;
                    options.disableFlowFromArgsToParams = true;
                })
        );
    }

    public static Score runAnalysis(BenchMark benchMark) throws IOException {
        String resultDeclarationFilePath = getResultingDeclarationPath(benchMark);

        if (new File(resultDeclarationFilePath).exists()) {
            return null;
        }

        System.out.println("Analysing " + benchMark.name + " - output: " + resultDeclarationFilePath);

        FunctionExpression AST = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, getScript(benchMark)).toTSCreateAST();

        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.getOptions(), benchMark.dependencyScripts(), benchMark.testFiles, benchMark.getOptions().asyncTest), AST);
        Snap.Obj emptySnap = JSNAPUtil.getEmptyJSnap(benchMark.getOptions(), benchMark.dependencyScripts(), AST); // Not empty, just the one without the library we are analyzing.

        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObject, benchMark.getOptions()).extract();

        NativeClassesMap nativeClasses = parseNatives(globalObject, benchMark.languageLevel.environment, benchMark.dependencyDeclarations(), libraryClasses);

        TypeAnalysis typeAnalysis = createTypeAnalysis(benchMark, globalObject, libraryClasses, nativeClasses);
        typeAnalysis.analyseFunctions();

        Map<String, DeclarationType> declaration = new DeclarationBuilder(emptySnap, globalObject, typeAnalysis.getTypeFactory()).buildDeclaration();

        if (benchMark.getOptions().combineInterfacesAfterAnalysis) {
            new RedundantInterfaceCleaner(declaration, nativeClasses, typeAnalysis.getTypeFactory().typeReducer).clean();
        }

        String printedDeclaration = new DeclarationPrinter(declaration, nativeClasses, benchMark.getOptions()).print();
//        System.out.println(printedDeclaration);

        Util.writeFile(resultDeclarationFilePath, printedDeclaration);

        if (benchMark.getOptions().filterResultBasedOnDeclaration) { // A little weird, because I'm reusing some code that also parses the resulting declaration. // TODO: Less weird.
            InterfaceType real = new DeclarationEvaluator.ParsedDeclaration(resultDeclarationFilePath, benchMark, globalObject, libraryClasses).invoke().getRealDeclaration();
            declaration = declaration.entrySet().stream().filter(entry -> real.getDeclaredProperties().keySet().contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, (entry) -> {
                return entry.getValue().accept(new FilterDeclarationVisitor(), real.getDeclaredProperties().get(entry.getKey()));
            }));
            printedDeclaration = new DeclarationPrinter(declaration, nativeClasses, benchMark.getOptions()).print();

            Util.writeFile(resultDeclarationFilePath, printedDeclaration);
        }

        Evaluation evaluation = null;
        String debugString = null;
        if (benchMark.declarationPath != null) {
            evaluation = new DeclarationEvaluator(resultDeclarationFilePath, benchMark, globalObject, libraryClasses, benchMark.getOptions()).evaluate();

            System.out.println(evaluation);

            if (benchMark.getOptions().debugPrint) {
                debugString = evaluation.debugPrint();
            }
        }

        if (benchMark.getOptions().tsCheck) {
            System.out.println("TSCheck: ");
            System.out.println(Util.tsCheck(benchMark.scriptPath, resultDeclarationFilePath));
            System.out.println("----------");
        }

        if (evaluation == null) {
            return new Score(Double.NaN, Double.NaN, Double.NaN);
        } else {
            String evaluationString = "\n\n/*\n" + evaluation.toString() + "\n*/\n";
            if (debugString != null) {
                evaluationString = "\n\n/*\n" + debugString + "\n\n*/\n" + evaluationString;
            }
            Util.writeFile(resultDeclarationFilePath, printedDeclaration + evaluationString);
            return evaluation.score();
        }
    }

    public static Score runEvaluation(BenchMark benchMark) throws IOException {
        String resultDeclarationFilePath = getResultingDeclarationPath(benchMark);

        if (!new File(resultDeclarationFilePath).exists()) {
            return new Score(-1, -1, -1);
        }

        Score readFromFile = readScoreFromDeclaration(resultDeclarationFilePath);
        if (readFromFile != null) {
            return readFromFile;
        }

        System.out.println("Get evaluation of " + benchMark.name + " - from: " + resultDeclarationFilePath);

        FunctionExpression AST = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, getScript(benchMark)).toTSCreateAST();
        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.getOptions(), benchMark.dependencyScripts(), benchMark.testFiles, benchMark.getOptions().asyncTest), AST);

        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObject, benchMark.getOptions()).extract();

        Evaluation evaluation = null;
        if (benchMark.declarationPath != null) {
            evaluation = new DeclarationEvaluator(resultDeclarationFilePath, benchMark, globalObject, libraryClasses, benchMark.getOptions()).evaluate();
        }

        if (evaluation == null) {
            throw new RuntimeException();
        } else {
            return evaluation.score();
        }
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

    private static String getResultingDeclarationPath(BenchMark benchMark) {
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
        if (options.filterResultBasedOnDeclaration) {
            fileSuffix += "_filtered";
        }
        if (options.useJSDoc) {
            fileSuffix += "_jsDoc";
        }
        return benchMark.scriptPath + "." + fileSuffix + ".gen.d.ts";
    }

    private static TypeAnalysis createTypeAnalysis(BenchMark benchMark, Snap.Obj globalObject, HashMap<Snap.Obj, LibraryClass> libraryClasses, NativeClassesMap nativeClasses) {
        switch (benchMark.getOptions().staticMethod) {
            case MIXED:
                return new MixedTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, false);
            case UPPER:
                return new MixedTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, true);
            case ANDERSON:
                return new PureSubsetsTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses);
            case UNIFICATION:
                return new UnionEverythingTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses);
            case UNIFICATION_CONTEXT_SENSITIVE:
                return new UnionRecursivelyTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses);
            case OLD_UNIFICATION_CONTEXT_SENSITIVE:
                return new OldTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses);
            case OLD_UNIFICATION:
                return new OldTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses);
            case COMBINED:
                return new CombinedTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, false);
            case UPPER_LOWER:
                return new CombinedTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, true);
            case MIXED_CONTEXT_SENSITIVE:
                return new MixedContextSensitiveTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, false);
            case LOWER_CONTEXT_SENSITIVE:
                return new MixedContextSensitiveTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, true);
            case ANDERSON_CONTEXT_SENSITIVE:
                return new PureSubsetsContextSensitiveTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses);
            case COMBINED_CONTEXT_SENSITIVE:
                return new CombinedContextSensitiveTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, false);
            case UPPER_LOWER_CONTEXT_SENSITIVE:
                return new CombinedContextSensitiveTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, true);
            case NONE:
                return new NoTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, false);
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

    public static void tsCheck() throws IOException {
        for (BenchMark benchmark : allBenchmarks) {
            benchmark.getOptions().tsCheck = true;
            runAnalysis(benchmark);
        }
    }

    private static void benchAll() throws IOException {
        double combinedScore = 0;
        for (BenchMark benchmark : allBenchmarks) {
            double score = runAnalysis(benchmark).fMeasure;
            if (!Double.isNaN(score)) {
                combinedScore += score;
            }
        }

        System.out.println("\nCombined score: " + combinedScore);
    }

    private static void generateDeclarations(List<BenchMark> benchMarks) throws IOException {
        if (benchMarks == null) {
            benchMarks = allBenchmarks;
        }
        for (BenchMark benchmark : benchMarks) {
            benchmark.declarationPath = null;
            runAnalysis(benchmark);
        }

        System.out.println("Generated all declarations");
    }

    private static void printTable() throws IOException {
        Map<BenchMark, Score> scores = new HashMap<>();
        for (BenchMark benchmark : allBenchmarks) {
            if (benchmark.declarationPath == null || benchmark == test) {
                continue;
            }
            Score score = runAnalysisWithTimeout(benchmark, Long.MAX_VALUE);
            if (score == null) {
                scores.put(benchmark, new Score(-1, -1, -1));
            } else {
                scores.put(benchmark, score);
            }
        }
        System.out.print("\n\n\n\n\n");
        System.out.println("\\begin{table}[]\n" +
                "\\centering\n" +
                "\\begin{tabular}{l|l|ccc}\n" +
                "& & \\multicolumn{3}{c}{\\textit{score}}  \\\\\n" +
                "\\textit{benchmark} & \\textit{lines of code} & \\multicolumn{1}{l}{\\textit{f-measure}} & \\multicolumn{1}{l}{\\textit{recall}} & \\multicolumn{1}{l}{\\textit{precision}} \\\\ \\hline");
        List<BenchMark> benches = scores.keySet().stream().sorted((o1, o2) -> o1.name.compareTo(o2.name)).collect(Collectors.toList());
        for (int i = 0; i < benches.size(); i++) {
            BenchMark benchMark = benches.get(i);
            String fMeasure = toFixed(scores.get(benchMark).fMeasure, 3);
            String precision = toFixed(scores.get(benchMark).precision, 3);
            String recall = toFixed(scores.get(benchMark).recall, 3);
            System.out.print(benchMark.name + " & " + Util.lines(benchMark.scriptPath) + " & " + fMeasure + " & " + precision + " & " + recall);
            if (i != benches.size() - 1) {
                System.out.print(" \\\\");
            }
            System.out.print("\n");
        }

        System.out.println("\\end{tabular}\n" +
                "\\caption{Evaluation of benchmarks} \\label{evaluationTable}\n" +
                "\\end{table}");

        System.out.println("\n\n");
        double sumScore = 0;
        for (Score score : scores.values()) {
            sumScore += score.fMeasure;
        }
        System.out.println("Combined score: " + sumScore);

    }

    public static Score runAnalysisWithTimeout(BenchMark benchMark, long timeout) {
        AtomicReference<Score> result = new AtomicReference<>(null);
        Thread benchThread = new Thread(() -> {
            try {
                result.set(runAnalysis(benchMark));
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
