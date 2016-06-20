package dk.webbies.tscreate.evaluation.descriptions;

/**
 * Created by erik1 on 09-06-2016.
 */
public interface DescriptionVisitor<T> {
    T visit(Description.SimpleDescription description);

    T visit(PropertyMissingDescription description);

    T visit(ExcessPropertyDescription description);

    T visit(RightPropertyDescription description);

    T visit(WrongNumberOfArgumentsDescription description);

    T visit(WrongSimpleTypeDescription description);
}
