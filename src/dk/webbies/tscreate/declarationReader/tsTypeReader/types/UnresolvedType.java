package dk.webbies.tscreate.declarationReader.tsTypeReader.types;

/**
 * Placeholder type which should be replaced with a real type later.
 * (deserialization does not see all types at once)
 */
public class UnresolvedType implements Type {
    private final int id;

    public UnresolvedType(int id) {
        this.id = id;
    }

    @Override
    public <T> T accept(TypeVisitor<T> v) {
        return v.visit(this);
    }

    public int getId() {
        return id;
    }

    @Override
    public <T, A> T accept(TypeVisitorWithArgument<T, A> v, A a) {
        return v.visit(this, a);
    }
}
