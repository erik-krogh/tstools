package dk.webbies.tscreate.analysis.declarations.types;

/**
 * Created by Erik Krogh Kristensen on 17-09-2015.
 */
public class UnresolvedDeclarationType implements DeclarationType {
    private DeclarationType resolvedType = null;

    private static int instanceCounter = 0;
    private final int counter;

    public UnresolvedDeclarationType() {
        this.counter = instanceCounter++;
    }

    public UnresolvedDeclarationType(DeclarationType resolvedType) {
        this();
        this.resolvedType = resolvedType;
    }

    public void setResolvedType(DeclarationType resolvedType) {
        if (this.resolvedType != null) {
            throw new RuntimeException();
        }
        if (resolvedType == this) {
            throw new RuntimeException();
        }
        this.resolvedType = resolvedType;
    }

    public boolean isResolved() {
        return this.resolvedType != null;
    }

    public DeclarationType getResolvedType() {
        if (this.resolvedType == null) {
            throw new RuntimeException();
        }
        while (resolvedType instanceof UnresolvedDeclarationType) {
            resolvedType = ((UnresolvedDeclarationType) resolvedType).getResolvedType();
        }
        return resolvedType;
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        return this.getResolvedType().accept(visitor);
    }

    @Override
    public <T, A> T accept(DeclarationTypeVisitorWithArgument<T, A> visitor, A argument) {
        return this.getResolvedType().accept(visitor, argument);
    }
}
