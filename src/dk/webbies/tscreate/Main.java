package dk.webbies.tscreate;

import com.google.javascript.jscomp.parsing.parser.Parser;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.analysis.declarations.DeclarationBuilder;
import dk.webbies.tscreate.analysis.declarations.DeclarationPrinter;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.methods.mixed.MixedTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.old.analysis.OldTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.pureSubsets.PureSubsetsTypeAnalysis;
import dk.webbies.tscreate.evaluation.DeclarationEvaluator;
import dk.webbies.tscreate.evaluation.Evaluation;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.ClassHierarchyExtractor;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.paser.JavaScriptParser;
import dk.webbies.tscreate.util.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.declarationReader.DeclarationParser.*;
import static dk.webbies.tscreate.util.Util.toFixed;

/**
 * Created by Erik Krogh Kristensen on 01-09-2015.
 */
public class Main {
    // FIXME: Test does the .gen compare to thhe handwritten, if one tries to take a program writen against the hand-written, og replace the hand-written with the generated. Are there then compile-errors, and why are they there.
    // FIXME: If not, then the tool is a success.

    // FIXME: Check the outout from underscore, and try to se how the evaluation does on a single static method, what does it say.

    // FIXME: Ember is way to slow at the moment. 
    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            long start = System.currentTimeMillis();

//            tsCheck();
//            generateDeclarations(BenchMark.allBenchmarks);
//            runAnalysis(BenchMark.underscore);
//            benchAll();
//            printTable();
//            compareMethods(BenchMark.allBenchmarks, 1 * 60 * 1000);
//            compareMethods(Arrays.asList(BenchMark.underscore), 10 * 60 * 1000);



            long end = System.currentTimeMillis();

            System.out.println("Ran in " + toFixed((end - start) / 1000.0, 1) + "s");
        } catch (Throwable e) {
            System.err.println("Crashed: ");
            e.printStackTrace(System.err);
        } finally {
            System.exit(0);
        }
    }

    public static Score runAnalysis(BenchMark benchMark) throws IOException {
        System.out.println("Analysing " + benchMark.name);
        String resultDeclarationFilePath = benchMark.scriptPath + ".gen.d.ts";

        FunctionExpression AST = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, getScript(benchMark)).toTSCreateAST();

        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.options, benchMark.dependencyScripts(), benchMark.testFiles, benchMark.options.asyncTest), AST);
        Snap.Obj emptySnap = JSNAPUtil.getEmptyJSnap(benchMark.options, benchMark.dependencyScripts(), AST); // Not empty, just the one without the library we are analyzing.

        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObject, benchMark.options).extract();

        NativeClassesMap nativeClasses = parseNatives(globalObject, benchMark.languageLevel.environment, benchMark.dependencyDeclarations(), libraryClasses);

        TypeAnalysis typeAnalysis = createTypeAnalysis(benchMark, globalObject, libraryClasses, nativeClasses);
        typeAnalysis.analyseFunctions();

        Map<String, DeclarationType> declaration = new DeclarationBuilder(emptySnap, globalObject, typeAnalysis.getTypeFactory()).buildDeclaration();

        String printedDeclaration = new DeclarationPrinter(declaration, nativeClasses, benchMark.options).print();
        System.out.println(printedDeclaration);

        Util.writeFile(resultDeclarationFilePath, printedDeclaration);

        Evaluation evaluation = null;
        String debugString = null;
        if (benchMark.declarationPath != null) {
            evaluation = new DeclarationEvaluator(resultDeclarationFilePath, benchMark, globalObject, libraryClasses, benchMark.options).getEvaluation();

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

    @SuppressWarnings("Duplicates")
    private static void compareMethods(List<BenchMark> benchMarks, long timeout) throws IOException {
        Map<BenchMark, Map<Options.StaticAnalysisMethod, Score>> benchmarkScores = new HashMap<>();
        int decimals = 4;
        for (BenchMark benchMark : benchMarks) {
            if (benchMark.declarationPath == null) {
                continue;
            }
            Map<Options.StaticAnalysisMethod, Score> scores = new HashMap<>();
            benchmarkScores.put(benchMark, scores);
            for (Options.StaticAnalysisMethod method : Options.StaticAnalysisMethod.values()) {
                benchMark.options.staticMethod = method;
                System.out.println("With method: " + method.prettyString);
                scores.put(method, runAnalysisWithTimeout(benchMark, timeout));
            }

            for (Map.Entry<BenchMark, Map<Options.StaticAnalysisMethod, Score>> entry : benchmarkScores.entrySet()) {
                Map<Options.StaticAnalysisMethod, Score> dupScores = entry.getValue();
                System.out.println("Benchmark: " + entry.getKey().name);
                for (Map.Entry<Options.StaticAnalysisMethod, Score> scoreEntry : dupScores.entrySet()) {
                    Score score = scoreEntry.getValue();
                    if (score == null) {
                        score = new Score(-1, -1, -1);
                    }
                    System.out.println(scoreEntry.getKey().prettyString + " : " + toFixed(score.fMeasure, decimals) + " - " + toFixed(score.recall, decimals) + " - " + toFixed(score.precision, decimals));
                }
            }

        }

        for (Map.Entry<BenchMark, Map<Options.StaticAnalysisMethod, Score>> entry : benchmarkScores.entrySet()) {
            Map<Options.StaticAnalysisMethod, Score> scores = entry.getValue();
            System.out.println("Benchmark: " + entry.getKey().name);
            for (Map.Entry<Options.StaticAnalysisMethod, Score> scoreEntry : scores.entrySet()) {
                Score score = scoreEntry.getValue();
                if (score == null) {
                    score = new Score(-1, -1, -1);
                }
                System.out.println(scoreEntry.getKey().prettyString + " : " + toFixed(score.fMeasure, decimals) + " - " + toFixed(score.recall, decimals) + " - " + toFixed(score.precision, decimals));
            }
        }
    }

    private static TypeAnalysis createTypeAnalysis(BenchMark benchMark, Snap.Obj globalObject, HashMap<Snap.Obj, LibraryClass> libraryClasses, NativeClassesMap nativeClasses) {
        switch (benchMark.options.staticMethod) {
            case MY_MIXED_METHOD:
                return new MixedTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses);
            case TRADITIONAL_SUBSETS:
                return new PureSubsetsTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses);
            case TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING:
//                return new UnionEverythingTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses);
                return new OldTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses);
            case TRADITIONAL_UNIFICATION_RECURSIVELY_RESOLVE_CALLGRAPH:
