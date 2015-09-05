package dk.webbies.tscreate.paser;

/**
 * A default implementation of NodeVisitor, that visits everything, but returns null for everything.
 */
public abstract class NodeTransverse implements NodeVisitor<Void> {
    @Override
    public Void visit(BinaryExpression expression) {
        expression.getLhs().accept(this);
        expression.getRhs().accept(this);
        return null;
    }

    @Override
    public Void visit(BlockStatement blockStatement) {
        blockStatement.getStatements().forEach(statement -> statement.accept(this));
        return null;
    }

    @Override
    public Void visit(Return aReturn) {
        aReturn.getExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(MemberExpression memberExpression) {
        memberExpression.getExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(StringLiteral string) {
        return null;
    }

    @Override
    public Void visit(Identifier identifier) {
        return null;
    }

    @Override
    public Void visit(BooleanLiteral booleanLiteral) {
        return null;
    }

    @Override
    public Void visit(UndefinedLiteral undefined) {
        return null;
    }

    @Override
    public Void visit(FunctionExpression function) {
        function.getBody().accept(this);
        if (function.getName() != null) {
            function.getName().accept(this);
        }
        function.getArguments().forEach(arg -> arg.accept(this));
        return null;
    }

    @Override
    public Void visit(NumberLiteral number) {
        return null;
    }

    @Override
    public Void visit(ExpressionStatement expressionStatement) {
        expressionStatement.getExpression().accept(this);
        return null;
    }

    @Override
    public Void visit(NullLiteral nullLiteral) {
        return null;
    }

    @Override
    public Void visit(VariableNode variableNode) {
        variableNode.getlValue().accept(this);
        variableNode.getInit().accept(this);
        return null;
    }

    @Override
    public Void visit(CallExpression callExpression) {
        callExpression.getFunction().accept(this);
        callExpression.getArgs().stream().forEach(arg -> arg.accept(this));
        return null;
    }

    @Override
    public Void visit(IfStatement ifStatement) {
        ifStatement.getCondition().accept(this);
        ifStatement.getIfBranch().accept(this);
        ifStatement.getElseBranch().accept(this);
        return null;
    }
}
