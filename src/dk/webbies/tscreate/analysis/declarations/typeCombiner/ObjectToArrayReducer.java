package dk.webbies.tscreate.analysis.declarations.typeCombiner;

import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.util.Util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by erik1 on 12-02-2016.
 */
public class ObjectToArrayReducer implements SameTypeSingleInstanceReducer<UnnamedObjectType> {
    private final TypeReducer combiner;

    public ObjectToArrayReducer(TypeReducer combiner) {
        this.combiner = combiner;
    }

    @Override
    public DeclarationType reduce(UnnamedObjectType object) {
        if (object.getDeclarations().keySet().stream().map(Util::isInteger).filter(isInt -> isInt).count() >= 3) {
            List<DeclarationType> indexType = object.getDeclarations().entrySet().stream().filter(entry -> Util.isInteger(entry.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());

            UnnamedObjectType resultObject = new UnnamedObjectType(object.getDeclarations().entrySet().stream().filter(entry -> !Util.isInteger(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            DeclarationType resultArray = new DynamicAccessType(PrimitiveDeclarationType.Number(), new CombinationType(combiner, indexType));
            return new UnionDeclarationType(resultObject, resultArray);
        }
        return null;
    }

    @Override
    public Class<UnnamedObjectType> getTheClass() {
        return UnnamedObjectType.class;
    }
}
