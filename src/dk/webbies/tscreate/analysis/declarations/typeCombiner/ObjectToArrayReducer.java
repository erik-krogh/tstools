package dk.webbies.tscreate.analysis.declarations.typeCombiner;

import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.util.Util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by erik1 on 12-02-2016.
 */
public class ObjectToArrayReducer implements SameTypeSingleInstanceReducer<UnnamedObjectType> {
    private final TypeReducer combiner;
    private final PrimitiveDeclarationType number = PrimitiveDeclarationType.Number(Collections.EMPTY_SET); // Cached, originals, and I don't need different ones.

    public ObjectToArrayReducer(TypeReducer combiner) {
        this.combiner = combiner;
    }

    @Override
    public DeclarationType reduce(UnnamedObjectType object) {
        if (object.getDeclarations().keySet().stream().map(Util::isInteger).filter(isInt -> isInt).count() >= 3) {
            List<DeclarationType> indexType = object.getDeclarations().entrySet().stream().filter(entry -> Util.isInteger(entry.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());

            UnnamedObjectType resultObject = new UnnamedObjectType(object.getDeclarations().entrySet().stream().filter(entry -> !Util.isInteger(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)), object.getNames());
            DeclarationType resultArray = new DynamicAccessType(number, new CombinationType(combiner, indexType), Collections.EMPTY_SET);
            combiner.originals.put(resultArray, Collections.singletonList(object));
            combiner.originals.put(resultObject, Collections.singletonList(object));
            return new UnionDeclarationType(resultObject, resultArray);
        }
        return null;
    }

    @Override
    public Class<UnnamedObjectType> getTheClass() {
        return UnnamedObjectType.class;
    }
}
