package dk.webbies.tscreate.evaluation.descriptions;

import dk.au.cs.casa.typescript.types.SimpleType;
import dk.au.cs.casa.typescript.types.Type;

/**
 * Created by erik1 on 09-06-2016.
 */
public class WrongSimpleTypeDescription implements Description {
    private final SimpleType expected;
    private final Type actual;

    public WrongSimpleTypeDescription(SimpleType expected, Type actual) {
        this.expected = expected;
        this.actual = actual;
    }

    public SimpleType getExpected() {
        return expected;
    }

    public Type getActual() {
        return actual;
    }

    @Override
    public <T> T accept(DescriptionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public DescriptionType getType() {
        return DescriptionType.FALSE_NEGATIVE;
    }

    @Override
    public String toString() {
        return "Wrong simple type, was supposed to be: " + expected.getKind().toString() + " was " + actual;
    }
}
