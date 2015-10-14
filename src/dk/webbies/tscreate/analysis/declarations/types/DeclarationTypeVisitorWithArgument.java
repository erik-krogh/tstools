package dk.webbies.tscreate.analysis.declarations.types;

/**
 * Created by Erik Krogh Kristensen on 04-09-2015.
 */
public interface DeclarationTypeVisitorWithArgument<T, A> {
    T visit(FunctionType functionType, A argument);

    T visit(PrimitiveDeclarationType primitive, A argument);

    T visit(UnnamedObjectType objectType, A argument);

    T visit(InterfaceType interfaceType, A argument);

    T visit(UnionDeclarationType union, A argument);

    T visit(NamedObjectType namedObjectType, A argument);

    T visit(ClassType classType, A argument);

    T visit(ClassInstanceType instanceType, A argument);

    T visit(ModuleType moduleType, A argument);
}
