package dk.webbies.tscreate.paser.AST;

/**
 * Created by hamid on 10/21/15.
 */
public interface CFGExpressionVisitor<T> {
    T visit(BinaryExpression binOp, T aux);

    T visit(BooleanLiteral bool, T aux);

    T visit(CallExpression callExpression, T aux);

    T visit(CommaExpression commaExpression, T aux);

    T visit(ConditionalExpression conditionalExpression, T aux);

    T visit(FunctionExpression functionExpression, T aux);

    T visit(Identifier identifier, T aux);

    T visit(MemberExpression memberExpression, T aux);

    T visit(MemberLookupExpression memberLookupExpression, T aux);

    T visit(MethodCallExpression methodCallExpression, T aux);

    T visit(NewExpression newExpression, T aux);

    T visit(NullLiteral nullLiteral, T aux);

    T visit(NumberLiteral numberLiteral, T aux);

    T visit(ObjectLiteral objectLiteral, T aux);

    T visit(StringLiteral stringLiteral, T aux);

    T visit(ThisExpression thisExpression, T aux);

    T visit(UnaryExpression unaryExpression, T aux);

    T visit(UndefinedLiteral undefinedLiteral, T aux);

    T visit(PhiNodeExpression phiNode, T aux);
}
