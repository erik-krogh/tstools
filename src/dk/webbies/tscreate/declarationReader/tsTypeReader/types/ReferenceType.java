package dk.webbies.tscreate.declarationReader.tsTypeReader.types;

import java.util.List;

public class ReferenceType implements Type {

    private Type target;
    private List<Type> typeArguments;

    public Type getTarget() {
        return target;
    }

    public void setTarget(Type target) {
        this.target = target;
    }

    public List<Type> getTypeArguments() {
        return typeArguments;

    }

    public void setTypeArguments(List<Type> typeArguments) {
        this.typeArguments = typeArguments;
    }

    @Override
    public String toString() {
        return "Reference";
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
