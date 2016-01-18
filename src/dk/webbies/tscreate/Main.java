package dk.webbies.tscreate;

import com.google.javascript.jscomp.parsing.parser.Parser;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.analysis.TypeFactory;
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
import java.util.Map;

import static dk.webbies.tscreate.declarationReader.DeclarationParser.*;

/**
 * Created by Erik Krogh Kristensen on 01-09-2015.
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            long start = System.currentTimeMillis();

//            runAnalysis(BenchMark.test);

            benchAll();

            long end = System.currentTimeMillis();

            System.out.println("Ran in " + (end - start) + "ms");
        } catch (Throwable e) {
            System.err.println("Crashed: ");
            e.printStackTrace(System.err);
        } finally {
            System.exit(0);
        }
    }

    private static void benchAll() throws IOException {
        double combinedScore = 0;
        for (BenchMark benchmark : BenchMark.allBenchmarks) {
            double score = runAnalysis(benchmark);
            if (!Double.isNaN(score)) {
                combinedScore += score;
            }
        }

        System.out.println();
        System.out.println("Combined score: " + combinedScore);
    }

    public static double runAnalysis(BenchMark benchMark) throws IOException {
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
        TypeFactory typeFactory = typeAnalysis.getTypeFactory();

        Map<String, DeclarationType> declaration = new DeclarationBuilder(emptySnap, globalObject, typeFactory).buildDeclaration();

        String printedDeclaration = new DeclarationPrinter(declaration, nativeClasses).print();
//        System.out.println(printedDeclaration);

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
            return Double.NaN;
        } else {
//            Util.writeFile(resultDeclarationFilePath, printedDeclaration + "\n\n" + evaluation.toString() + "\n");
            return evaluation.score();
        }

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
