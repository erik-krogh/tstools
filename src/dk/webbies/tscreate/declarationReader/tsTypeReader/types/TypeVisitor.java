package dk.webbies.tscreate.declarationReader.tsTypeReader.types;

public interface TypeVisitor<T> {
    T visit(AnonymousType t);

    T visit(ClassType t);

    T visit(GenericType t);

    T visit(InterfaceType t);

    T visit(ReferenceType t);

    T visit(SimpleType t);

    T visit(TupleType t);

    T visit(UnionType t);

    T visit(UnresolvedType t);

    T visit(TypeParameterType t);
}
