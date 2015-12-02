package dk.webbies.tscreate;

import dk.webbies.tscreate.paser.AST.*;

/**
 * Created by Erik Krogh Kristensen on 01-12-2015.
 */
public class FindSSAVisitor implements NodeTransverse<Void> {
    @Override
    public Void visit(FunctionExpression function) {
        for (Statement statement : function.getBody().getStatements()) {
            if (statement instanceof ExpressionStatement && ((ExpressionStatement) statement).getExpression() instanceof BinaryExpression) {
                BinaryExpression binop = (BinaryExpression) ((ExpressionStatement) statement).getExpression();
                checkBinop(binop, function);
            }
        }

        return NodeTransverse.super.visit(function);
    }

    private void checkBinop(BinaryExpression binop, FunctionExpression function) {
        if (binop.getOperator() == Operator.EQUAL) {
            if (binop.getLhs() instanceof Identifier) {
                Identifier id = (Identifier) binop.getLhs();
                if (function.getArguments().stream().anyMatch(arg -> arg.getName().equals(id.getName()))) {
                    System.out.println(id.getName() + " was redeclared at: " + (id.location.start.line + 1));
                }
            }
        }
    }

}
