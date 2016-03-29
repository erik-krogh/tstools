package dk.webbies.tscreate.cleanup.heuristics;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.webbies.tscreate.Score;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.InterfaceDeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.UnnamedObjectType;
import dk.webbies.tscreate.cleanup.CollectEveryTypeVisitor;
import dk.webbies.tscreate.cleanup.DeclarationTypeToTSTypes;
import dk.webbies.tscreate.cleanup.RedundantInterfaceCleaner;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.evaluation.Evaluation;
import dk.webbies.tscreate.util.Util;

import java.util.*;

import static dk.webbies.tscreate.cleanup.heuristics.HeuristicsUtil.*;
import static dk.webbies.tscreate.cleanup.heuristics.HeuristicsUtil.hasDynAccess;
import static dk.webbies.tscreate.cleanup.heuristics.HeuristicsUtil.hasObject;
import static dk.webbies.tscreate.cleanup.heuristics.HeuristicsUtil.numberOfFields;

/**
 * Created by erik1 on 15-03-2016.
 */
public class CombineInterfacesHeuristic implements ReplacementHeuristic{

    private final DeclarationParser.NativeClassesMap nativeClasses;
    private final DeclarationTypeToTSTypes decsToTS;
    private RedundantInterfaceCleaner redundantInterfaceCleaner;

    public CombineInterfacesHeuristic(DeclarationParser.NativeClassesMap nativeClasses, DeclarationTypeToTSTypes decsToTS, RedundantInterfaceCleaner redundantInterfaceCleaner) {
        this.nativeClasses = nativeClasses;
        this.decsToTS = decsToTS;
        this.redundantInterfaceCleaner = redundantInterfaceCleaner;
    }

    @Override
    public Multimap<DeclarationType, DeclarationType> findReplacements(CollectEveryTypeVisitor collected) {
        ArrayListMultimap<DeclarationType, DeclarationType> replacements = ArrayListMultimap.create();

        Map<Class<? extends DeclarationType>, Set<DeclarationType>> byType = collected.getEverythingByType();

        List<DeclarationType> interfaces = Util.concat(byType.get(InterfaceDeclarationType.class), byType.get(UnnamedObjectType.class));

        for (int i = 0; i < interfaces.size(); i++) {
            DeclarationType one = interfaces.get(i);
            for (int j = 0; j < interfaces.size(); j++) {
                if (i == j) {
                    continue;
                }
                DeclarationType two = interfaces.get(j);
                Evaluation similarity = redundantInterfaceCleaner.evaluteSimilarity(one, two, decsToTS, nativeClasses);
                Score score = similarity.score(true);
                if (score.precision > 0.7 && score.recall > 0.7) {
                    if (score.fMeasure >= 0.85) {
                        combine(one, two, replacements);
                    } else if (score.precision == 1 || score.recall == 1){
                        combine(one, two, replacements);
                    } else if (!hasObject(one) && !hasObject(two)){
                        continue;
                    } else if (hasDynAccess(one) ^/*XOR*/ hasDynAccess(two) && numberOfFields(one) + numberOfFields(two) <= 5 && score.fMeasure < 0.9) { // If only one has dynamic access, I think they should be quite similar before i merge them.
                        continue;
                    } else if (hasObject(one) ^/*XOR*/ hasObject(two) && numberOfFields(one) + numberOfFields(two) <= 3 && score.fMeasure < 0.9) {
                        continue;
                    } else if (hasDynAccess(one) && hasDynAccess(two) && Math.min(numberOfFields(one), numberOfFields(two)) <= 2 && score.fMeasure < 0.9) {
                        continue;
                    } else if (hasDynAccess(one) && !hasDynAccess(two) && numberOfFields(one) > numberOfFields(two) && score.recall >= 0.8) {
                        combine(one, two, replacements);
                    } else if (Math.min(numberOfFields(one), numberOfFields(two)) >= 4 && score.fMeasure >= 0.7) {
                        combine(one, two, replacements);
                    } else if (Math.min(numberOfFields(one), numberOfFields(two)) >= 4 && score.recall >= 0.95) {
                        combine(one, two, replacements);
                    } else if (Math.max(numberOfFields(one), numberOfFields(two)) <= 2 && score.fMeasure <= 0.7) { // Only one of the fields are similar.
                        continue;
                    } else if (Math.max(numberOfFields(one), numberOfFields(two)) <= 2 && score.fMeasure >= 0.8) {
                        combine(one, two, replacements);
                    } else if (Math.max(numberOfFields(one), numberOfFields(two)) <= 5 && score.fMeasure <= 0.8) {
                        continue;
                    } else if (hasObject(one) && hasObject(two) && numberOfFields(two) > numberOfFields(one) && score.precision >= 0.8) {
                        combine(one, two, replacements);
                    } else if (hasObject(one) && hasObject(two) && numberOfFields(one) > numberOfFields(two) && score.recall >= 0.8) {
                        combine(one, two, replacements);
                    } else if (Math.min(numberOfFields(one), numberOfFields(two)) >= 5 && score.fMeasure <= 0.8) {
                        continue;
                    } else {
                        System.out.println("Found something similar");
                    }
                }
            }
        }

        return replacements;
    }

    @Override
    public String getDescription() {
        return "interface == interface";
    }
}
