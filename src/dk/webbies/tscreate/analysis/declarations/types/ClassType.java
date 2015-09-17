package dk.webbies.tscreate.analysis.declarations.types;

/**
 * Created by Erik Krogh Kristensen on 17-09-2015.
 */
public class ClassType implements DeclarationType{
    public DeclarationType constructorType;
    public DeclarationType propertiesType;
    private String name;
    public DeclarationType superClass;

    public ClassType(DeclarationType constructorType, DeclarationType propertiesType, String name) {
        this.constructorType = constructorType;
        this.propertiesType = propertiesType;
        this.name = name;
    }

    public void setSuperClass(DeclarationType superClass) {
        this.superClass = superClass;
    }

    // Typecasting these two, because they are unresolvedType for a while.
    public FunctionType getConstructorType() {
        return (FunctionType) constructorType;
    }

    public ObjectType getPropertiesType() {
        return (ObjectType) propertiesType;
    }

    public DeclarationType getSuperClass() {
        return superClass;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
