package dk.webbies.tscreate.cleanup.heuristics;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.webbies.tscreate.Score;
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
    private RedundantInterfaceCleaner redundantInterfaceCleaner;

    public ReplaceInterfaceWithClassInstanceHeuristic(DeclarationParser.NativeClassesMap nativeClasses, TypeReducer combiner, DeclarationTypeToTSTypes decsToTypes, RedundantInterfaceCleaner redundantInterfaceCleaner) {
        this.nativeClasses = nativeClasses;
        this.reducer = combiner;
        this.decsToTypes = decsToTypes;
        this.redundantInterfaceCleaner = redundantInterfaceCleaner;
    }

    @Override
    public Multimap<DeclarationType, DeclarationType> findReplacements(CollectEveryTypeVisitor collected) {
        Map<Class<? extends DeclarationType>, Set<DeclarationType>> byType = collected.getEverythingByType();

        List<DeclarationType> classTypes = getClassAndNamedTypes(byType);

        Multimap<DeclarationType, DeclarationType> replacements = ArrayListMultimap.create();

        List<DeclarationType> objects = Util.concat(byType.get(UnnamedObjectType.class), byType.get(InterfaceDeclarationType.class), byType.get(FunctionType.class));

        for (DeclarationType candidate : objects) {
            List<Pair<DeclarationType, Evaluation>> possibleReplacement = getPossibleReplacements(classTypes, candidate);
            if (possibleReplacement.isEmpty()) {
                continue;
            }

            Collections.sort(possibleReplacement, (a, b) -> Double.compare(b.right.score(true).precision, a.right.score(true).precision));

            Pair<DeclarationType, Evaluation> bestPossible = possibleReplacement.get(0);

            Score score = bestPossible.right.score(true);
            if (score.precision < 0.7) {
                continue;
            }

            if (possibleReplacement.size() >= 2 && score.precision == possibleReplacement.get(1).right.score(true).precision) {
                // We got at least two that are equally good.
                List<DeclarationType> equallyGood = possibleReplacement.stream().filter(pair -> pair.right.score(true).precision == score.precision).map(pair -> pair.left).collect(Collectors.toList());

                DeclarationType combined = new CombinationType(reducer, equallyGood).getCombined();
                if (!(combined instanceof UnionDeclarationType)) {
                    replacements.put(candidate, combined);
                }

            } else {
                replacements.put(candidate, bestPossible.left);
            }
        }

        return replacements;
    }

    @Override
    public String getDescription() {
        return "Interface -> ClassInstance";
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
            Evaluation eval = redundantInterfaceCleaner.evaluteSimilarity(candidate, truth, decsToTypes, nativeClasses);
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
