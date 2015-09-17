package dk.webbies.tscreate;

import com.google.javascript.jscomp.parsing.parser.Parser;
import dk.webbies.tscreate.analysis.declarations.DeclarationBlock;
import dk.webbies.tscreate.analysis.declarations.DeclarationBuilder;
import dk.webbies.tscreate.analysis.declarations.DeclarationToStringVisitor;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.ClassHierarchyExtractor;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.paser.JavaScriptParser;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Erik Krogh Kristensen on 01-09-2015.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        Options options = Options.separateFunctions();
        options.unionShortCircuitLogic = false;
        runAnalysis("Test script", "tests/test.js", options, LanguageLevel.ES6);
        long end = System.currentTimeMillis();
        System.out.println("Ran in " + (end - start) + "ms");
    }

    public static void runAnalysis(String name, String path, Options options, LanguageLevel language) throws IOException {
        String script = Util.readFile(path);
        FunctionExpression program = new JavaScriptParser(language.closureCompilerMode).parse(name, script).toTSCreateAST();
        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(path), program);

        DeclarationParser.markNatives(globalObject, language.declarationMode);

        Snap.Obj librarySnap = JSNAPUtil.extractUnique(globalObject);
        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(librarySnap).extract();

        DeclarationBlock declaration = new DeclarationBuilder(librarySnap, libraryClasses, options, globalObject).buildDeclaration();

        BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(new File(path + ".gen.d.ts")));
        TeeOutputStream out = new TeeOutputStream(fileOut, System.out);
        new DeclarationToStringVisitor(out).visit(declaration);
        fileOut.close();
    }

    public enum LanguageLevel {
        ES5(Parser.Config.Mode.ES5, DeclarationParser.Environment.ES5DOM),
        ES6(Parser.Config.Mode.ES6, DeclarationParser.Environment.ES6DOM);

        public final Parser.Config.Mode closureCompilerMode;
        public final DeclarationParser.Environment declarationMode;

        LanguageLevel(Parser.Config.Mode closure, DeclarationParser.Environment declaration) {
            this.closureCompilerMode = closure;
            this.declarationMode = declaration;
        }
    }
}
