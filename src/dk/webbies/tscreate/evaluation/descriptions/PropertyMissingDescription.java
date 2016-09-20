package dk.webbies.tscreate.evaluation.descriptions;

/**
 * Created by erik1 on 09-06-2016.
 */
public class PropertyMissingDescription implements Description {
    private String property;

    public PropertyMissingDescription(String property) {
        this.property = property;
    }

    public String getPropertyName() {
        return property;
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
        return "Property missing: " + property;
    }
}
