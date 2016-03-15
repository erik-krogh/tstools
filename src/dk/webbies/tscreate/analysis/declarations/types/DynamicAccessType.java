package dk.webbies.tscreate.analysis.declarations.types;

import java.util.Set;

/**
 * Created by Erik Krogh Kristensen on 05-11-2015.
 */
public class DynamicAccessType extends DeclarationType {
    private DeclarationType lookupType;
    private DeclarationType returnType;

    public DynamicAccessType(DeclarationType lookupType, DeclarationType returnType, Set<String> names) {
        super(names);
        this.lookupType = lookupType;
        this.returnType = returnType;
    }

    public DeclarationType getLookupType() {
        return lookupType;
    }

    public DeclarationType getReturnType() {
        return returnType;
    }

    public void setLookupType(DeclarationType lookupType) {
        this.lookupType = lookupType;
    }

    public void setReturnType(DeclarationType returnType) {
        this.returnType = returnType;
    }

    public boolean isNumberIndexer() {
        DeclarationType resolvedLookup = this.lookupType.resolve();
        return resolvedLookup instanceof PrimitiveDeclarationType && ((PrimitiveDeclarationType) resolvedLookup).getType() == PrimitiveDeclarationType.Type.NUMBER;
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        // This is a type, that only exists within interfaceTypes, and it should be handled separatly when the interfaceType is handled.
        throw new UnsupportedOperationException();
    }

    @Override
    public <T, A> T accept(DeclarationTypeVisitorWithArgument<T, A> visitor, A argument) {
        // This is a type, that only exists within interfaceTypes, and it should be handled separatly when the interfaceType is handled.
        throw new UnsupportedOperationException();
    }
}
