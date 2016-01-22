package dk.webbies.tscreate;

import com.google.javascript.jscomp.parsing.parser.Parser;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.analysis.declarations.DeclarationBuilder;
import dk.webbies.tscreate.analysis.declarations.DeclarationPrinter;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
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
import java.util.stream.Collectors;

import static dk.webbies.tscreate.declarationReader.DeclarationParser.*;

/**
 * Created by Erik Krogh Kristensen on 01-09-2015.
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            long start = System.currentTimeMillis();

//            tsCheck();
//            runAnalysis(BenchMark.test);
//            benchAll();
            printTable();

            long end = System.currentTimeMillis();

            System.out.println("Ran in " + ((end - start) / 1000.0) + "s");
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

        String script = Util.readFile(benchMark.scriptPath);

        FunctionExpression AST = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, script).toTSCreateAST();

        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.options, benchMark.dependencyScripts()), AST);
        Snap.Obj emptySnap = JSNAPUtil.getEmptyJSnap(benchMark.options, benchMark.dependencyScripts(), AST); // Not empty, just the one without the library we are analyzing.

        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObject).extract();

        NativeClassesMap nativeClasses = parseNatives(globalObject, benchMark.languageLevel.environment, benchMark.dependencyDeclarations(), libraryClasses);

        TypeAnalysis typeAnalysis = new TypeAnalysis(libraryClasses, benchMark.options, globalObject, nativeClasses);
        typeAnalysis.analyseFunctions();

        Map<String, DeclarationType> declaration = new DeclarationBuilder(emptySnap, globalObject, typeAnalysis.getTypeFactory()).buildDeclaration();

        String printedDeclaration = new DeclarationPrinter(declaration, nativeClasses).print();
        System.out.println(printedDeclaration);

        Util.writeFile(resultDeclarationFilePath, printedDeclaration);

        Evaluation evaluation = null;
        if (benchMark.declarationPath != null) {
            evaluation = new DeclarationEvaluator(resultDeclarationFilePath, benchMark, globalObject, libraryClasses).getEvaluation();

            System.out.println(evaluation);

//            evaluation.debugPrint();
        }

        if (benchMark.options.tsCheck) {
            System.out.println("TSCheck: ");
            System.out.println(Util.tsCheck(benchMark.scriptPath, resultDeclarationFilePath));
            System.out.println("----------");
        }

        if (evaluation == null) {
            return new Score(Double.NaN, Double.NaN, Double.NaN);
        } else {
            Util.writeFile(resultDeclarationFilePath, printedDeclaration + "\n\n/*\n" + evaluation.toString() + "\n*/\n");
            return evaluation.score();
        }
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

        System.out.println();
        System.out.println("Combined score: " + combinedScore);
    }

    private static void printTable() throws IOException {
        Map<BenchMark, Score> scores = new HashMap<>();
        for (BenchMark benchmark : BenchMark.allBenchmarks) {
            if (benchmark.declarationPath == null || benchmark == BenchMark.test) {
                continue;
            }
            Score score = runAnalysis(benchmark);
            if (!Double.isNaN(score.fMeasure)) {
                scores.put(benchmark, score);
            }
        }
        System.out.print("\n\n\n\n\n");
        System.out.println("\\begin{table}[]\n" +
                "\\centering\n" +
                "\\begin{tabular}{l|l|ccc}\n" +
                "& & \\multicolumn{3}{c}{\\textit{score}}  \\\\\n" +
                "\\textit{benchmark} & \\textit{lines of code} & \\multicolumn{1}{l}{\\textit{f-measure}} & \\multicolumn{1}{l}{\\textit{recall}} & \\multicolumn{1}{l}{\\textit{precision}} \\\\ \\hline\n");
        List<BenchMark> benches = scores.keySet().stream().sorted((o1, o2) -> o1.name.compareTo(o2.name)).collect(Collectors.toList());
        for (int i = 0; i < benches.size(); i++) {
            BenchMark benchMark = benches.get(i);
            String fMeasure = Util.toFixed(scores.get(benchMark).fMeasure, 3);
            String precision = Util.toFixed(scores.get(benchMark).precision, 3);
            String recall = Util.toFixed(scores.get(benchMark).recall, 3);
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
