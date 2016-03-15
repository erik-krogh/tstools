package dk.webbies.tscreate.cleanup.heuristics;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.cleanup.CollectEveryTypeVisitor;
import dk.webbies.tscreate.cleanup.DeclarationTypeToTSTypes;
import dk.webbies.tscreate.cleanup.RedundantInterfaceCleaner;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.evaluation.Evaluation;
import dk.webbies.tscreate.util.Pair;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 13-03-2016.
 */
public class ReplaceInterfaceWithClassInstanceHeuristic implements ReplacementHeuristic {
    private final DeclarationParser.NativeClassesMap nativeClasses;
    private final TypeReducer reducer;
    private DeclarationTypeToTSTypes decsToTypes;

    public ReplaceInterfaceWithClassInstanceHeuristic(DeclarationParser.NativeClassesMap nativeClasses, TypeReducer combiner) {
        this.nativeClasses = nativeClasses;
        this.reducer = combiner;
        decsToTypes = new DeclarationTypeToTSTypes(this.nativeClasses);
    }

    @Override
    public Multimap<DeclarationType, DeclarationType> findReplacements(CollectEveryTypeVisitor collected) {
        Map<Class<? extends DeclarationType>, Set<DeclarationType>> byType = collected.getEverythingByType();

        List<DeclarationType> classTypes = getClassAndNamedTypes(byType);

        Multimap<DeclarationType, DeclarationType> replacements = ArrayListMultimap.create();

        List<DeclarationType> objects = Util.concat(byType.get(UnnamedObjectType.class), byType.get(InterfaceType.class), byType.get(FunctionType.class));

        // FIXME: Make sure functions and objects are not printed twice in DeclarationPrinter.
        // FIXME: Make sure union-types are "re-evaluated" when running the InplaceReplamentThunghy.

        for (DeclarationType candidate : objects) {
            List<Pair<DeclarationType, Evaluation>> possibleReplacement = getPossibleReplacements(classTypes, candidate);
            if (possibleReplacement.isEmpty()) {
                continue;
            }

            Collections.sort(possibleReplacement, (a, b) -> Double.compare(a.second.score(true).precision, b.second.score(true).precision));

            Pair<DeclarationType, Evaluation> bestPossible = possibleReplacement.get(0);

            if (bestPossible.second.score(true).precision < 0.5) {
                continue; // TODO: What to use?
            }

            if (possibleReplacement.size() >= 2 && bestPossible.second.score(true).precision == possibleReplacement.get(1).second.score(true).precision) {
                // We got at least two that are equally good.
                List<DeclarationType> equallyGood = possibleReplacement.stream().filter(pair -> pair.second.score(true).precision == bestPossible.second.score(true).precision).map(pair -> pair.first).collect(Collectors.toList());

                DeclarationType combined = new CombinationType(reducer, equallyGood).getCombined();
                if (!(combined instanceof UnionDeclarationType)) {
                    replacements.put(candidate, combined);
                }

                System.out.println(bestPossible.second.score(true).precision + " I got two equally good replacements, and I cannot pick one");
            } else {
                replacements.put(candidate, bestPossible.first);
            }
        }

        return replacements;
    }

    private List<Pair<DeclarationType, Evaluation>> getPossibleReplacements(List<DeclarationType> classTypes, DeclarationType candidate) {
        List<Pair<DeclarationType, Evaluation>> possibleReplacement = new ArrayList<>();
        for (DeclarationType truth : classTypes) {
            if (candidate instanceof FunctionType && truth instanceof NamedObjectType) {
                continue; // To many of these are simply false.
            }
            if (candidate == truth) {
                continue;
            }
            Evaluation eval = RedundantInterfaceCleaner.evaluteSimilarity(candidate, truth, decsToTypes, nativeClasses);
            if (eval.score(true).fMeasure == 0) {
                continue;
            }
            possibleReplacement.add(new Pair<>(truth, eval));
        }
        return possibleReplacement;
    }

    private List<DeclarationType> getClassAndNamedTypes(Map<Class<? extends DeclarationType>, Set<DeclarationType>> byType) {
        List<DeclarationType> result = new ArrayList<>();
        if (byType.get(ClassType.class) != null) {
            byType.get(ClassType.class).stream().map(clazz -> new ClassInstanceType(clazz, Collections.EMPTY_SET)).forEach(type -> result.add((DeclarationType)type));
        }
        nativeClasses.getNativeTypeNames().stream().map(name -> new NamedObjectType(name, false)).forEach(result::add);
        return result;
    }

}
