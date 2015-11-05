package dk.webbies.tscreate.paser;

import dk.webbies.tscreate.paser.AST.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hamid on 10/16/15.
 */
public class CFGBuilder {
    private CFGStatementVisitor stmtVisitor = new CFGStmtVisitor();
    private CFGExpressionVisitor exprVisitor = new CFGExprVisitor();

    public Map<FunctionExpression, CFGNode> functionExpression2CFGNode = new HashMap<>();
    public Map<CFGNode, FunctionExpression> callNode2functionExpr = new HashMap<>(); // the node containing callsite --> FunctionExpression

    //public CFGEnv DUMMY_ENV;

    static int count = 0;
    private void printAstNode(AstNode stmt) {
        h.Helper.printDebug(String.valueOf(count), h.Helper.getText(stmt));
        h.Helper.printDebug("e" + count , ">>>>>>>>>>>>>>>>>>>>>");
        count++;
    }


    private CFGUse useNodeUnderconstruction = null;
    private final void initNewUseNode(AstNode astNode) {
        assert (useNodeUnderconstruction == null);
        useNodeUnderconstruction = new CFGUse(astNode);
    }

    // The following two methods should be used to create CFGDef nodes during AST traversal.
    // The first one  (creates if necessary and) returns the current CFGUse node
    // Second method gets you the current CFGUSE nod and un-init it
    private final CFGUse makeUseNode(AstNode astNode) {
        if (useNodeUnderconstruction == null) initNewUseNode(astNode);
        return useNodeUnderconstruction;
    }
    private final CFGUse getCurrentAndResetUseNode() {
        assert (useNodeUnderconstruction != null);
        CFGUse ret = useNodeUnderconstruction;
        useNodeUnderconstruction = null;
        return ret;
    }
    class CFGExprVisitor implements  CFGExpressionVisitor<CFGEnv> {
        @Override
        public CFGEnv visit(BinaryExpression binOp, CFGEnv au) {
            if (au == null) {
                //throw new RuntimeException();
                au = DUMMY_ENV;
            }
            printAstNode(binOp);
            switch (binOp.getOperator()) {
                case EQUAL:
                {
                    // preparing for creating defnode: def(lhs)
                    Expression var = binOp.getLhs();
                    if (!(var instanceof Identifier)) throw new RuntimeException();
                    Identifier id = (Identifier) var;
                    // process rhs first
                    CFGEnv aux = binOp.getRhs().accept(exprVisitor, au);
                    if (aux == null) {
                        //throw new RuntimeException();
                        aux = DUMMY_ENV;
                    }
                    CFGNode defNode = new CFGDef(binOp, id);
                    CFGEnv outEnv = CFGEnv.createOutCfgEnv(aux.getAppendNode(), defNode);
                    var.accept(exprVisitor, null); // debug purposes only

                    return outEnv;
                }
                case PLUS:
                case MINUS:
                case MULT:
                {

                    // creates a CFGUseNode if necessary (otherwise use the one under construction)
                    boolean isTopUseNode = (useNodeUnderconstruction == null);
                    {if (isTopUseNode) h.Helper.printDebug("TOPNODE");
                    printAstNode(binOp);}
                    CFGUse currUseNode = makeUseNode(binOp);

                    Expression lhs = binOp.getLhs();
                    Expression rhs = binOp.getRhs();
                    lhs.accept(exprVisitor, null);
                    rhs.accept(exprVisitor, null);
                    ;
                    if (isTopUseNode) {
                        CFGUse useNode = getCurrentAndResetUseNode();
                        assert (useNode == currUseNode);
                        CFGEnv outEnv = CFGEnv.createOutCfgEnv(au.getAppendNode(), useNode);
                        assert (useNodeUnderconstruction == null);
                        return outEnv;
                    }
                    return null;
                }
                default:
                    return null;
            }

            //binOp.getLhs().accept(exprVisitor, null);
            //binOp.getRhs().accept(exprVisitor,null);

        }

        @Override
        public CFGEnv visit(BooleanLiteral bool, CFGEnv aux) {
            printAstNode(bool);
            return null;
        }

        @Override
        public CFGEnv visit(CallExpression callExpression, CFGEnv aux) {
            printAstNode(callExpression);
            callExpression.getFunction().accept(exprVisitor,null);
            for (Expression arg : callExpression.getArgs()) {
                arg.accept(exprVisitor,null);
            }
            return null;
        }

        @Override
        public CFGEnv visit(CommaExpression commaExpression, CFGEnv aux) {
            printAstNode(commaExpression);
            for (Expression expr : commaExpression.getExpressions()) {
                expr.accept(exprVisitor,null);
            }
            return null;
        }

