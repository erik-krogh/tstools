package dk.webbies.tscreate.evaluation.descriptions;

/**
 * Created by erik1 on 09-06-2016.
 */
public class RightPropertyDescription implements Description {
    private String property;

    public RightPropertyDescription(String property) {
        this.property = property;
    }

    @Override
    public <T> T accept(DescriptionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public DescriptionType getType() {
        return DescriptionType.TRUE_POSITIVE;
    }

    @Override
    public String toString() {
        return "Correct property: " + property;
    }
}
