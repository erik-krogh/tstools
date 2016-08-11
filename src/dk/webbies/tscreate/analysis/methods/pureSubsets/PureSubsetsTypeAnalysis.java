package dk.webbies.tscreate.analysis.methods.pureSubsets;

import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.HeapValueFactory;
import dk.webbies.tscreate.analysis.methods.mixed.MixedTypeAnalysis;
import dk.webbies.tscreate.analysis.unionFind.FunctionNode;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.analysis.unionFind.UnionNode;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.AstNode;
import dk.webbies.tscreate.paser.AST.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by erik1 on 05-02-2016.
 */
public class PureSubsetsTypeAnalysis extends MixedTypeAnalysis {
    public PureSubsetsTypeAnalysis(Map<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj globalObject, DeclarationParser.NativeClassesMap nativeClasses, Map<AstNode, Set<Snap.Obj>> callsites) {
        super(libraryClasses, options, globalObject, nativeClasses, false, callsites);
    }

    @Override
    public void applyConstraints(Snap.Obj closure, Map<Snap.Obj, FunctionNode> functionNodes, UnionFindSolver solver, FunctionNode functionNode, HeapValueFactory heapFactory, Map<Identifier, UnionNode> identifierMap) {
        new PureSubsetsConstraintVisitor(closure, solver, identifierMap, functionNode, functionNodes, heapFactory, this, this.nativeTypeFactory, callsites).visit(closure.function.astNode);
    }
}
