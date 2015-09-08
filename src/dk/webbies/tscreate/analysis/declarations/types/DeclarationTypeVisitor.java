package dk.webbies.tscreate.analysis.declarations.types;

/**
 * Created by Erik Krogh Kristensen on 04-09-2015.
 */
public interface DeclarationTypeVisitor<T> {
    T visit(FunctionType functionType);

    T visit(PrimitiveDeclarationType primitive);

    T visit(ObjectType objectType);

    T visit(InterfaceType interfaceType);
}
