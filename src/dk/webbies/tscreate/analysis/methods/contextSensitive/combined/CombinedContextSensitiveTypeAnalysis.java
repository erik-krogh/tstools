package dk.webbies.tscreate.analysis.methods.contextSensitive.combined;

import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.HeapValueFactory;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.analysis.TypeFactory;
import dk.webbies.tscreate.analysis.methods.contextSensitive.mixed.MixedContextSensitiveTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.contextSensitive.pureSubsets.PureSubsetsContextSensitiveTypeAnalysis;
import dk.webbies.tscreate.analysis.unionFind.FunctionNode;
import dk.webbies.tscreate.analysis.unionFind.IncludeNode;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.AstNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by erik1 on 24-02-2016.
 */
@SuppressWarnings("Duplicates")
public class CombinedContextSensitiveTypeAnalysis implements TypeAnalysis {
    private final MixedContextSensitiveTypeAnalysis mixed;
    private final PureSubsetsContextSensitiveTypeAnalysis subset;
    private final TypeFactory typeFactory;

    public CombinedContextSensitiveTypeAnalysis(HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj globalObject, DeclarationParser.NativeClassesMap nativeClasses, boolean upperBoundMethod, Map<AstNode, Set<Snap.Obj>> callsites) {
        mixed = new MixedContextSensitiveTypeAnalysis(libraryClasses, options, globalObject, nativeClasses, upperBoundMethod, callsites);
        subset = new PureSubsetsContextSensitiveTypeAnalysis(libraryClasses, options, globalObject, nativeClasses, callsites);

        typeFactory = new CombinerTypeFactory(globalObject, libraryClasses, options, nativeClasses, this);
        mixed.typeFactory = typeFactory;
        subset.typeFactory = typeFactory;
    }

    @Override
    public void analyseFunctions() {
        System.out.println("Running combined, mixed");
        mixed.analyseFunctions();
        System.out.println("Running combined, subsets");
        subset.analyseFunctions();
    }

    @Override
    public TypeFactory getTypeFactory() {
        return typeFactory;
    }

    @Override
    public Options getOptions() {
        return mixed.getOptions();
    }

    @Override
    public Map<Snap.Obj, LibraryClass> getLibraryClasses() {
        return mixed.getLibraryClasses();
    }

    @Override
    public DeclarationParser.NativeClassesMap getNativeClasses() {
        return mixed.getNativeClasses();
    }

    // Just used for getters and setters, not that important.
    @Override
    public FunctionNode getFunctionNode(Snap.Obj closure) {
        FunctionNode mixed = this.mixed.getFunctionNode(closure);
        FunctionNode subset = this.subset.getFunctionNode(closure);
        if (mixed == null && subset == null) {
            return null;
        }
        if (mixed == null) {
            return subset;
        }
        if (subset == null) {
            return mixed;
        }
        UnionFindSolver solver = this.mixed.solver;
        FunctionNode result = FunctionNode.create(closure, solver);
        solver.union(result.thisNode, new IncludeNode(solver, mixed.thisNode, subset.thisNode));
        solver.union(result.returnNode, new IncludeNode(solver, mixed.returnNode, subset.returnNode));
        for (int i = 0; i < result.arguments.size(); i++) {
            solver.union(result.arguments.get(i), new IncludeNode(solver, mixed.arguments.get(i), subset.arguments.get(i)));
        }
        return result;
    }

    @Override
    public HeapValueFactory getHeapFactory() {
        return mixed.getHeapFactory();
    }

    @Override
    public UnionFindSolver getSolver() {
        return mixed.solver;
    }

    @Override
    public Snap.Obj getGlobalObject() {
        return mixed.getGlobalObject();
    }
}
