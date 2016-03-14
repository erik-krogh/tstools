package dk.webbies.tscreate.analysis.declarations.types;

import dk.webbies.tscreate.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Erik Krogh Kristensen on 09-09-2015.
 */
public class UnionDeclarationType extends DeclarationType {
    private List<DeclarationType> types;

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
            unfoldDeclaration(acc, dec);
            return acc;
        }, Util::reduceList);
    }

    private void unfoldDeclaration(ArrayList<DeclarationType> acc, DeclarationType type) {
        if (type instanceof UnionDeclarationType) {
            List<DeclarationType> types = ((UnionDeclarationType) type).types;
            for (DeclarationType subType : types) {
                unfoldDeclaration(acc, subType);
            }

        } else {
            acc.add(type);
        }
    }

    public UnionDeclarationType(Collection<? extends DeclarationType> types) {
        this(types.toArray(new DeclarationType[types.size()]));
    }

    public List<DeclarationType> getTypes() {
        return types;
    }

    public void setTypes(List<DeclarationType> types) {
        this.types = types;
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <T, A> T accept(DeclarationTypeVisitorWithArgument<T, A> visitor, A argument) {
        return visitor.visit(this, argument);
    }
}
