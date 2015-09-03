package dk.webbies.tscreate.declarationReader.tsTypeReader.types;

public class AnonymousType implements Type {
    @Override
    public String toString() {
        return "Anonymous";
    }

    @Override
    public <T> T accept(TypeVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public <T, A> T accept(TypeVisitorWithArgument<T, A> v, A a) {
        return v.visit(this, a);
    }

}
