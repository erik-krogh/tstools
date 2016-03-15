package dk.webbies.tscreate.analysis.declarations.types;

import java.util.Set;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class PrimitiveDeclarationType extends DeclarationType {
    public Type getType() {
        return type;
    }

    public static PrimitiveDeclarationType Void(Set<String> names) {
        return new PrimitiveDeclarationType(Type.VOID, names);
    }

    public static PrimitiveDeclarationType NonVoid(Set<String> names) {
        return new PrimitiveDeclarationType(Type.NON_VOID, names);
    }

    public static PrimitiveDeclarationType Any(Set<String> names) {
        return new PrimitiveDeclarationType(Type.ANY, names);
    }

    public static PrimitiveDeclarationType Number(Set<String> names) {
        return new PrimitiveDeclarationType(Type.NUMBER, names);
    }

    public static PrimitiveDeclarationType Boolean(Set<String> names) {
        return new PrimitiveDeclarationType(Type.BOOLEAN, names);
    }

    public static PrimitiveDeclarationType String(Set<String> names) {
        return new PrimitiveDeclarationType(Type.STRING, names);
    }

    public static PrimitiveDeclarationType StringOrNumber(Set<String> names) {
        return new PrimitiveDeclarationType(Type.STRING_OR_NUMBER, names);
    }

    public static PrimitiveDeclarationType fromType(Type type, Set<String> names) {
        return new PrimitiveDeclarationType(type, names);
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
    private PrimitiveDeclarationType(Type type, Set<String> names) {
        super(names);
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
}
