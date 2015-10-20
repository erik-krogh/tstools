package dk.webbies.tscreate.analysis.declarations.types.typeCombiner.singleTypeReducers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.webbies.tscreate.analysis.declarations.types.CombinationType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.UnnamedObjectType;
import dk.webbies.tscreate.analysis.declarations.types.typeCombiner.TypeReducer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 18-10-2015.
 */
public class UnnamedObjectReducer implements SingleTypeReducer<UnnamedObjectType, UnnamedObjectType> {
    private final TypeReducer combiner;

    public UnnamedObjectReducer(TypeReducer combiner) {
        this.combiner = combiner;
    }

    @Override
    public Class<UnnamedObjectType> getAClass() {
        return UnnamedObjectType.class;
    }

    @Override
    public Class<UnnamedObjectType> getBClass() {
        return UnnamedObjectType.class;
    }

    @Override
    public DeclarationType reduce(UnnamedObjectType one, UnnamedObjectType two) throws CantReduceException {

        Multimap<String, DeclarationType> declarations = ArrayListMultimap.create();
        for (Map.Entry<String, DeclarationType> entry : one.getDeclarations().entrySet()) {
            declarations.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, DeclarationType> entry : two.getDeclarations().entrySet()) {
            declarations.put(entry.getKey(), entry.getValue());
        }

        Map<String, DeclarationType> result = new HashMap<>();
        for (Map.Entry<String, Collection<DeclarationType>> entry : declarations.asMap().entrySet()) {
            result.put(entry.getKey(), new CombinationType(combiner, entry.getValue()));
        }

        return new UnnamedObjectType(result);
    }
}
