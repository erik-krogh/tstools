package dk.webbies.tscreate.analysis.declarations.types;

import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 17-09-2015.
 */
public class ClassType implements DeclarationType{
    public DeclarationType constructorType;
    public Map<String, DeclarationType> propertiesType;
    private String name;
    public DeclarationType superClass;

    public ClassType(DeclarationType constructorType, Map<String, DeclarationType> properties, String name) {
        this.constructorType = constructorType;
        this.propertiesType = properties;
        this.name = name;
    }

    public void setSuperClass(DeclarationType superClass) {
        this.superClass = superClass;
    }

    // Typecasting these two, because they are unresolvedType for a while.
    public FunctionType getConstructorType() {
        return (FunctionType) constructorType;
    }

    public Map<String, DeclarationType> getProperties() {
        return propertiesType;
    }

    public ClassType getSuperClass() {
        return (ClassType) superClass;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
