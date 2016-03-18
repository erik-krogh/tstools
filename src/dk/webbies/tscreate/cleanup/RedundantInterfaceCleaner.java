package dk.webbies.tscreate.cleanup;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.cleanup.heuristics.*;
import dk.webbies.tscreate.evaluation.DeclarationEvaluator;
import dk.webbies.tscreate.evaluation.Evaluation;
import dk.webbies.tscreate.util.Pair;

import java.util.*;

import static dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;

/**
 * Created by erik1 on 10-03-2016.
 */
public class RedundantInterfaceCleaner {
    private final NativeClassesMap nativeClasses;
    private Map<String, DeclarationType> declaration;
    private final TypeReducer reducer;
    private final List<ReplacementHeuristic> heuristics = new ArrayList<>();
    private final DeclarationTypeToTSTypes decsToTS;

    public RedundantInterfaceCleaner(Map<String, DeclarationType> declaration, NativeClassesMap nativeClasses, TypeReducer reducer) {
        this.declaration = declaration;
        this.reducer = reducer;
        this.nativeClasses = nativeClasses;

        decsToTS = new DeclarationTypeToTSTypes(this.nativeClasses);

        // The heuristics
        this.heuristics.add(new FindFunctionsHeuristic(reducer));
        this.heuristics.add(new ReplaceInterfaceWithClassInstanceHeuristic(nativeClasses, reducer, decsToTS, this));
        this.heuristics.add(new FindInstancesByName(nativeClasses, decsToTS, reducer, this));
        this.heuristics.add(new CombineInterfacesHeuristic(nativeClasses, decsToTS, this));

    }

    // FIXME: Other heuristic, that specifically looks at the similarity of types inside union-types.
    public void clean() {
        boolean progress = true;
        int counter = 0;

        // Just making things cleaner.
        cleanDeclarations();
//        reducer.clearCache();

        while (progress) {
            CollectEveryTypeVisitor collector = new CollectEveryTypeVisitor(declaration.values());
            System.out.println("Removing redundant types (" + counter++ + ")   decs:(" + collector.getEveryThing().size() + ")");

            progress = false;
            for (ReplacementHeuristic heuristic : heuristics) {
                Multimap<DeclarationType, DeclarationType> replacements = heuristic.findReplacements(collector);
                if (replacements == null || replacements.isEmpty()) {
                    continue;
                }
                System.out.println("Found redundant types using: " + heuristic.getDescription());
                progress = true;
                new InplaceDeclarationReplacer(replacements, collector, reducer, declaration).cleanStuff();
//                reducer.clearCache();
                break;
            }
        }
    }

    private void cleanDeclarations() {
        CollectEveryTypeVisitor collector = new CollectEveryTypeVisitor(declaration.values());
        new InplaceDeclarationReplacer(ArrayListMultimap.create(), collector, reducer, declaration).cleanStuff();
    }

    private Map<Pair<Type, Type>, Evaluation> evaluationCache = new HashMap<>();
    public Evaluation evaluteSimilarity(DeclarationType candidateDec, DeclarationType truthDec, DeclarationTypeToTSTypes decsToTypes, NativeClassesMap nativeClasses) {
        Type candidateDeclaration = decsToTypes.getType(candidateDec);
        Type truthDeclaration = decsToTypes.getType(truthDec);

        if (evaluationCache.containsKey(new Pair<>(candidateDeclaration, truthDeclaration))) {
            return evaluationCache.get(new Pair<>(candidateDeclaration, truthDeclaration));
        }

        Options options = new Options();
        options.maxEvaluationDepth = 2;
        options.debugPrint = true;
        options.evaluationSkipExcessProperties = false;
        options.evaluationAnyAreOK = true;
        Set<Type> nativeTypes = nativeClasses.nativeTypes();
        Evaluation result = DeclarationEvaluator.evaluate(options, truthDeclaration, candidateDeclaration, nativeTypes, nativeClasses, nativeClasses, nativeClasses);
        evaluationCache.put(new Pair<>(candidateDeclaration, truthDeclaration), result);
        return result;
    }
}
