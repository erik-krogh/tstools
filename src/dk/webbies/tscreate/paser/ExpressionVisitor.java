package dk.webbies.tscreate.paser;

import dk.webbies.tscreate.paser.AST.*;

/**
 * Created by Erik Krogh Kristensen on 07-09-2015.
 */
public interface ExpressionVisitor<T> {
    T visit(BinaryExpression binOp);

    T visit(BooleanLiteral bool);

    T visit(CallExpression callExpression);

    T visit(CommaExpression commaExpression);

    T visit(ConditionalExpression conditionalExpression);

    T visit(FunctionExpression functionExpression);

    T visit(Identifier identifier);

    T visit(MemberExpression memberExpression);

    T visit(DynamicAccessExpression memberLookupExpression);

    T visit(MethodCallExpression methodCallExpression);

    T visit(NewExpression newExpression);

    T visit(NullLiteral nullLiteral);

    T visit(NumberLiteral numberLiteral);

    T visit(ObjectLiteral objectLiteral);

    T visit(StringLiteral stringLiteral);

    T visit(ThisExpression thisExpression);

    T visit(UnaryExpression unaryExpression);

    T visit(UndefinedLiteral undefinedLiteral);

    T visit(GetterExpression getter);

    T visit(SetterExpression setter);

    T visit(ArrayLiteral arrayLiteral);

    T visit(RegExpExpression regExp);
}
