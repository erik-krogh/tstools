package dk.webbies.tscreate.paser.AST;

/**
 * Created by erik1 on 01-09-2015.
 */
public enum Operator {
    EQUAL,
    NOT_EQUAL,
    EQUAL_EQUAL,
    NOT_EQUAL_EQUAL,
    EQUAL_EQUAL_EQUAL,

    PLUS,
    MINUS,
    MULT,
    DIV,
    MOD,

    MINUS_EQUAL,
    MULT_EQUAL,
    DIV_EQUAL,
    MOD_EQUAL,
    PLUS_EQUAL,

    LESS_THAN,
    GREATER_THAN,
    LESS_THAN_EQUAL,
    GREATER_THAN_EQUAL,

    PLUS_PLUS,
    MINUS_MINUS,

    INSTANCEOF,
    DELETE,
    VOID,
    IN,

    NOT,
    TYPEOF,

    AND,
    OR,

    BITWISE_AND,
    BITWISE_OR,
    BITWISE_XOR,
    BITWISE_NOT,
    LEFT_SHIFT,
    RIGHT_SHIFT,
    UNSIGNED_RIGHT_SHIFT, LEFT_SHIFT_EQUAL, RIGHT_SHIFT_EQUAL, UNSIGNED_RIGHT_SHIFT_EQUAL,
}
