package dk.webbies.tscreate.evaluation.descriptions;

/**
 * Created by erik1 on 09-06-2016.
 */
public class WrongNumberOfArgumentsDescription implements Description {
    private final int oldArgCount;
    private final int newArgCount;

    public WrongNumberOfArgumentsDescription(int oldArgCount, int newArgCount) {
        this.oldArgCount = oldArgCount;
        this.newArgCount = newArgCount;
    }

    @Override
    public <T> T accept(DescriptionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public DescriptionType getType() {
        return DescriptionType.FALSE_NEGATIVE;
    }

    public int getOldArgCount() {
        return oldArgCount;
    }

    public int getNewArgCount() {
        return newArgCount;
    }
}
