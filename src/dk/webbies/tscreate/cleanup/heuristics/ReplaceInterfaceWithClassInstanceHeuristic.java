package dk.webbies.tscreate.cleanup.heuristics;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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
    private DeclarationTypeToTSTypes decsToTypes;

    public ReplaceInterfaceWithClassInstanceHeuristic(DeclarationParser.NativeClassesMap nativeClasses) {
        this.nativeClasses = nativeClasses;
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
            Pair<DeclarationType, Evaluation> replacement = Collections.max(possibleReplacement, (a, b) -> Double.compare(a.second.score(true).precision, b.second.score(true).precision));
            if (replacement.second.score(true).precision >= 1) {
                replacements.put(candidate, replacement.first);
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
        List<DeclarationType> result = byType.get(ClassType.class).stream().map(ClassInstanceType::new).collect(Collectors.toList());
        nativeClasses.getNativeTypeNames().stream().map(name -> new NamedObjectType(name, false)).forEach(result::add);
        return result;
    }

}
