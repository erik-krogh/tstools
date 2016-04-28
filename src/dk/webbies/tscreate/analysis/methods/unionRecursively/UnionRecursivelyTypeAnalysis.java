package dk.webbies.tscreate.analysis.methods.unionRecursively;

import dk.au.cs.casa.typescript.types.Signature;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.HeapValueFactory;
import dk.webbies.tscreate.analysis.methods.mixed.MixedTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.old.analysis.unionFind.HeapValueNode;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.Identifier;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
@SuppressWarnings("Duplicates")
public class UnionRecursivelyTypeAnalysis extends MixedTypeAnalysis {
    private final Snap.Obj globalObject;

    public UnionRecursivelyTypeAnalysis(HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj globalObject, NativeClassesMap nativeClasses) {
        super(libraryClasses, options, globalObject, nativeClasses, false);
        this.globalObject = globalObject;
    }

    @Override
    public void applyConstraints(Snap.Obj closure, Map<Snap.Obj, FunctionNode> functionNodes, UnionFindSolver solver, FunctionNode functionNode, HeapValueFactory heapFactory, Map<Identifier, UnionNode> identifierMap) {
        new UnionRecursivelyConstraintVisitor(closure, solver, identifierMap, functionNode, functionNodes, heapFactory, this.nativeTypeFactory, this).visit(closure.function.astNode);
    }

    @Override
    public void analyse(Snap.Obj closure, Map<Snap.Obj, FunctionNode> originalFunctionNodes, UnionFindSolver solver, FunctionNode functionNode, HeapValueFactory heapValueFactory) {
        HashMap<Snap.Obj, FunctionNode> functionNodes = new HashMap<>();
        functionNodes.put(closure, functionNode);
        this.analyseKeepFunctionNodes(closure, functionNodes, solver, functionNode, new HeapValueNode.Factory(globalObject, solver, libraryClasses, this));
    }

    void analyseKeepFunctionNodes(Snap.Obj closure, Map<Snap.Obj, FunctionNode> functionNodes, UnionFindSolver solver, FunctionNode functionNode, HeapValueFactory heapFactory) {
        super.analyse(closure, functionNodes, solver, functionNode, heapFactory);
    }

    @Override
    public void analyseFunctions() {
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

            solver.finish();
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
}
