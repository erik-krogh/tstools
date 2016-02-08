package dk.webbies.tscreate.analysis.methods.pureSubsets;

import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.HeapValueFactory;
import dk.webbies.tscreate.analysis.SubsetHeapValueFactory;
import dk.webbies.tscreate.analysis.methods.mixed.MixedTypeAnalysis;
import dk.webbies.tscreate.analysis.unionFind.FunctionNode;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.analysis.unionFind.UnionNode;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by erik1 on 05-02-2016.
 */
public class PureSubsetsTypeAnalysis extends MixedTypeAnalysis {
    public PureSubsetsTypeAnalysis(HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj globalObject, DeclarationParser.NativeClassesMap nativeClasses) {
        super(libraryClasses, options, globalObject, nativeClasses);
    }

    @Override
    public void applyConstraints(Snap.Obj closure, Map<Snap.Obj, FunctionNode> functionNodes, UnionFindSolver solver, FunctionNode functionNode, HeapValueFactory heapFactory, Map<Identifier, UnionNode> identifierMap) {
        new PureSubsetsConstraintVisitor(closure, solver, identifierMap, functionNode, functionNodes, heapFactory, this, this.nativeTypeFactory).visit(closure.function.astNode);
    }
}
