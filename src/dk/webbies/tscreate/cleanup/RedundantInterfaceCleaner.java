package dk.webbies.tscreate.cleanup;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.cleanup.heuristics.*;
import dk.webbies.tscreate.evaluation.DeclarationEvaluator;
import dk.webbies.tscreate.evaluation.Evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public void runHeuristics() {
        boolean progress = true;
        int counter = 0;

        // Just making things cleaner.
        cleanDeclarations();
//        reducer.clearCache();

        SetMultimap<DeclarationType, DeclarationType> allReplacements = HashMultimap.create();

        while (progress) {
            CollectEveryTypeVisitor collector = new CollectEveryTypeVisitor(declaration, false);
            System.out.println("Removing redundant types (" + counter++ + ")   decs:(" + collector.getEveryThing().size() + ")");

            progress = false;
            for (ReplacementHeuristic heuristic : heuristics) {
                Multimap<DeclarationType, DeclarationType> replacements = heuristic.findReplacements(collector);
                if (replacements == null || replacements.isEmpty()) {
                    continue;
                }
                collector = new CollectEveryTypeVisitor(declaration, false);
                replacements.keySet().forEach(allReplacements::removeAll);
                allReplacements.putAll(replacements);
                System.out.println("Found redundant types using: " + heuristic.getDescription());
                progress = true;
                for (DeclarationType replacement : allReplacements.values()) {
                    replacement.accept(collector);
                }

                new InplaceDeclarationReplacer(allReplacements, collector, reducer, declaration).cleanStuff();
//                reducer.clearCache();
                break;
            }
        }
    }

    public void cleanDeclarations() {
        CollectEveryTypeVisitor collector = new CollectEveryTypeVisitor(declaration, false);
        new InplaceDeclarationReplacer(ArrayListMultimap.create(), collector, reducer, declaration).cleanStuff();
    }

    public Evaluation evaluteSimilarity(DeclarationType candidateDec, DeclarationType truthDec, DeclarationTypeToTSTypes decsToTypes, NativeClassesMap nativeClasses) {
        Type candidateDeclaration = decsToTypes.getType(candidateDec);
        Type truthDeclaration = decsToTypes.getType(truthDec);


        Options options = new Options();
        options.maxEvaluationDepth = 2;
        options.debugPrint = false;
        options.evaluationSkipExcessProperties = false;
        options.evaluationAnyAreOK = true;
        Set<Type> nativeTypes = nativeClasses.nativeTypes();
        return DeclarationEvaluator.getEvaluation(options, truthDeclaration, candidateDeclaration, nativeTypes, nativeClasses, nativeClasses, nativeClasses, true);
    }
}
