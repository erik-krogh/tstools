package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.unionFind.FunctionNode;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;

import java.util.Map;

/**
 * Created by erik1 on 05-02-2016.
 */
public interface TypeAnalysis {
    void analyseFunctions();

    TypeFactory getTypeFactory();

    Options getOptions();

    Map<Snap.Obj, LibraryClass> getLibraryClasses();

    DeclarationParser.NativeClassesMap getNativeClasses();

    FunctionNode getFunctionNode(Snap.Obj closure);

    HeapValueFactory getHeapFactory();

    UnionFindSolver getSolver();
}
