package dk.webbies.tscreate.analysis.declarations.types;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 06-09-2015.
 */
public class NamedObjectType extends ObjectType {
    private String name;
    private Set<String> knownSubTypes = new HashSet<>();

    public NamedObjectType(String name) {
        this.name = name;
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
