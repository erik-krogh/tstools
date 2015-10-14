package dk.webbies.tscreate.analysis.declarations.types;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public enum PrimitiveDeclarationType implements DeclarationType {
    NUMBER("number"),
    BOOLEAN("boolean"),
    STRING("string"),
    VOID("void"),
    UNDEFINED("void"),
    ANY("any");

    private String prettyString;

    PrimitiveDeclarationType(String prettyString) {
        this.prettyString = prettyString;
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <T, A> T accept(DeclarationTypeVisitorWithArgument<T, A> visitor, A argument) {
        return visitor.visit(this, argument);
    }


    public String getPrettyString() {
        return prettyString;
    }
}
