package dk.webbies.tscreate.analysis.declarations.types;

import dk.webbies.tscreate.util.Pair;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 06-09-2015.
 */
public class NamedObjectType extends ObjectType {
    private final String name;
    private final Set<String> knownSubTypes = new HashSet<>();
    public final boolean isBaseType;

    private DeclarationType indexType; // For generic arrays.

    public NamedObjectType(String name, boolean isBaseType) {
        this(name, isBaseType, null);
    }

    public NamedObjectType(String name, boolean isBaseType, DeclarationType indexType) {
        this(new Pair<>(name, isBaseType), indexType);
    }

    public NamedObjectType(Pair<String, Boolean> pair) {
        this(pair, null);
    }

    public NamedObjectType(Pair<String, Boolean> pair, DeclarationType indexType) {
        super(Collections.EMPTY_SET);
        this.name = pair.left;
        this.indexType = indexType;
        this.isBaseType = pair.right;
    }

    public DeclarationType getIndexType() {
        return indexType;
    }

    public void setIndexType(DeclarationType indexType) {
        this.indexType = indexType;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <T, A> T accept(DeclarationTypeVisitorWithArgument<T, A> visitor, A argument) {
        return visitor.visit(this, argument);
    }

    public Set<String> getKnownSubTypes() {
        return this.knownSubTypes;
    }

    public void addKnownSubTypes(Collection<String> knownSubTypes) {
        this.knownSubTypes.addAll(knownSubTypes);
    }

    public void addKnownSubType(String knownSubTypes) {
        this.knownSubTypes.add(knownSubTypes);
    }
}


