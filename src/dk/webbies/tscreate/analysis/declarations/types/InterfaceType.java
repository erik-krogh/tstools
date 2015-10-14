package dk.webbies.tscreate.analysis.declarations.types;

/**
 * Created by Erik Krogh Kristensen on 08-09-2015.
 */
public class InterfaceType implements DeclarationType {
    public DeclarationType function = null;
    public DeclarationType object = null;

    public final String name;

    public InterfaceType(String name) {
        // TODO: Ensure no conflicts
        this.name = name;
    }

    // TODO: Unresolved.
    public FunctionType getFunction() {
        if (function == null && object == null) {
            throw new NullPointerException("An interface must have either an object or function associated");
        }
        return (FunctionType) function;
    }

    public UnnamedObjectType getObject() {
        if (function == null && object == null) {
            throw new NullPointerException("An interface must have either an object or function associated");
        }
        return (UnnamedObjectType) object; // TODO: Sometimes an namedObjectType.
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <T, A> T accept(DeclarationTypeVisitorWithArgument<T, A> visitor, A argument) {
        return visitor.visit(this, argument);
    }
}
