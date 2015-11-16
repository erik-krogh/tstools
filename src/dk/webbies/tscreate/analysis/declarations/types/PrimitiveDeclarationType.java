package dk.webbies.tscreate.analysis.declarations.types;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class PrimitiveDeclarationType extends DeclarationType {
    public Type getType() {
        return type;
    }

    public static PrimitiveDeclarationType NonVoid() {
        return new PrimitiveDeclarationType(Type.NON_VOID);
    }

    public static PrimitiveDeclarationType Any() {
        return new PrimitiveDeclarationType(Type.ANY);
    }

    public static PrimitiveDeclarationType Number() {
        return new PrimitiveDeclarationType(Type.NUMBER);
    }

    public static PrimitiveDeclarationType Boolean() {
        return new PrimitiveDeclarationType(Type.BOOLEAN);
    }

    public static PrimitiveDeclarationType String() {
        return new PrimitiveDeclarationType(Type.STRING);
    }

    public static PrimitiveDeclarationType StringOrNumber() {
        return new PrimitiveDeclarationType(Type.STRING_OR_NUMBER);
    }

    public static PrimitiveDeclarationType fromType(Type type) {
        return new PrimitiveDeclarationType(type);
    }

    public enum Type {
        NUMBER("number"),
        BOOLEAN("boolean"),
        STRING("string"),
        STRING_OR_NUMBER("string | number"),
        VOID("void"),
        ANY("any"),
        NON_VOID("any");

        private final String prettyString;

        Type(String prettyString) {
            this.prettyString = prettyString;
        }
    }

    private final Type type;
    private PrimitiveDeclarationType(Type type) {
        this.type = type;
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
        return this.type.prettyString;
    }

    public static PrimitiveDeclarationType Void() {
        return new PrimitiveDeclarationType(Type.VOID);
    }
}
