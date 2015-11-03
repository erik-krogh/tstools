package dk.webbies.tscreate.analysis.declarations.types;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public interface DeclarationType {
    <T> T accept(DeclarationTypeVisitor<T> visitor);

    <T, A> T accept(DeclarationTypeVisitorWithArgument<T, A> visitor, A argument);

    default DeclarationType resolve() {
        return DeclarationType.resolve(this);
    }

    public static DeclarationType resolve(DeclarationType type) {
        if (type instanceof UnresolvedDeclarationType) {
            return DeclarationType.resolve(((UnresolvedDeclarationType) type).getResolvedType());
        } else if (type instanceof CombinationType) {
            return ((CombinationType) type).getCombined();
        } else {
            return type;
        }
    }
}
