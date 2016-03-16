package dk.webbies.tscreate.cleanup;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.cleanup.heuristics.CombineInterfacesHeuristic;
import dk.webbies.tscreate.cleanup.heuristics.FindFunctionsHeuristic;
import dk.webbies.tscreate.cleanup.heuristics.ReplaceInterfaceWithClassInstanceHeuristic;
import dk.webbies.tscreate.cleanup.heuristics.ReplacementHeuristic;
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
    private final Map<String, DeclarationType> declaration;
    private final TypeReducer reducer;
    private final List<ReplacementHeuristic> heuristics = new ArrayList<>();

    public RedundantInterfaceCleaner(Map<String, DeclarationType> declaration, NativeClassesMap nativeClasses, TypeReducer reducer) {
        this.declaration = declaration;
        this.reducer = reducer;

        DeclarationTypeToTSTypes decsToTS = new DeclarationTypeToTSTypes(nativeClasses);

        // The heuristics
        this.heuristics.add(new FindFunctionsHeuristic());
        this.heuristics.add(new ReplaceInterfaceWithClassInstanceHeuristic(nativeClasses, reducer, decsToTS));
        this.heuristics.add(new CombineInterfacesHeuristic(nativeClasses, decsToTS));

    }

    // FIXME: Rules for making stuff like {apply: () => any} to a function.

    // FIXME: Other heuristic, that specifically looks at the similarity of types inside union-types.
    public void clean() {
        boolean progress = true;
        int counter = 0;

        // Just making things cleaner.
        cleanDeclarations();


        while (progress) {
            System.out.println("Removing redundant types (" + counter++ + ")");
            CollectEveryTypeVisitor collector = new CollectEveryTypeVisitor(declaration.values());

            progress = false;
            for (ReplacementHeuristic heuristic : heuristics) {
                Multimap<DeclarationType, DeclarationType> replacements = heuristic.findReplacements(collector);
                if (replacements == null || replacements.isEmpty()) {
                    continue;
                }
                System.out.println("Found redundant types using: " + heuristic.getDescription());
                progress = true;
                Set<DeclarationType> everyThing = collector.getEveryThing();
                InplaceDeclarationReplacer replacer = new InplaceDeclarationReplacer(replacements, everyThing, reducer);
                everyThing.forEach(dec -> dec.accept(replacer));
                break;
            }
        }
    }

    private void cleanDeclarations() {
        Set<DeclarationType> everything = new CollectEveryTypeVisitor(declaration.values()).getEveryThing();
        InplaceDeclarationReplacer replacer = new InplaceDeclarationReplacer(ArrayListMultimap.create(), everything, reducer);
        everything.forEach(dec -> dec.accept(replacer));
    }

    public static Evaluation evaluteSimilarity(DeclarationType candidateDec, DeclarationType truthDec, DeclarationTypeToTSTypes decsToTypes, NativeClassesMap nativeClasses) {
        Type condidateDeclaration = decsToTypes.getType(candidateDec);
        Type truthDeclaration = decsToTypes.getType(truthDec);

        Options options = new Options();
        options.maxEvaluationDepth = 4;
        options.debugPrint = true;
        options.evaluationSkipExcessProperties = false;
        options.evaluationAnyAreOK = true;
        Set<Type> nativeTypes = nativeClasses.nativeTypes();
        return DeclarationEvaluator.evaluate(options, truthDeclaration, condidateDeclaration, nativeTypes, nativeClasses, nativeClasses, nativeClasses);
    }
}
