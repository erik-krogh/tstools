package dk.webbies.tscreate.evaluation.descriptions;

import dk.au.cs.casa.typescript.types.SimpleType;

import static dk.webbies.tscreate.evaluation.descriptions.Description.SimpleDescriptionType.*;

/**
 * Created by erik1 on 09-06-2016.
 */
public interface Description {
    <T> T accept(DescriptionVisitor<T> visitor);

    enum DescriptionType {
        TRUE_POSITIVE,
        FALSE_NEGATIVE,
        FALSE_POSITIVE;
    }

    DescriptionType getType();

    enum SimpleDescriptionType {
        WRONG_NATIVE_TYPE, EXPECTED_INTERFACE, WAS_INTERFACE, SHOULD_NOT_BE_CONSTRUCTOR, SHOULD_NOT_BE_FUNCTION, SHOULD_BE_CONSTRUCTOR, SHOULD_BE_FUNCTION, WAS_FUNCTION, MISSING_INDEXER, EXCESS_INDEXER, WAS_INDEXER, RIGHT_SIMPLE_TYPE, WAS_TUPLE, WAS_NOT_TUPLE, WAS_SYMBOL, WAS_NOT_SYMBOL, RIGHT_NATIVE_TYPE
    }

    public static final class SimpleDescription implements Description{
        SimpleDescriptionType simpleType;
        DescriptionType type;

        public SimpleDescription(SimpleDescriptionType simpleType, DescriptionType type) {
            this.simpleType = simpleType;
            this.type = type;
        }

        public SimpleDescriptionType getSimpleType() {
            return simpleType;
        }

        @Override
        public <T> T accept(DescriptionVisitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public DescriptionType getType() {
            return type;
        }

        @Override
        public String toString() {
            return simpleType.toString();
        }
    }


    public static Description RightNativeType() {
        return new SimpleDescription(RIGHT_NATIVE_TYPE, DescriptionType.TRUE_POSITIVE);
    }

    static Description WrongNativeType() {
        return new SimpleDescription(WRONG_NATIVE_TYPE, DescriptionType.FALSE_NEGATIVE);
    }

    static Description ExpectedInterface() {
        return new SimpleDescription(EXPECTED_INTERFACE, DescriptionType.FALSE_NEGATIVE);
    }

    static Description WasInterface() {
        return new SimpleDescription(WAS_INTERFACE, DescriptionType.TRUE_POSITIVE);
    }

    static Description PropertyMissing(String property) {
        return new PropertyMissingDescription(property);
    }

    public static Description ExcessProperty(String property) {
        return new ExcessPropertyDescription(property);
    }

    static Description RightProperty(String property) {
        return new RightPropertyDescription(property);
    }

    static Description ShouldNotBeFunction(boolean isConstructor) {
        if (isConstructor) {
            return new SimpleDescription(SHOULD_NOT_BE_CONSTRUCTOR, DescriptionType.FALSE_POSITIVE);
        } else {
            return new SimpleDescription(SHOULD_NOT_BE_FUNCTION, DescriptionType.FALSE_POSITIVE);
        }
    }

    static Description ShouldBeFunction(boolean isConstructor) {
        if (isConstructor) {
            return new SimpleDescription(SHOULD_BE_CONSTRUCTOR, DescriptionType.FALSE_NEGATIVE);
        } else {
            return new SimpleDescription(SHOULD_BE_FUNCTION, DescriptionType.FALSE_NEGATIVE);
        }
    }

    static Description WasFunction() {
        return new SimpleDescription(WAS_FUNCTION, DescriptionType.TRUE_POSITIVE);
    }

    static Description WrongNumberOfArgs(int expected, int actual) {
        return new WrongNumberOfArgumentsDescription(expected, actual);
    }

    static Description MissingIndexer() {
        return new SimpleDescription(MISSING_INDEXER, DescriptionType.FALSE_NEGATIVE);
    }

    static Description ExcessIndexer() {
        return new SimpleDescription(EXCESS_INDEXER, DescriptionType.FALSE_POSITIVE);
    }

    static Description WasIndexer() {
        return new SimpleDescription(WAS_INDEXER, DescriptionType.TRUE_POSITIVE);
    }

    static Description RightSimpleType(SimpleType type) {
        return new SimpleDescription(RIGHT_SIMPLE_TYPE, DescriptionType.TRUE_POSITIVE);
    }

    static Description WrongSimpleType(SimpleType expected, dk.au.cs.casa.typescript.types.Type actual) {
        return new WrongSimpleTypeDescription(expected, actual);
    }

    static Description WasTuple() {
        return new SimpleDescription(WAS_TUPLE, DescriptionType.TRUE_POSITIVE);
    }

    static Description WasNotTuple() {
        return new SimpleDescription(WAS_NOT_TUPLE, DescriptionType.FALSE_NEGATIVE);
    }

    static Description WasSymbolType() {
        return new SimpleDescription(WAS_SYMBOL, DescriptionType.TRUE_POSITIVE);
    }

    static Description WasNotSymbolType() {
        return new SimpleDescription(WAS_NOT_SYMBOL, DescriptionType.FALSE_NEGATIVE);
    }
}
