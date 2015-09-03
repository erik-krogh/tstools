package dk.webbies.tscreate.declarationReader.tsTypeReader.types;

public interface TypeVisitorWithArgument<T, A> {
    T visit(AnonymousType t, A a);

    T visit(ClassType t, A a);

    T visit(GenericType t, A a);

    T visit(InterfaceType t, A a);

    T visit(ReferenceType t, A a);

    T visit(SimpleType t, A a);

    T visit(TupleType t, A a);

    T visit(UnionType t, A a);

    T visit(UnresolvedType t, A a);

    T visit(TypeParameterType t, A a);
}
