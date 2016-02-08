package dk.webbies.tscreate;

import dk.webbies.tscreate.paser.AST.*;

/**
 * Created by Erik Krogh Kristensen on 01-12-2015.
 */
public class FindEqualVisitor implements NodeTransverse<Void> {
    public int doubleEqCounter = 0;
    public int trippleEqCounter = 0;

    @Override
    public Void visit(BinaryExpression expression) {
        if (expression.getOperator() == Operator.EQUAL_EQUAL) {
            doubleEqCounter++;
        }
        if (expression.getOperator() == Operator.EQUAL_EQUAL_EQUAL) {
            trippleEqCounter++;
        }
        return NodeTransverse.super.visit(expression);
    }

}
