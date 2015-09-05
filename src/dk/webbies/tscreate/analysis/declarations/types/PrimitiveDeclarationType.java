package dk.webbies.tscreate.analysis.declarations.types;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public enum PrimitiveDeclarationType implements DeclarationType {
    NUMBER("number"),
    BOOLEAN("boolean"),
    STRING("string"),
    VOID("void"),
    NULL("null"),
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

    public String getPrettyString() {
        return prettyString;
    }
}
