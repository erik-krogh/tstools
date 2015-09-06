package dk.webbies.tscreate.paser;

/**
 * Created by Erik Krogh Kristensen on 01-09-2015.
 */
public interface NodeVisitor<T> {
    T visit(BinaryExpression expression);

    T visit(BlockStatement blockStatement);

    T visit(Return aReturn);

    T visit(MemberExpression memberExpression);

    T visit(StringLiteral string);

    T visit(Identifier identifier);

    T visit(BooleanLiteral booleanLitteral);

    T visit(UndefinedLiteral undefined);

    T visit(FunctionExpression function);

    T visit(NumberLiteral number);

    T visit(ExpressionStatement expressionStatement);

    T visit(NullLiteral nullLiteral);

    T visit(VariableNode variableNode);

    T visit(CallExpression callExpression);

    T visit(IfStatement ifStatement);

    T visit(ObjectLiteral object);
}
