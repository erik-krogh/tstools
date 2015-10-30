package dk.webbies.tscreate.paser;

import dk.webbies.tscreate.paser.AST.*;

import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 07-09-2015.
 */
public interface StatementTransverse<T> extends StatementVisitor<T>  {
    ExpressionVisitor<T> getExpressionVisitor();

    @Override
    public default T visit(BlockStatement blockStatement) {
        blockStatement.getStatements().forEach(statement -> statement.accept(this));
        return null;
    }

    @Override
    public default T visit(Return aReturn) {
        aReturn.getExpression().accept(getExpressionVisitor());
        return null;
    }

    @Override
    public default T visit(ExpressionStatement expressionStatement) {
        expressionStatement.getExpression().accept(getExpressionVisitor());
        return null;
    }

    @Override
    public default T visit(VariableNode variableNode) {
        variableNode.getlValue().accept(getExpressionVisitor());
        variableNode.getInit().accept(getExpressionVisitor());
        return null;
    }

    @Override
    public default T visit(IfStatement ifStatement) {
        ifStatement.getCondition().accept(getExpressionVisitor());
        ifStatement.getIfBranch().accept(this);
        ifStatement.getElseBranch().accept(this);
        return null;
    }

    @Override
    public default T visit(SwitchStatement switchStatement) {
        switchStatement.getExpression().accept(getExpressionVisitor());
        for (Map.Entry<Expression, Statement> entry : switchStatement.getCases()) {
            entry.getKey().accept(getExpressionVisitor());
            entry.getValue().accept(this);
        }
        switchStatement.getDefaultCase().getStatements().forEach(statement -> statement.accept(this));
        return null;
    }

    @Override
    public default T visit(ForStatement forStatement) {
        forStatement.getInitialize().accept(this);
        forStatement.getCondition().accept(getExpressionVisitor());
        forStatement.getIncrement().accept(getExpressionVisitor());
        forStatement.getBody().accept(this);
        return null;
    }


    @Override
    public default T visit(WhileStatement whileStatement) {
        whileStatement.getCondition().accept(getExpressionVisitor());
        whileStatement.getBody().accept(this);
        return null;
    }

    @Override
    public default T visit(ContinueStatement continueStatement) {
        return null;
    }

    @Override
    public default T visit(BreakStatement breakStatement) {
        return null;
    }

    @Override
    public default T visit(ThrowStatement throwStatement) {
        throwStatement.getExpression().accept(getExpressionVisitor());
        return null;
    }

    @Override
    public default T visit(ForInStatement forIn) {
        forIn.getInitializer().accept(this);
        forIn.getCollection().accept(getExpressionVisitor());
        forIn.getBody().accept(this);
        return null;
    }

    @Override
    public default T visit(TryStatement tryStatement) {
        tryStatement.getTryBlock().accept(this);
        if (tryStatement.getCatchBlock() != null) {
            tryStatement.getCatchBlock().accept(this);
        }
        if (tryStatement.getFinallyBlock() != null) {
            tryStatement.getFinallyBlock().accept(this);
        }
        return null;
    }

    @Override
    public default T visit(CatchStatement catchStatement) {
        catchStatement.getException().accept(getExpressionVisitor());
        catchStatement.getBody().accept(this);
        return null;
    }

    @Override
    public default T visit(LabeledStatement labeledStatement) {
        labeledStatement.getStatement().accept(this);
        return null;
    }
}
