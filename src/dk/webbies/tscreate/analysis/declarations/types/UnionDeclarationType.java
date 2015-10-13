package dk.webbies.tscreate.analysis.declarations.types;

import dk.webbies.tscreate.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Erik Krogh Kristensen on 09-09-2015.
 */
public class UnionDeclarationType implements DeclarationType {
    private final ArrayList<DeclarationType> types;

    public UnionDeclarationType(DeclarationType ...types) {
        if (types.length == 0) {
            throw new IllegalArgumentException();
        }
        // This does 2 things: filters out nulls, and flattens 1-level nested UnionDeclarations.
        for (DeclarationType type : types) {
            if (type == null) {
                throw new RuntimeException();
            }
        }

        this.types = Arrays.asList(types).stream().filter(type -> type != null).reduce(new ArrayList<>(), (acc, dec) -> {
            if (dec instanceof UnionDeclarationType) {
                acc.addAll(((UnionDeclarationType) dec).types);
            } else {
                acc.add(dec);
            }
            return acc;
        }, Util::reduceList);
    }

    public UnionDeclarationType(Collection<? extends DeclarationType> types) {
        this(types.toArray(new DeclarationType[types.size()]));
    }

    public ArrayList<DeclarationType> getTypes() {
        return types;
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
