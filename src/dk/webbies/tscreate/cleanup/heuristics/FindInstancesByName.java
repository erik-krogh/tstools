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
import dk.webbies.tscreate.util.Pair;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.cleanup.heuristics.HeuristicsUtil.hasDynAccess;
import static dk.webbies.tscreate.cleanup.heuristics.HeuristicsUtil.hasObject;
import static dk.webbies.tscreate.cleanup.heuristics.HeuristicsUtil.numberOfFields;

/**
 * Created by erik1 on 16-03-2016.
 */
public class FindInstancesByName implements ReplacementHeuristic {
    private final DeclarationParser.NativeClassesMap nativeClasses;
    private final DeclarationTypeToTSTypes decsToTypes;
    private final TypeReducer reducer;
    private RedundantInterfaceCleaner redundantInterfaceCleaner;

    public FindInstancesByName(DeclarationParser.NativeClassesMap nativeClasses, DeclarationTypeToTSTypes decsToTypes, TypeReducer reducer, RedundantInterfaceCleaner redundantInterfaceCleaner) {
        this.nativeClasses = nativeClasses;
        this.decsToTypes = decsToTypes;
        this.reducer = reducer;
        this.redundantInterfaceCleaner = redundantInterfaceCleaner;
    }

    private static final Set<String> blackListedNames = Arrays.asList("window").stream().map(String::toLowerCase).collect(Collectors.toSet());

    @Override
    public Multimap<DeclarationType, DeclarationType> findReplacements(CollectEveryTypeVisitor collected) {
        ArrayListMultimap<DeclarationType, DeclarationType> replacements = ArrayListMultimap.create();

        Map<Class<? extends DeclarationType>, Set<DeclarationType>> byType = collected.getEverythingByType();

        List<DeclarationType> objects = Util.concat(byType.get(InterfaceDeclarationType.class), byType.get(UnnamedObjectType.class));

        Map<String, DeclarationType> candidateMap = new HashMap<>();
        // Adding classes we have inferred.
        if (byType.get(ClassType.class) != null) {
            byType.get(ClassType.class).stream().map(clazz -> (ClassType)clazz).forEach(clazz -> candidateMap.put(clazz.getName().toLowerCase(), clazz.getEmptyNameInstance()));
        }
        // Adding natives
        nativeClasses.getNativeDeclarationTypes().forEach(dec -> candidateMap.put(dec.getName().toLowerCase(), dec));



        for (DeclarationType object : objects) {
            Set<String> names = object.getNames().stream().map(String::toLowerCase).collect(Collectors.toSet());
            List<String> commonNames = Util.intersection(names, candidateMap.keySet()).stream().filter(key -> !blackListedNames.contains(key)).collect(Collectors.toList());
            if (commonNames.isEmpty()) {
                continue;
            }
            handleManyNames(replacements, object, commonNames.stream().map(candidateMap::get).collect(Collectors.toList()));
        }


        return replacements;
    }

    private void handleManyNames(ArrayListMultimap<DeclarationType, DeclarationType> replacements, DeclarationType object, List<DeclarationType> classes) {
        List<Pair<Score, DeclarationType>> possibleReplacements = new ArrayList<>();
        for (DeclarationType clazz : classes) {
            Score score = redundantInterfaceCleaner.evaluteSimilarity(object, clazz, decsToTypes, nativeClasses).score(true);
            possibleReplacements.add(new Pair<>(score, clazz));
        }
        double maxPrecision = Collections.max(possibleReplacements, (a, b) -> Double.compare(b.left.precision, a.left.precision)).left.precision;
        possibleReplacements = possibleReplacements.stream().filter(pair -> pair.left.precision == maxPrecision).collect(Collectors.toList());

        boolean hasString = possibleReplacements.stream().map(Pair::getRight).anyMatch(clazz -> clazz instanceof NamedObjectType && ((NamedObjectType) clazz).getName().equals("String"));
        boolean hasArray = possibleReplacements.stream().map(Pair::getRight).anyMatch(clazz -> clazz instanceof NamedObjectType && ((NamedObjectType) clazz).getName().equals("Array"));

        Pair<Score, DeclarationType> bestPossible = possibleReplacements.get(0);
        Score score = bestPossible.left;
        if (score.fMeasure == 0) {
            return; // No way to tell which is the best.
        } else if (score.precision <= 0.5) {
            return;
        } else if (!hasObject(object) && hasDynAccess(object)) {
            return;
        } else if (score.precision == 1) {
            // We good
        } else if (hasString) {
            return;// Precision isn't 1. So there might be a "push" or something. Meaning I'm not sure.
        } else if (numberOfFields(object) >= 4 && score.precision >= 0.6){
            // We good
        } else if (hasArray && hasDynAccess(object) && score.precision > 0.6) {
            // We good
        } else {
            return; // TODO: See when it happens, and consider what should happen
        }

        List<DeclarationType> found = possibleReplacements.stream().filter(pair -> pair.left.precision == score.precision).map(pair -> pair.right).collect(Collectors.toList());
        DeclarationType result = new CombinationType(reducer, found).getCombined();

        if (!(result instanceof UnionDeclarationType)) {
            replacements.put(object, result);
        }

    }

    @Override
    public String getDescription() {
        return "name: [name] -> instanceof [name]";
    }
}
