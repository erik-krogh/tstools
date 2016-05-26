package dk.webbies.tscreate.analysis.methods.unionEverything;

import dk.au.cs.casa.typescript.types.Signature;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.*;
import dk.webbies.tscreate.analysis.methods.mixed.MixedTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.old.analysis.unionFind.HeapValueNode;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.*;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
@SuppressWarnings("Duplicates")
public class UnionEverythingTypeAnalysis extends MixedTypeAnalysis {

    private HashMap<Snap.Obj, LibraryClass> libraryClasses;
    private Snap.Obj globalObject;

    public UnionEverythingTypeAnalysis(HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj globalObject, NativeClassesMap nativeClasses, Map<AstNode, Set<Snap.Obj>> callsites) {
        super(libraryClasses, options, globalObject, nativeClasses, false, callsites);
        this.libraryClasses = libraryClasses;
        this.globalObject = globalObject;
        this.heapFactory = new HeapValueNode.Factory(globalObject, solver, libraryClasses, this);
        this.nativeTypeFactory = new NativeTypeFactory(heapFactory.getPrimitivesFactory(), solver, nativeClasses);
    }

    @Override
    public void applyConstraints(Snap.Obj closure, Map<Snap.Obj, FunctionNode> functionNodes, UnionFindSolver solver, FunctionNode functionNode, HeapValueFactory heapFactory, Map<Identifier, UnionNode> identifierMap) {
        new UnionEverythingConstraintVisitor(closure, solver, identifierMap, functionNode, functionNodes, heapFactory, this, this.nativeTypeFactory, callsites).visit(closure.function.astNode);
    }

    @Override
    public void analyseFunctions() {
        System.out.println("Analyzing " + functionNodes.size() + " functions");

        int counter = 0;
        for (Snap.Obj closure : functionNodes.keySet()) {
//            System.out.println("Analysis: " + ++counter + "/" + functionNodes.size());
            FunctionNode functionNode = functionNodes.get(closure);

            if (options.skipStaticAnalysisWhenPossible) {
                if (canGetEverythingFromRecordedCalls(closure)) {
                    addCallsToFunction(closure, solver, functionNode, heapFactory);
                } else {
                    analyse(closure, functionNodes, solver, functionNode, heapFactory);
                }
            } else {
                analyse(closure, functionNodes, solver, functionNode, heapFactory);
            }
        }

        solver.finish(); // Way faster to wait, and do it here.

        System.out.println("Resolving native functions");

        for (Snap.Obj closure : this.nativeFunctions) {
            UnionNode node = new EmptyNode(solver);
            for (Signature signature : closure.function.callSignatures) {
                FunctionNode functionNode = nativeTypeFactory.fromSignature(signature);
                solver.union(node, new IncludeNode(solver, functionNode));
            }

            List<UnionFeature> features = UnionFeature.getReachable(node.getFeature());
            typeFactory.registerFunction(closure, features);
        }

        solver.finish();

        System.out.println("Collapsing cycles");

        solver.collapseCycles();

        this.analysisFinished = true;

        counter = 0;
        for (Map.Entry<Snap.Obj, FunctionNode> entry : functionNodes.entrySet()) {
//            System.out.println("Inference: " + ++counter + "/" + functionNodes.size());
            Snap.Obj closure = entry.getKey();
            FunctionNode functionNode = entry.getValue();

            typeFactory.registerFunction(closure, Collections.singletonList(functionNode.getFeature()));
            typeFactory.currentClosure = closure;

            // This is a very ugly hack to make it actually infer a type-function, no-matter what garbage gets unified with the function.
            UnionFeature feature = new UnionClass(solver, FunctionNode.create(entry.getKey(), solver)).getFeature();
            feature.getFunctionFeature().takeIn(functionNode.getFeature().getFunctionFeature());

            typeFactory.putResolvedFunctionType(closure, typeFactory.getTypeNoCache(feature));
            typeFactory.currentClosure = null;
        }

        System.out.println("Resolving class types");

        typeFactory.resolveClassTypes();

    }

    @Override
    public HeapValueFactory getHeapFactory() {
        return new HeapValueNode.Factory(globalObject, solver, libraryClasses, this);
    }

    @Override
    public void analyse(Snap.Obj closure, Map<Snap.Obj, FunctionNode> functionNodes, UnionFindSolver solver, FunctionNode functionNode, HeapValueFactory heapFactory) {
        super.analyse(closure, functionNodes, solver, functionNode, heapFactory);
    }

}
