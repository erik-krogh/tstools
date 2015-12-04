package dk.webbies.tscreate.paser;

import dk.webbies.tscreate.paser.AST.*;

/**
 * Created by hamid on 10/20/15.
 */
public interface CFGStatementVisitor<T> {
        T visit(BlockStatement block, T aux);

        T visit(BreakStatement breakStatement, T aux);

        T visit(ContinueStatement continueStatement, T aux);

        T visit(ExpressionStatement expressionStatement, T aux);

        T visit(ForStatement forStatement, T aux);

        T visit(IfStatement ifStatement, T aux);

        T visit(Return aReturn, T aux);

        T visit(SwitchStatement switchStatement, T aux);

        T visit(ThrowStatement throwStatement, T aux);

        T visit(VariableNode variableNode, T aux);

        T visit(WhileStatement whileStatement, T aux);

        T visit(ForInStatement forinStatement, T aux);

        T visit(TryStatement tryStatement, T aux);

        T visit(CatchStatement catchStatement, T aux);

        T visit(LabeledStatement labeledStatement, T aux);
}
