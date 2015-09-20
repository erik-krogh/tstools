package dk.webbies.tscreate.analysis.declarations.types;

import dk.webbies.tscreate.jsnap.classes.LibraryClass;

/**
 * Created by Erik Krogh Kristensen on 18-09-2015.
 */
public class ClassInstanceType implements DeclarationType {
    public DeclarationType clazz;

    public ClassInstanceType(DeclarationType clazz) {
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
