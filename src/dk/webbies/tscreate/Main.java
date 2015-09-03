package dk.webbies.tscreate;

import dk.webbies.tscreate.analysis.typeDeclaration.DeclarationBlock;
import dk.webbies.tscreate.analysis.typeDeclaration.DeclarationBuilder;
import dk.webbies.tscreate.analysis.typeDeclaration.FunctionType;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.jsnapconvert.classes.ClassHierarchyExtrator;
import dk.webbies.tscreate.jsnapconvert.JSNAPConverter;
import dk.webbies.tscreate.jsnapconvert.Snap;
import dk.webbies.tscreate.jsnapconvert.classes.LibraryClass;
import dk.webbies.tscreate.paser.JavaScriptParser;
import dk.webbies.tscreate.paser.Program;
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
        runAnalysis("Test script", "tests/test.js");
    }

    public static void runAnalysis(String name, String path) throws IOException {
        String script = getJavaScript(path);
        Program program = new JavaScriptParser(name, script).parse();
        Snap.Obj globalObject = JSNAPConverter.getStateDump(getJsnapRaw(path), program);
        Snap.Obj librarySnap = JSNAPConverter.extractUnique(globalObject);
        HashMap<Snap.Obj, LibraryClass> classes = new ClassHierarchyExtrator(librarySnap).extract();

        Map<Snap.Obj, FunctionType> functionTypes = new TypeAnalysis(librarySnap, classes, program).getFunctionTypes();

        DeclarationBlock declaration = new DeclarationBuilder(librarySnap, classes, functionTypes).buildDeclaration();

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
            System.out.println("Creating JSNAP from scratch. ");
            String jsnap = Util.runNodeScript("lib/tscheck/node_modules/jsnap/jsnap.js " + path);
            FileWriter writer = new FileWriter(jsnapFile);
            IOUtils.write(jsnap, writer);
            writer.close();
            return jsnap;
        } else {
            System.out.println("Using cached JSNAP");
            FileReader reader = new FileReader(jsnapFile);
            String result = IOUtils.toString(reader);
            reader.close();
            return result;
        }

    }
}