//                return new UnionRecursivelyTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses);
                return new OldTypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses);
            default:
                throw new RuntimeException("I don't even know this static analysis method. ");
        }
    }

    private static String getScript(BenchMark benchMark) throws IOException {
        StringBuilder result = new StringBuilder();
        result.append(Util.readFile(benchMark.scriptPath)).append("\n");
        for (String testFile : benchMark.testFiles) {
            result.append(Util.readFile(testFile)).append("\n");
        }
        return result.toString();
    }

    public static void tsCheck() throws IOException {
        for (BenchMark benchmark : BenchMark.allBenchmarks) {
            benchmark.options.tsCheck = true;
            runAnalysis(benchmark);
        }
    }

    private static void benchAll() throws IOException {
        double combinedScore = 0;
        for (BenchMark benchmark : BenchMark.allBenchmarks) {
            double score = runAnalysis(benchmark).fMeasure;
            if (!Double.isNaN(score)) {
                combinedScore += score;
            }
        }

        System.out.println("\nCombined score: " + combinedScore);
    }

    private static void generateDeclarations(List<BenchMark> benchMarks) throws IOException {
        if (benchMarks == null) {
            benchMarks = BenchMark.allBenchmarks;
        }
        double combinedScore = 0;
        for (BenchMark benchmark : benchMarks) {
            benchmark.declarationPath = null;
            runAnalysis(benchmark);
        }

        System.out.println("Generated all declarations");
    }

    private static void printTable() throws IOException {
        Map<BenchMark, Score> scores = new HashMap<>();
        for (BenchMark benchmark : BenchMark.allBenchmarks) {
            if (benchmark.declarationPath == null || benchmark == BenchMark.test) {
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
