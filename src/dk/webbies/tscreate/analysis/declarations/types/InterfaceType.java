package dk.webbies.tscreate.analysis.declarations.types;

/**
 * Created by Erik Krogh Kristensen on 08-09-2015.
 */
public class InterfaceType implements DeclarationType {
    private int useCounter = 0;
    public FunctionType function = null;
    public ObjectType object = null;

    public final String name;

    public InterfaceType(String name) {
        // TODO: Ensure no conflicts
        this.name = name;
    }

    public void incrementUseCounter() {
        this.useCounter++;
    }

    public int getUseCounter() {
        return useCounter;
    }

    public FunctionType getFunction() {
        if (function == null && object == null && false) { // TODO:
            throw new NullPointerException("An interface must have either an object or function associated");
        }
        return function;
    }

    public ObjectType getObject() {
        if (function == null && object == null && false) {
            throw new NullPointerException("An interface must have either an object or function associated");
        }
        return object;
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
