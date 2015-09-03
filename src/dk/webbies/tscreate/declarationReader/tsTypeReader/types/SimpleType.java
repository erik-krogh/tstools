package dk.webbies.tscreate.declarationReader.tsTypeReader.types;

public class SimpleType implements Type {
    private final SimpleTypeKind kind;

    public SimpleType(SimpleTypeKind kind) {
        this.kind = kind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleType that = (SimpleType) o;

        return kind == that.kind;
    }

    public SimpleTypeKind getKind() {
        return kind;
    }

    @Override
    public <T, A> T accept(TypeVisitorWithArgument<T, A> v, A a) {
        return v.visit(this, a);
    }

    @Override
    public int hashCode() {
        return kind != null ? kind.hashCode() : 0;
    }

    @Override
    public String toString() {
        return kind.toString();
    }

    @Override
    public <T> T accept(TypeVisitor<T> v) {
        return v.visit(this);
    }

}

