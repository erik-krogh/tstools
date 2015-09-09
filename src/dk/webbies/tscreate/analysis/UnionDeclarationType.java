package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationTypeVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by Erik Krogh Kristensen on 09-09-2015.
 */
public class UnionDeclarationType implements DeclarationType {
    private final ArrayList<DeclarationType> types;

    public UnionDeclarationType(DeclarationType ...types) {
        this.types = new ArrayList<>(Arrays.asList(types));
    }

    public UnionDeclarationType(Set<? extends DeclarationType> types) {
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
