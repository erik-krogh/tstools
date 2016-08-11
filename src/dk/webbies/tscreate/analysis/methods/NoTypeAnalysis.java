package dk.webbies.tscreate.analysis.methods;

import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.*;
import dk.webbies.tscreate.analysis.methods.mixed.MixedTypeAnalysis;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.*;

import java.util.*;

/**
 * Created by erik1 on 12-05-2016.
 */
public class NoTypeAnalysis extends MixedTypeAnalysis {
    public NoTypeAnalysis(Map<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj globalObject, DeclarationParser.NativeClassesMap nativeClasses, boolean upperBoundMethod, Map<AstNode, Set<Snap.Obj>> callsites) {
        super(libraryClasses, options, globalObject, nativeClasses, upperBoundMethod, callsites);
    }

    @Override
    public void applyConstraints(Snap.Obj closure, Map<Snap.Obj, FunctionNode> functionNodes, UnionFindSolver solver, FunctionNode functionNode, HeapValueFactory heapFactory, Map<Identifier, UnionNode> identifierMap) {
        // Do nothing
    }
}
