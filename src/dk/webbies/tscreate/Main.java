package dk.webbies.tscreate;

import com.google.javascript.jscomp.parsing.parser.Parser;
import dk.webbies.tscreate.analysis.declarations.DeclarationBlock;
import dk.webbies.tscreate.analysis.declarations.DeclarationBuilder;
import dk.webbies.tscreate.analysis.declarations.DeclarationToStringVisitor;
import dk.webbies.tscreate.analysis.declarations.types.FunctionType;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.jsnapconvert.classes.ClassHierarchyExtractor;
import dk.webbies.tscreate.jsnapconvert.JSNAPConverter;
import dk.webbies.tscreate.jsnapconvert.Snap;
import dk.webbies.tscreate.jsnapconvert.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.paser.JavaScriptParser;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 01-09-2015.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        runAnalysis("Test script", "tests/underscore.js");
        long end = System.currentTimeMillis();
        System.out.println("Ran in " + (end-start) + "ms");
    }

    public static void runAnalysis(String name, String path) throws IOException {
        String script = getJavaScript(path);
        FunctionExpression program = new JavaScriptParser(Parser.Config.Mode.ES5).parse(name, script).toTSCreateAST();
        Snap.Obj globalObject = JSNAPConverter.getStateDump(getJsnapRaw(path), program);
        Snap.Obj librarySnap = JSNAPConverter.extractUnique(globalObject);
        HashMap<Snap.Obj, LibraryClass> classes = new ClassHierarchyExtractor(librarySnap).extract();

        // TODO: Make sure that all instances of a class is unioned together.
        Map<Snap.Obj, FunctionType> functionTypes = new TypeAnalysis(librarySnap, classes, program).getFunctionTypes();

        DeclarationBlock declaration = new DeclarationBuilder(librarySnap, classes, functionTypes).buildDeclaration();

        DeclarationToStringVisitor stringBuilder = new DeclarationToStringVisitor();
        stringBuilder.visit(declaration);
        System.out.println(stringBuilder.getResult());

    }

    private static String getJavaScript(String path) throws IOException {
        FileReader reader = new FileReader(new File(path));
        String result = IOUtils.toString(reader);
        reader.close();
        return result;
    }

    private static String getJsnapRaw(String path) throws IOException {
        File jsFile = new File(path);
        if (!jsFile.exists()) {
            throw new RuntimeException("Cannot create a snapshot of a file that doesn't exist");
        }
        long jsLastModified = jsFile.lastModified();
        File jsnapFile = new File(path + ".jsnap");
        boolean recreate = false;
        if (!jsnapFile.exists()) {
            recreate = true;
        } else {
            long jsnapLastModified = jsnapFile.lastModified();
            if (jsnapLastModified < jsLastModified) {
                recreate = true;
            }
        }

        if (recreate) {
            System.out.println("Creating JSNAP from scratch. \n");
            String jsnap = Util.runNodeScript("lib/tscheck/node_modules/jsnap/jsnap.js " + path);
            FileWriter writer = new FileWriter(jsnapFile);
            IOUtils.write(jsnap, writer);
            writer.close();
            return jsnap;
        } else {
            FileReader reader = new FileReader(jsnapFile);
            String result = IOUtils.toString(reader);
            reader.close();
            return result;
        }

    }
}
