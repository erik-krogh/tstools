package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.SameTypeMultiReducer;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.SameTypeReducer;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 08-11-2015.
 */
public class NamedObjectReducer extends SameTypeMultiReducer<NamedObjectType> {
    private final Snap.Obj global;
    private final NativeClassesMap nativeClasses;
    private final TypeReducer combiner;

    public NamedObjectReducer(Snap.Obj global, NativeClassesMap nativeClasses, Map<DeclarationType, List<DeclarationType>> originals, TypeReducer combiner) {
        super(originals);
        this.global = global;
        this.nativeClasses = nativeClasses;
        this.combiner = combiner;
    }

    @Override
    public Class<NamedObjectType> getTheClass() {
        return NamedObjectType.class;
    }

    @Override
    public DeclarationType reduce(Collection<NamedObjectType> namedTypesCollection) {
        List<String> resultNames = new ArrayList<>();

        Set<NamedObjectType> namedTypes = new HashSet<>(namedTypesCollection);
        Set<NamedObjectType> nonBaseNamedTypes = namedTypes.stream().filter(type -> !type.isBaseType).collect(Collectors.toSet());
        if (nonBaseNamedTypes.isEmpty()) {
            nonBaseNamedTypes.addAll(namedTypes);
        }
        while (!nonBaseNamedTypes.isEmpty()) {
            Set<String> names = new HashSet<>();
            nonBaseNamedTypes.stream().map(NamedObjectType::getName).forEach(names::add);

            // First finding all the candidates, which are a sub-type of it all.
            List<Pair<String, Integer>> candidatesWithScore = nonBaseNamedTypes.stream().map(NamedObjectType::getName).map((candidate) -> {
                int count = (int) names.stream().filter(subName -> subName.equals(candidate) || nativeClasses.getBaseNames(subName).contains(candidate)).count();
                return new Pair<>(candidate, count);
            }).distinct().sorted((a, b) -> /* Reverse */- Integer.compare(a.second, b.second)).collect(Collectors.toList());

            if (candidatesWithScore.isEmpty()) {
                throw new RuntimeException();
            }
            int maxCount = candidatesWithScore.iterator().next().second;

            Set<String> candidates = candidatesWithScore.stream().filter(cand -> cand.second == maxCount).map(cand -> cand.first).collect(Collectors.toSet());

            for (String s : new ArrayList<>(candidates)) {
                if (candidates.size() <= 1) {
                    break;
                }
                candidates.removeAll(nativeClasses.getBaseNames(s));
            }

            if (candidates.isEmpty()) {
                throw new RuntimeException();
            }

            for (String resultName : candidates) {
                resultNames.add(resultName);

                Set<String> toRemove = nonBaseNamedTypes.stream().map(NamedObjectType::getName).filter(name -> nativeClasses.getBaseNames(name).contains(resultName)).collect(Collectors.toSet());
                nonBaseNamedTypes = nonBaseNamedTypes.stream().filter(named -> !toRemove.contains(named.getName()) && !resultName.equals(named.getName())).collect(Collectors.toSet());
            }
        }

        if (resultNames.isEmpty()) {
            throw new RuntimeException();
        }

        CombinationType indexType = null;
        if (namedTypes.stream().anyMatch(named -> named.indexType != null)) {
            indexType = new CombinationType(combiner, namedTypes.stream().map(named -> named.indexType).filter(Objects::nonNull).collect(Collectors.toList()));
        }

        Set<String> knownSubTypes = namedTypes.stream().map(NamedObjectType::getName).collect(Collectors.toSet());

        List<NamedObjectType> finalResults = new ArrayList<>();
        for (String name : resultNames) {
            NamedObjectType result = new NamedObjectType(name, false, indexType);
            knownSubTypes.remove(name);
            result.addKnownSubTypes(knownSubTypes);
            finalResults.add(result);
        }

        if (finalResults.size() == namedTypesCollection.size()) {
            // We achieved nothing, we therefore do not have a strictly smaller type, therefore return null.
            return null;
        }

        if (finalResults.size() == 1) {
            return finalResults.iterator().next();
        } else {
            return new UnionDeclarationType(finalResults);
        }
    }
}
