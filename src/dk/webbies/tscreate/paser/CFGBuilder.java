package dk.webbies.tscreate.paser;

import dk.webbies.tscreate.paser.AST.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hamid on 10/16/15.
 */
public class CFGBuilder {
    private   CFGStatementVisitor stmtVisitor = new CFGStmtVisitor();
    private CFGExpressionVisitor exprVisitor = new CFGExprVisitor();

    public Map<FunctionExpression, CFGNode> functionExpression2CFGNode = new HashMap<>();
    public Map<CFGNode, FunctionExpression> callNode2functionExpr = new HashMap<>(); // the node containing callsite --> FunctionExpression

    static int count = 0;
    private void printAstNode(AstNode stmt) {
        h.Helper.printDebug(String.valueOf(count), h.Helper.getText(stmt));
        h.Helper.printDebug("e" + count , ">>>>>>>>>>>>>>>>>>>>>");
        count++;
    }


    class CFGExprVisitor implements  CFGExpressionVisitor<CFGEnv> {
        @Override
        public CFGEnv visit(BinaryExpression binOp, CFGEnv aux) {
            printAstNode(binOp);
            return null;
        }

        @Override
        public CFGEnv visit(BooleanLiteral bool, CFGEnv aux) {
            printAstNode(bool);
            return null;
        }

        @Override
        public CFGEnv visit(CallExpression callExpression, CFGEnv aux) {
            printAstNode(callExpression);
            return null;
        }

        @Override
        public CFGEnv visit(CommaExpression commaExpression, CFGEnv aux) {
            printAstNode(commaExpression);
            return null;
        }

        @Override
        public CFGEnv visit(ConditionalExpression conditionalExpression, CFGEnv aux) {
            printAstNode(conditionalExpression);
            return null;
        }

        @Override
        public CFGEnv visit(FunctionExpression functionExpression, CFGEnv aux) {
            // aux == null: means this is the main function
            printAstNode(functionExpression);
            CFGEnv inCfgEnv = CFGEnv.createInCfgEnv();
            CFGNode entry = inCfgEnv.getAppendNode();
            functionExpression2CFGNode.put(functionExpression, entry);
            return  functionExpression.getBody().accept(stmtVisitor, inCfgEnv);
        }

        @Override
        public CFGEnv visit(Identifier identifier, CFGEnv aux) {
            printAstNode(identifier);
            return null;
        }

        @Override
        public CFGEnv visit(MemberExpression memberExpression, CFGEnv aux) {
            printAstNode(memberExpression);
            return null;
        }

        @Override
        public CFGEnv visit(MemberLookupExpression memberLookupExpression, CFGEnv aux) {
            printAstNode(memberLookupExpression);
            return null;
        }

        @Override
        public CFGEnv visit(MethodCallExpression methodCallExpression, CFGEnv aux) {
            printAstNode(methodCallExpression);
            return null;
        }

        @Override
        public CFGEnv visit(NewExpression newExpression, CFGEnv aux) {
            printAstNode(newExpression);
            return null;
        }

        @Override
        public CFGEnv visit(NullLiteral nullLiteral, CFGEnv aux) {
            printAstNode(nullLiteral);
            return null;
        }

        @Override
        public CFGEnv visit(NumberLiteral numberLiteral, CFGEnv aux) {
            printAstNode(numberLiteral);
            return null;
        }

        @Override
        public CFGEnv visit(ObjectLiteral objectLiteral, CFGEnv aux) {
            printAstNode(objectLiteral);
            return null;
        }

        @Override
        public CFGEnv visit(StringLiteral stringLiteral, CFGEnv aux) {
            printAstNode(stringLiteral);
            return null;
        }

        @Override
        public CFGEnv visit(ThisExpression thisExpression, CFGEnv aux) {
            printAstNode(thisExpression);
            return null;
        }

        @Override
        public CFGEnv visit(UnaryExpression unaryExpression, CFGEnv aux) {
            printAstNode(unaryExpression);
            return null;
        }

        @Override
        public CFGEnv visit(UndefinedLiteral undefinedLiteral, CFGEnv aux) {
            printAstNode(undefinedLiteral);
            return null;
        }

        @Override
        public CFGEnv visit(PhiNodeExpression phiNode, CFGEnv aux) {
            printAstNode(phiNode);
            return null;
        }
    }

    class CFGStmtVisitor implements CFGStatementVisitor<CFGEnv> {
         @Override
        public CFGEnv visit(BlockStatement block, CFGEnv aux) {
            printAstNode(block);
            for (Statement stmt : block.getStatements()) {
                stmt.accept(stmtVisitor, null);
            }
            return null;
        }

        @Override
        public CFGEnv visit(BreakStatement breakStatement, CFGEnv aux) {
            printAstNode(breakStatement);
            return null;
        }

        @Override
        public CFGEnv visit(ContinueStatement continueStatement, CFGEnv aux) {
            printAstNode(continueStatement);
            return null;
        }

        @Override
        public CFGEnv visit(ExpressionStatement expressionStatement, CFGEnv aux) {
            printAstNode(expressionStatement);

            Expression expr = expressionStatement.getExpression();
            expr.accept(exprVisitor,null);
            return null;
        }

        @Override
        public CFGEnv visit(ForStatement forStatement, CFGEnv aux) {
            printAstNode(forStatement);
            return null;
        }

        @Override
        public CFGEnv visit(IfStatement ifStatement, CFGEnv aux) {
            printAstNode(ifStatement);
            ifStatement.getCondition().accept(exprVisitor, null);
            ifStatement.getIfBranch().accept(stmtVisitor, null);
            ifStatement.getElseBranch().accept(stmtVisitor,null);
            return null;
        }

        @Override
        public CFGEnv visit(Return aReturn, CFGEnv aux) {
            printAstNode(aReturn);
            aReturn.getExpression().accept(exprVisitor, null);
            return null;
        }

        @Override
        public CFGEnv visit(SwitchStatement switchStatement, CFGEnv aux) {
            printAstNode(switchStatement);
            switchStatement.getExpression().accept(exprVisitor, null);
            //for (Map)
            return null;
        }

        @Override
        public CFGEnv visit(ThrowStatement throwStatement, CFGEnv aux) {
            printAstNode(throwStatement);
            return null;
        }

        @Override
        public CFGEnv visit(VariableNode variableNode, CFGEnv aux) {
            printAstNode(variableNode);
            variableNode.getlValue().accept(exprVisitor, null);
            variableNode.getInit().accept(exprVisitor, null);
            return null;
        }

        @Override
        public CFGEnv visit(WhileStatement whileStatement, CFGEnv aux) {
            printAstNode(whileStatement);

            return null;
        }

        @Override
        public CFGEnv visit(ForInStatement forinStatement, CFGEnv aux) {
            printAstNode(forinStatement);

            return null;
        }

        @Override
        public CFGEnv visit(TryStatement tryStatement, CFGEnv aux) {
            printAstNode(tryStatement);

            return null;
        }

        @Override
        public CFGEnv visit(CatchStatement catchStatement, CFGEnv aux) {
            printAstNode(catchStatement);
            return null;
        }
    }

    public void processMain(FunctionExpression mainFunction) {
        //processFunction(mainFunction);
        exprVisitor.visit(mainFunction, null);
    }

}
