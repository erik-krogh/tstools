package dk.webbies.tscreate.analysis.declarations.types;

/**
 * Created by Erik Krogh Kristensen on 04-09-2015.
 */
public interface DeclarationTypeVisitor<T> {
    T visit(FunctionType functionType);

    T visit(PrimitiveDeclarationType primitive);

    T visit(UnnamedObjectType objectType);

    T visit(InterfaceDeclarationType interfaceType);

    T visit(UnionDeclarationType union);

    T visit(NamedObjectType namedObjectType);

    T visit(ClassType classType);

    T visit(ClassInstanceType instanceType);
}
