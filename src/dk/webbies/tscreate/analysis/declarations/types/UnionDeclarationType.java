package dk.webbies.tscreate.analysis.declarations.types;

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
        // This does 2 things: filters out nulls, and flattens 1-level nested UnionDeclarations.
        for (DeclarationType type : types) {
            if (type == null) {
                throw new RuntimeException();
            }
        }

        this.types = Arrays.asList(types).stream().filter(type -> type != null).reduce(new ArrayList<>(), (acc, dec) -> {
            if (dec instanceof UnionDeclarationType) {
                acc.addAll(((UnionDeclarationType)dec).types);
            } else {
                acc.add(dec);
            }
            return acc;
        }, (acc1, acc2) -> {acc1.addAll(acc2);return acc1;});
    }

    public UnionDeclarationType(Collection<? extends DeclarationType> types) {
        this.types = new ArrayList<>(types);
    }

    public ArrayList<DeclarationType> getTypes() {
        return types;
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
