package dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.SameTypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.CombinationType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.UnnamedObjectType;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 18-10-2015.
 */
public class UnnamedObjectReducer extends SameTypeReducer<UnnamedObjectType> {
    private final TypeReducer combiner;

    public UnnamedObjectReducer(TypeReducer combiner, Map<DeclarationType, List<DeclarationType>> originals) {
        super(originals);
        this.combiner = combiner;
    }

    @Override
    public Class<UnnamedObjectType> getTheClass() {
        return UnnamedObjectType.class;
    }

    @Override
    public UnnamedObjectType reduceIt(UnnamedObjectType one, UnnamedObjectType two) {
        Multimap<String, DeclarationType> declarations = ArrayListMultimap.create();
        for (Map.Entry<String, DeclarationType> entry : one.getDeclarations().entrySet()) {
            declarations.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, DeclarationType> entry : two.getDeclarations().entrySet()) {
            declarations.put(entry.getKey(), entry.getValue());
        }

        Map<String, DeclarationType> result = new HashMap<>();
        for (Map.Entry<String, Collection<DeclarationType>> entry : declarations.asMap().entrySet()) {
            Collection<DeclarationType> types = entry.getValue();
            if (types.size() == 0) {
                throw new RuntimeException();
            } else if (types.size() == 1) {
                result.put(entry.getKey(), types.iterator().next());
            } else {
                result.put(entry.getKey(), new CombinationType(combiner, types.toArray(new DeclarationType[types.size()])));
            }
        }

        return new UnnamedObjectType(result);
    }
}
