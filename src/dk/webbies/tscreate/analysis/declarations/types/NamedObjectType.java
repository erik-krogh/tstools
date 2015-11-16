package dk.webbies.tscreate.analysis.declarations.types;

/**
 * Created by Erik Krogh Kristensen on 06-09-2015.
 */
public class NamedObjectType extends ObjectType {
    private String name;

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
}
