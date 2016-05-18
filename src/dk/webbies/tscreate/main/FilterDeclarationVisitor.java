package dk.webbies.tscreate.main;

import dk.au.cs.casa.typescript.types.InterfaceType;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.analysis.declarations.types.*;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by erik1 on 17-05-2016.
 */
public class FilterDeclarationVisitor implements DeclarationTypeVisitorWithArgument<DeclarationType,Type> {
    @Override
    public DeclarationType visit(FunctionType functionType, Type argument) {
        return functionType; // Not doing anything beneath a function.
    }

    @Override
    public DeclarationType visit(PrimitiveDeclarationType primitive, Type argument) {
        return primitive;
    }

    private DeclarationType recurse(Map.Entry<String, DeclarationType> entry, InterfaceType inter) {
        return entry.getValue().accept(this, inter.getDeclaredProperties().get(entry.getKey()));
    }

    @Override
    public DeclarationType visit(UnnamedObjectType objectType, Type type) {
        if (type instanceof InterfaceType) {
            InterfaceType inter = (InterfaceType) type;
            return new UnnamedObjectType(objectType.getDeclarations().entrySet().stream().filter(entry -> {
                return inter.getDeclaredProperties().keySet().contains(entry.getKey());
            }).collect(Collectors.toMap(Map.Entry::getKey, entry -> recurse(entry, inter))), objectType.getNames());
        }
        return objectType;
    }

    @Override
    public DeclarationType visit(InterfaceDeclarationType interfaceType, Type argument) {
        return interfaceType;
    }

    @Override
    public DeclarationType visit(UnionDeclarationType union, Type argument) {
        return union;
    }

    @Override
    public DeclarationType visit(NamedObjectType namedObjectType, Type argument) {
        return namedObjectType;
    }

    @Override
    public DeclarationType visit(ClassType classType, Type argument) {
        if (!(argument instanceof InterfaceType)) {
            return classType;
        }
        if (((InterfaceType) argument).getDeclaredConstructSignatures().isEmpty()) {
            return classType;
        }
        Map<String, DeclarationType> staticFields = ((UnnamedObjectType) new UnnamedObjectType(classType.getStaticFields(), Collections.EMPTY_SET).accept(this, argument)).getDeclarations();

        Type instanceType = ((InterfaceType) argument).getDeclaredConstructSignatures().iterator().next().getResolvedReturnType();

        Map<String, DeclarationType> protoFields = ((UnnamedObjectType) new UnnamedObjectType(classType.getPrototypeFields(), Collections.EMPTY_SET).accept(this, instanceType)).getDeclarations();

        return new ClassType(classType.getName(), classType.getConstructorType(), protoFields, staticFields, classType.getLibraryClass());
    }

    @Override
    public DeclarationType visit(ClassInstanceType instanceType, Type argument) {
        return instanceType;
    }
}
