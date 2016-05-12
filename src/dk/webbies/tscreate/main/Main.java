package dk.webbies.tscreate.main;

import com.google.javascript.jscomp.parsing.parser.Parser;
import dk.webbies.tscreate.BenchMark;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.Score;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.analysis.declarations.DeclarationBuilder;
import dk.webbies.tscreate.analysis.declarations.DeclarationPrinter;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
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
        // TODO: Look at bench, argue why: better score -> better dec. // <- Hamid looking at that
        // TODO: Sample excess fields, see how many are private variables. // <- Hamid looking at that
        // TODO: Write section of lower fixpoint != better result.

        // TODO: Think about object access things and lower/upper-bound. Basically, make subsets less upper-bound like.

        // TODO: Få styr på hvad der er LOWER/UPPER

        // TODO: Upper/lower, cut flow.

        // TODO: Lav en analyse metode: No static analysis, der kun tager ting fra dynamisk analyse (såsom calls).

        // TODO: Slå til/fra individuelt lower/upper i function calls.
        // TODO: Opdel evaluering i aspekter, function-signatures/modul-struktur (statisk/dynamisk analyse)? Lav en evaluering der antager at den anden del er perfekt.

        // Benchmarks, where I can run ALL the static analysis methods.
        List<BenchMark> stableBenches = asList(async, require, knockout, backbone, hammer, moment, handlebars, underscore, Q, please, path, p2, mathjax, materialize, photoswipe, peer);
        List<Options.StaticAnalysisMethod> allMethods = asList(ANDERSON, MIXED, COMBINED, UPPER, UPPER_LOWER);//, ANDERSON_CONTEXT_SENSITIVE, MIXED_CONTEXT_SENSITIVE, COMBINED_CONTEXT_SENSITIVE, LOWER_CONTEXT_SENSITIVE, UPPER_LOWER_CONTEXT_SENSITIVE, UNIFICATION, UNIFICATION_CONTEXT_SENSITIVE);
        long start = System.currentTimeMillis();
        try {
//            allBenchmarks.remove(sugar);
//            allBenchmarks.forEach(bench -> bench.options.combineInterfacesAfterAnalysis = false);
//            CompareMethods.compareMethods(allBenchmarks, Arrays.asList(UPPER_LOWER), 20 * 60 * 1000, false);

            test.options.staticMethod = UPPER;
            runAnalysis(test);


        } catch (Throwable e) {
            System.err.println("Crashed: ");

            e.printStackTrace(System.err);
        }
        long end = System.currentTimeMillis();
        System.out.println("Ran in " + toFixed((end - start) / 1000.0, 1) + "s");

        System.exit(0);
    }

    public static Score runAnalysis(BenchMark benchMark) throws IOException {
        String resultDeclarationFilePath = getResultingDeclarationPath(benchMark);

        if (readScoreFromDeclaration(resultDeclarationFilePath) != null) {
            return new Score(-1, -1, -1);
        }

        System.out.println("Analysing " + benchMark.name + " - output: " + resultDeclarationFilePath);

        FunctionExpression AST = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, getScript(benchMark)).toTSCreateAST();

        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.options, benchMark.dependencyScripts(), benchMark.testFiles, benchMark.options.asyncTest), AST);
        Snap.Obj emptySnap = JSNAPUtil.getEmptyJSnap(benchMark.options, benchMark.dependencyScripts(), AST); // Not empty, just the one without the library we are analyzing.

        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObject, benchMark.options).extract();

        NativeClassesMap nativeClasses = parseNatives(globalObject, benchMark.languageLevel.environment, benchMark.dependencyDeclarations(), libraryClasses);

        TypeAnalysis typeAnalysis = createTypeAnalysis(benchMark, globalObject, libraryClasses, nativeClasses);
        typeAnalysis.analyseFunctions();

        Map<String, DeclarationType> declaration = new DeclarationBuilder(emptySnap, globalObject, typeAnalysis.getTypeFactory()).buildDeclaration();

        if (benchMark.options.combineInterfacesAfterAnalysis) {
            new RedundantInterfaceCleaner(declaration, nativeClasses, typeAnalysis.getTypeFactory().typeReducer).clean();
        }

        String printedDeclaration = new DeclarationPrinter(declaration, nativeClasses, benchMark.options).print();
        System.out.println(printedDeclaration);

        Util.writeFile(resultDeclarationFilePath, printedDeclaration);

        Evaluation evaluation = null;
        String debugString = null;
        if (benchMark.declarationPath != null) {
            evaluation = new DeclarationEvaluator(resultDeclarationFilePath, benchMark, globalObject, libraryClasses, benchMark.options).evaluate();

            System.out.println(evaluation);

            if (benchMark.options.debugPrint) {
                debugString = evaluation.debugPrint();
            }
        }

        if (benchMark.options.tsCheck) {
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
        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.options, benchMark.dependencyScripts(), benchMark.testFiles, benchMark.options.asyncTest), AST);

        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObject, benchMark.options).extract();

        Evaluation evaluation = null;
        if (benchMark.declarationPath != null) {
            evaluation = new DeclarationEvaluator(resultDeclarationFilePath, benchMark, globalObject, libraryClasses, benchMark.options).evaluate();
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
        String fileSuffix = benchMark.options.staticMethod.fileSuffix;
        if (benchMark.options.combineInterfacesAfterAnalysis) {
            fileSuffix = fileSuffix + "_smaller";
        }
        return benchMark.scriptPath + "." + fileSuffix + ".gen.d.ts";
    }

    private static TypeAnalysis createTypeAnalysis(BenchMark benchMark, Snap.Obj globalObject, HashMap<Snap.Obj, LibraryClass> libraryClasses, NativeClassesMap nativeClasses) {
        switch (benchMark.options.staticMethod) {
            case MIXED:
                return new MixedTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses, false);
            case UPPER:
                return new MixedTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses, true);
            case ANDERSON:
                return new PureSubsetsTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses);
            case UNIFICATION:
                return new UnionEverythingTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses);
            case UNIFICATION_CONTEXT_SENSITIVE:
                return new UnionRecursivelyTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses);
            case OLD_UNIFICATION_CONTEXT_SENSITIVE:
                return new OldTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses);
            case OLD_UNIFICATION:
                return new OldTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses);
            case COMBINED:
                return new CombinedTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses, false);
            case UPPER_LOWER:
                return new CombinedTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses, true);
            case MIXED_CONTEXT_SENSITIVE:
                return new MixedContextSensitiveTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses, false);
            case LOWER_CONTEXT_SENSITIVE:
                return new MixedContextSensitiveTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses, true);
            case ANDERSON_CONTEXT_SENSITIVE:
                return new PureSubsetsContextSensitiveTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses);
            case COMBINED_CONTEXT_SENSITIVE:
                return new CombinedContextSensitiveTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses, false);
            case UPPER_LOWER_CONTEXT_SENSITIVE:
                return new CombinedContextSensitiveTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses, true);
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
            benchmark.options.tsCheck = true;
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
