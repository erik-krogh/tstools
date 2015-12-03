package dk.webbies.tscreate;

import com.google.javascript.jscomp.parsing.parser.Parser;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.analysis.declarations.DeclarationBuilder;
import dk.webbies.tscreate.analysis.declarations.DeclarationToString;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.evaluation.DeclarationEvaluator;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.ClassHierarchyExtractor;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.paser.JavaScriptParser;

import dk.webbies.tscreate.paser.SSA;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 01-09-2015.
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Options options = new Options();
        options.includeThisNodeFromHeap = true;
        options.includeThisNodeFromConstructor = false;
        options.includeThisNodeFromPrototypeMethods = false;

        options.interProceduralAnalysisWithHeap = false;

        long start = System.currentTimeMillis();

        runAnalysis("Test script", "tests/ssa/if.js", null, options, LanguageLevel.ES5);

        long end = System.currentTimeMillis();

        System.out.println("Ran in " + (end - start) + "ms");

        System.exit(0);
    }

    public static void runAnalysis(String name, String scriptPath, String declarationPath, Options options, LanguageLevel languageLevel) throws IOException {
        String resultDeclarationFilePath = scriptPath + ".gen.d.ts";

        String script = Util.readFile(scriptPath);
        FunctionExpression program = SSA.toSSA_(new JavaScriptParser(languageLevel.closureCompilerMode).parse(name, script).toTSCreateAST()); System.exit(0);
        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(scriptPath), program);

        Map<Type, String> typeNames = DeclarationParser.markNatives(globalObject, languageLevel.environment);

        Snap.Obj librarySnap = JSNAPUtil.extractUnique(globalObject);
        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObject).extract();

        Map<String, DeclarationType> declaration = new DeclarationBuilder(librarySnap, libraryClasses, options, globalObject, typeNames).buildDeclaration();

        BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(new File(resultDeclarationFilePath)));
        TeeOutputStream out = new TeeOutputStream(fileOut, System.out);
        new DeclarationToString(out, declaration).print();
        fileOut.close();

        if (declarationPath != null) {
            DeclarationEvaluator.Evaluation evaluation = new DeclarationEvaluator(resultDeclarationFilePath, declarationPath, languageLevel.environment).createEvaluation();

            System.out.println(evaluation.toString());
        }

    }

    public enum LanguageLevel {
        ES5(Parser.Config.Mode.ES5, DeclarationParser.Environment.ES5DOM),
        ES6(Parser.Config.Mode.ES6, DeclarationParser.Environment.ES6DOM);

        public final Parser.Config.Mode closureCompilerMode;
        public final DeclarationParser.Environment environment;

        LanguageLevel(Parser.Config.Mode closure, DeclarationParser.Environment declaration) {
            this.closureCompilerMode = closure;
            this.environment = declaration;
        }
    }
}
