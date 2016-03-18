package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.SingleTypeReducer;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.util.Util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 19-11-2015.
 */
public class DynamicAccessUnnamedObjectReducer extends SingleTypeReducer<DynamicAccessType, UnnamedObjectType> {

    private TypeReducer combiner;

    public DynamicAccessUnnamedObjectReducer(TypeReducer combiner, Map<DeclarationType, List<DeclarationType>> originals) {
        super(originals);
        this.combiner = combiner;
    }

    @Override
    public Class<DynamicAccessType> getAClass() {
        return DynamicAccessType.class;
    }

    @Override
    public Class<UnnamedObjectType> getBClass() {
        return UnnamedObjectType.class;
    }

    @Override
    public DeclarationType reduceIt(DynamicAccessType dynamicAccess, UnnamedObjectType obj) {
        if (obj.getDeclarations().keySet().stream().anyMatch(Util::isInteger)) {
            List<DeclarationType> indexTypes = obj.getDeclarations().entrySet().stream().filter(entry -> Util.isInteger(entry.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
            indexTypes.add(dynamicAccess.getReturnType());

            UnnamedObjectType resultUnnamed = new UnnamedObjectType(obj.getDeclarations().entrySet().stream().filter(entry -> !Util.isInteger(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)), obj.getNames());
            DynamicAccessType resultDynamic = new DynamicAccessType(dynamicAccess.getLookupType(), new CombinationType(combiner, indexTypes), dynamicAccess.getNames());

            return new UnionDeclarationType(resultUnnamed, resultDynamic);
        } else {
            return null;
        }
    }
}