        @Override
        public CFGEnv visit(ConditionalExpression conditionalExpression, CFGEnv aux) {
            printAstNode(conditionalExpression);
            conditionalExpression.getCondition().accept(exprVisitor,null);
            conditionalExpression.getLeft().accept(exprVisitor,null);
            conditionalExpression.getRight().accept(exprVisitor,null);
            return null;
        }

        @Override
        public CFGEnv visit(FunctionExpression functionExpression, CFGEnv aux) {
            // aux == null --> this is the main function
            printAstNode(functionExpression);
            CFGEnv inCfgEnv = CFGEnv.createInCfgEnv();
            CFGNode entry = inCfgEnv.getAppendNode();
            functionExpression2CFGNode.put(functionExpression, entry);
            return  functionExpression.getBody().accept(stmtVisitor, inCfgEnv);
        }

        @Override
        public CFGEnv visit(Identifier identifier, CFGEnv aux) {
            printAstNode(identifier);
            if (useNodeUnderconstruction != null) {
                // we are making a use node
                useNodeUnderconstruction.addUse(identifier);
            }
            return null;
        }

        @Override
        public CFGEnv visit(MemberExpression memberExpression, CFGEnv aux) {
            printAstNode(memberExpression);
            h.Helper.printDebug("property", memberExpression.getProperty());
            memberExpression.getExpression().accept(exprVisitor,null);
            return null;
        }

        @Override
        public CFGEnv visit(MemberLookupExpression memberLookupExpression, CFGEnv aux) {
            printAstNode(memberLookupExpression);
            memberLookupExpression.getOperand().accept(exprVisitor,null);
            memberLookupExpression.getLookupKey().accept(exprVisitor,null);
            return null;
        }

        @Override
        public CFGEnv visit(MethodCallExpression methodCallExpression, CFGEnv aux) {
            printAstNode(methodCallExpression);
            methodCallExpression.getMemberExpression().accept(exprVisitor,null);
            for (Expression arg : methodCallExpression.getArgs()) {
                arg.accept(exprVisitor, null);
            }

            return null;
        }

        @Override
        public CFGEnv visit(NewExpression newExpression, CFGEnv aux) {
            printAstNode(newExpression);
            newExpression.getOperand().accept(exprVisitor, null);
            for (Expression arg : newExpression.getArgs()) {
                arg.accept(exprVisitor, null);
            }
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
            unaryExpression.getExpression().accept(exprVisitor,null);
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
                aux = stmt.accept(stmtVisitor, aux);
            }
            return aux;
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
            for (Map.Entry<Expression, Statement> entry : switchStatement.getCases()) {
                entry.getKey().accept(exprVisitor, null);
                entry.getValue().accept(stmtVisitor,null);
            }
            return null;
        }

        @Override
        public CFGEnv visit(ThrowStatement throwStatement, CFGEnv aux) {
            printAstNode(throwStatement);
            throwStatement.getExpression().accept(exprVisitor,null);
            return null;
        }

        @Override
        public CFGEnv visit(VariableNode variableNode, CFGEnv au) {
            if (au == null) {
                //throw new RuntimeException();
                au = DUMMY_ENV;
            }
            printAstNode(variableNode);
            Expression var = variableNode.getlValue();
            if (!(var instanceof Identifier)) throw new RuntimeException();
            Identifier id = (Identifier) var;

            // first process init, as it is evaluated first
            CFGEnv aux = variableNode.getInit().accept(exprVisitor, au);
            if (aux == null) {
                //throw new RuntimeException();
                aux = DUMMY_ENV;
            }

            CFGNode defNode = new CFGDef(variableNode, id);
            //aux.getAppendNode().getSuccessors().add(defNode);
            CFGEnv outEnv = CFGEnv.createOutCfgEnv(aux.getAppendNode(), defNode);

            var.accept(exprVisitor, null); // debug purposes only

            return outEnv;
        }

        @Override
        public CFGEnv visit(WhileStatement whileStatement, CFGEnv aux) {
            printAstNode(whileStatement);
            whileStatement.getCondition().accept(exprVisitor,null);
            whileStatement.getBody().accept(stmtVisitor,null);
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
    public CFGBuilder() {
        //DUMMY_ENV  = CFGEnv.createInCfgEnv();
    }
    public void processMain(FunctionExpression mainFunction) {
        exprVisitor.visit(mainFunction, null);
    }
    public static CFGEnv DUMMY_ENV = CFGEnv.createInCfgEnv(); // should be removed
}
