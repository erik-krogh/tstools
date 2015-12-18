package dk.webbies.tscreate;

import com.google.javascript.jscomp.parsing.parser.Parser;
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
        long start = System.currentTimeMillis();

        runAnalysis(BenchMark.D3);
//
//        for (BenchMark benchmark : BenchMark.allBenchmarks) {
//            runAnalysis(benchmark);
//        }

        long end = System.currentTimeMillis();

        System.out.println("Ran in " + (end - start) + "ms");
        System.exit(0);
    }

    public static void runAnalysis(BenchMark benchMark) throws IOException {
        System.out.println("Analysing " + benchMark.name);
        String resultDeclarationFilePath = benchMark.scriptPath + ".gen.d.ts";

        String script = Util.readFile(benchMark.scriptPath);

        FunctionExpression program = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, script).toTSCreateAST();

        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.options, benchMark.dependencyScripts()), program);

        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObject).extract();

        NativeClassesMap nativeClasses = parseNatives(globalObject, benchMark.languageLevel.environment, benchMark.dependencyDeclarations(), libraryClasses);

        Snap.Obj emptySnap = JSNAPUtil.getEmptyJSnap(benchMark.options, benchMark.dependencyScripts(), program); // Not empty, just the one without the library we are analyzing.
        Map<String, DeclarationType> declaration = new DeclarationBuilder(emptySnap, libraryClasses, benchMark.options, globalObject, nativeClasses).buildDeclaration();

        String printedDeclaration = new DeclarationPrinter(declaration, nativeClasses).print();
        System.out.println(printedDeclaration);

        Util.writeFile(resultDeclarationFilePath, printedDeclaration);

        if (benchMark.declarationPath != null) {
            // FIXME: Doesn't handle dependencies yet.
            Evaluation evaluation = new DeclarationEvaluator(resultDeclarationFilePath, benchMark, globalObject, libraryClasses).getEvaluation();

            System.out.println(evaluation.print());
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
