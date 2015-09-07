package dk.webbies.tscreate.paser;

import dk.webbies.tscreate.paser.AST.*;

/**
 * Created by Erik Krogh Kristensen on 07-09-2015.
 *
 * Transverses all the Expressions and sub-Expressions (except the function, since it contains Statements).
 */
public interface ExpressionTransverse<T> extends ExpressionVisitor<T> {
    @Override
    public default T visit(BinaryExpression expression) {
        expression.getLhs().accept(this);
        expression.getRhs().accept(this);
        return null;
    }

    @Override
    public default T visit(UnaryExpression unOp) {
        unOp.getExpression().accept(this);
        return null;
    }


    @Override
    public default T visit(MemberExpression memberExpression) {
        memberExpression.getExpression().accept(this);
        return null;
    }

    @Override
    public default T visit(StringLiteral string) {
        return null;
    }

    @Override
    public default T visit(Identifier identifier) {
        return null;
    }

    @Override
    public default T visit(BooleanLiteral booleanLiteral) {
        return null;
    }

    @Override
    public default T visit(UndefinedLiteral undefined) {
        return null;
    }

    @Override
    public default T visit(NumberLiteral number) {
        return null;
    }


    @Override
    public default T visit(NullLiteral nullLiteral) {
        return null;
    }

    @Override
    public default T visit(CallExpression callExpression) {
        callExpression.getFunction().accept(this);
        callExpression.getArgs().stream().forEach(arg -> arg.accept(this));
        return null;
    }

    @Override
    public default T visit(ObjectLiteral object) {
        object.getProperties().values().forEach(value -> value.accept(this));
        return null;
    }

    @Override
    public default T visit(MethodCallExpression methodCall) {
        methodCall.getMemberExpression().accept(this);
        methodCall.getArgs().forEach(arg -> arg.accept(this));
        return null;
    }

    @Override
    public default T visit(ThisExpression thisExpression) {
        return null;
    }

    @Override
    public default T visit(NewExpression newExp) {
        newExp.getOperand().accept(this);
        newExp.getArguments().forEach(arg -> arg.accept(this));
        return null;
    }

    @Override
    public default T visit(ConditionalExpression cond) {
        cond.getCondition().accept(this);
        cond.getLeft().accept(this);
        cond.getRight().accept(this);
        return null;
    }

    @Override
    public default T visit(MemberLookupExpression memberLookup) {
        memberLookup.getOperand().accept(this);
        memberLookup.getLookupKey().accept(this);
        return null;
    }

    @Override
    public default T visit(CommaExpression commaExpression) {
        commaExpression.getExpressions().forEach(exp -> exp.accept(this));
        return null;
    }

    @Override
    public default T visit(ArrayLiteral array) {
        array.getExpressions().forEach(exp -> exp.accept(this));
        return null;
    }
}
