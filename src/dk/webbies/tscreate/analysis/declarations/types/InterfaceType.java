package dk.webbies.tscreate.analysis.declarations.types;

/**
 * Created by Erik Krogh Kristensen on 08-09-2015.
 */
public class InterfaceType extends DeclarationType {
    public FunctionType function = null;
    public UnnamedObjectType object = null;

    public DynamicAccessType dynamicAccess = null;

    public final String name;

    public static int counter = 0;

    public InterfaceType(String name) {
        // TODO: Ensure no conflicts
        if (name != null) {
            this.name = name;
        } else {
            this.name = "interface_" + counter++;
        }
    }

    public InterfaceType() {
        this(null);
    }

    public FunctionType getFunction() {
        if (function == null && object == null && dynamicAccess == null) {
            throw new NullPointerException("An interface must have either an object or function associated, or have some dynamic property access");
        }
        return function;
    }

    public UnnamedObjectType getObject() {
        if (function == null && object == null && dynamicAccess == null) {
            throw new NullPointerException("An interface must have either an object or function associated, or have some dynamic property access");
        }
        return object;
    }

    public DynamicAccessType getDynamicAccess() {
        if (function == null && object == null && dynamicAccess == null) {
            throw new NullPointerException("An interface must have either an object or function associated, or have some dynamic property access");
        }
        return dynamicAccess;
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
