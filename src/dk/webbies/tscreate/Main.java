package dk.webbies.tscreate;

import com.google.javascript.jscomp.parsing.parser.Parser;
import dk.webbies.tscreate.analysis.declarations.DeclarationBuilder;
import dk.webbies.tscreate.analysis.declarations.DeclarationPrinter;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.evaluation.DeclarationEvaluator;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.ClassHierarchyExtractor;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.paser.JavaScriptParser;
import dk.webbies.tscreate.paser.SSA;
import dk.webbies.tscreate.util.MultiOutputStream;
import dk.webbies.tscreate.util.Util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static dk.webbies.tscreate.declarationReader.DeclarationParser.*;

/**
 * Created by Erik Krogh Kristensen on 01-09-2015.
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Options options = new Options();

        options.interProceduralAnalysisWithHeap = true;

        long start = System.currentTimeMillis();

        options.createInstances = false; // This doesn't work with jQuery.
        runAnalysis("Test script", "tests/test.js", null, options, LanguageLevel.ES5);

        long end = System.currentTimeMillis();

        System.out.println("Ran in " + (end - start) + "ms");

        System.exit(0);
    }

    public static void runAnalysis(String name, String scriptPath, String declarationPath, Options options, LanguageLevel languageLevel) throws IOException {
        String resultDeclarationFilePath = scriptPath + ".gen.d.ts";

        String script = Util.readFile(scriptPath);
        FunctionExpression program = SSA.toSSA(new JavaScriptParser(languageLevel.closureCompilerMode).parse(name, script).toTSCreateAST());
        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(scriptPath, options), program);

        NativeClassesMap nativeClasses = markNatives(globalObject, languageLevel.environment);

        Snap.Obj librarySnap = JSNAPUtil.extractUnique(globalObject, options);
        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObject).extract();

        Map<String, DeclarationType> declaration = new DeclarationBuilder(librarySnap, libraryClasses, options, globalObject, nativeClasses).buildDeclaration();

        BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(new File(resultDeclarationFilePath)));
        OutputStream out = new MultiOutputStream(fileOut, System.out);
        new DeclarationPrinter(out, declaration).print();
        fileOut.close();

        if (declarationPath != null) {
            DeclarationEvaluator.Evaluation evaluation = new DeclarationEvaluator(resultDeclarationFilePath, declarationPath, languageLevel.environment).createEvaluation();

            System.out.println(evaluation.toString());
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
