package dk.webbies.tscreate.analysis.declarations.types;

/**
 * Created by Erik Krogh Kristensen on 18-09-2015.
 */
public class ClassInstanceType implements DeclarationType {
    public DeclarationType clazz;

    public ClassInstanceType(DeclarationType clazz) {
        if (clazz == null) {
            throw new NullPointerException();
        }
        if (!(clazz instanceof UnresolvedDeclarationType) && !(clazz instanceof ClassType)) {
            throw new RuntimeException();
        }
        this.clazz = clazz;
    }

    // Casting because of UnresolvedDeclarationType
    public ClassType getClazz() {
        return (ClassType) clazz;
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
