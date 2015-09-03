package dk.webbies.tscreate.declarationReader.tsTypeReader.types;

import java.util.List;

public class TupleType implements Type {
    private List<Type> elementTypes;
    private Type baseArrayType;

    @Override
    public <T> T accept(TypeVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public String toString() {
        return "Tuple(" +
                elementTypes +
                ')';
    }

    @Override
    public <T, A> T accept(TypeVisitorWithArgument<T, A> v, A a) {
        return v.visit(this, a);
    }

    public List<Type> getElementTypes() {
        return elementTypes;
    }

    public void setElementTypes(List<Type> elementTypes) {
        this.elementTypes = elementTypes;
    }

    public Type getBaseArrayType() {
        return baseArrayType;
    }

    public void setBaseArrayType(Type baseArrayType) {
        this.baseArrayType = baseArrayType;
    }
}
