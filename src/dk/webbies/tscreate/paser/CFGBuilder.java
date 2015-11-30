package dk.webbies.tscreate.paser;

import dk.webbies.tscreate.Util;
import dk.webbies.tscreate.paser.AST.*;

import java.io.PrintWriter;
import java.util.*;

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
            //h.Helper.printDebug("lhs, rhs: ", binOp.getLhs().toString() + ", " + binOp.getRhs().toString());
            if (false && au == null) {
                throw new RuntimeException(binOp.toString());
                // see the case for PLUS/MINUS/MULT;
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
                        throw new RuntimeException();

                    }
                    CFGNode defNode = new CFGDef(binOp, id);
                    CFGEnv outEnv = CFGEnv.createOutCfgEnv(aux.getAppendNode(), defNode, aux.ssAEnv()); // pass aux.SSAEnv
                    //var.accept(exprVisitor, null); // debug purposes only

                    return outEnv;
                }
                case PLUS:
                case MINUS:
                case MULT:
                case EQUAL_EQUAL:
                case LESS_THAN_EQUAL:
                case LESS_THAN:
                case GREATER_THAN:
                case GREATER_THAN_EQUAL:
                {

                    // creates a CFGUseNode if necessary (otherwise use the one under construction)
                    boolean isTopUseNode = (useNodeUnderconstruction == null);
                    {if (isTopUseNode) h.Helper.printDebug("TOPNODE");
                    printAstNode(binOp);}
                    CFGUse currUseNode = makeUseNode(binOp);

                    Expression lhs = binOp.getLhs();
                    Expression rhs = binOp.getRhs();
                    if (lhs instanceof Identifier)
                        currUseNode.addUse(lhs);
                    else
                        lhs.accept(exprVisitor, au); // we used to pass null as aux here! but hey, we want to pass ssaEnv (along with it aux)
                    if (rhs instanceof Identifier)
                        currUseNode.addUse(rhs);
                    else
                        rhs.accept(exprVisitor, au); // we used to pass null as aux here!

                    if (isTopUseNode) {
                        CFGUse useNode = getCurrentAndResetUseNode();
                        assert (useNode == currUseNode);
                        if (au == null) throw new RuntimeException();
                        CFGEnv outEnv = CFGEnv.createOutCfgEnv(au.getAppendNode(), useNode, au.ssAEnv());
                        assert (useNodeUnderconstruction == null);
                        return outEnv;
                    }

                }
                default:
                    return au;
            }

            //binOp.getLhs().accept(exprVisitor, null);
            //binOp.getRhs().accept(exprVisitor,null);

        }

        @Override
        public CFGEnv visit(BooleanLiteral bool, CFGEnv aux) {
            printAstNode(bool);
            return aux;
        }

        @Override
        public CFGEnv visit(CallExpression callExpression, CFGEnv aux) {
            printAstNode(callExpression);
            callExpression.getFunction().accept(exprVisitor,null);
            for (Expression arg : callExpression.getArgs()) {
                arg.accept(exprVisitor,null);
            }
            return aux;
        }

        @Override
        public CFGEnv visit(CommaExpression commaExpression, CFGEnv aux) {
            printAstNode(commaExpression);
            for (Expression expr : commaExpression.getExpressions()) {
                expr.accept(exprVisitor,null);
            }
            return aux;
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
            CFGJoin artificialJoin = new CFGJoin(inCfgEnv.getCopyOfSSAEnv()); // see paper Single pass generation of SSA ...
            CFGNode entry = inCfgEnv.getAppendNode();
            functionExpression2CFGNode.put(functionExpression, entry);
            CFGEnv realExit =  functionExpression.getBody().accept(stmtVisitor, inCfgEnv);
            // we can process artificialJoin (mainly backup vaues) here if necessary (***)
            return CFGEnv.createOutCfgEnv(new CFGNode[] {entry, realExit.getAppendNode()}, artificialJoin, artificialJoin.getBackupValues()); // TODO:***
        }

        @Override
        public CFGEnv visit(Identifier identifier, CFGEnv aux) {
            printAstNode(identifier);
            if (useNodeUnderconstruction != null) {
                // we are making a use node
                useNodeUnderconstruction.addUse(identifier);
            }
            return aux;
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
            return aux;
        }

        @Override
        public CFGEnv visit(NumberLiteral numberLiteral, CFGEnv aux) {
            printAstNode(numberLiteral);
            return aux;
        }

        @Override
        public CFGEnv visit(ObjectLiteral objectLiteral, CFGEnv aux) {
            printAstNode(objectLiteral);
            return aux;
        }

        @Override
        public CFGEnv visit(StringLiteral stringLiteral, CFGEnv aux) {
            printAstNode(stringLiteral);
            return aux;
        }

        @Override
        public CFGEnv visit(ThisExpression thisExpression, CFGEnv aux) {
            printAstNode(thisExpression);
            return aux;
        }

        @Override
        public CFGEnv visit(UnaryExpression unaryExpression, CFGEnv aux) {
            printAstNode(unaryExpression);
            // creates a CFGUseNode if necessary (otherwise use the one under construction)
            boolean isTopUseNode = (useNodeUnderconstruction == null);
            CFGUse currUseNode = makeUseNode(unaryExpression);

            Expression expr = unaryExpression.getExpression();

            if (expr instanceof Identifier)
                currUseNode.addUse(expr);
            else
                expr.accept(exprVisitor, null);

            if (isTopUseNode) {
                CFGUse useNode = getCurrentAndResetUseNode();
                assert (useNode == currUseNode);
                CFGEnv outEnv = CFGEnv.createOutCfgEnv(aux.getAppendNode(), useNode, aux.ssAEnv());
                assert (useNodeUnderconstruction == null);
                return outEnv;
            }


            unaryExpression.getExpression().accept(exprVisitor,null);
            return null;
        }

        @Override
        public CFGEnv visit(UndefinedLiteral undefinedLiteral, CFGEnv aux) {
            printAstNode(undefinedLiteral);
            return aux;
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
            if (aux.ssAEnv() == null) throw new RuntimeException();
            for (Statement stmt : block.getStatements()) {
                aux = stmt.accept(stmtVisitor, aux);

                if (aux.ssAEnv() == null) throw new RuntimeException("exc: " + stmt);
                h.Helper.printDebug("?????????????????????");
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
            return expr.accept(exprVisitor,aux);

        }

        @Override
        public CFGEnv visit(ForStatement forStatement, CFGEnv aux) {
            printAstNode(forStatement);
            return null;
        }

        @Override
        public CFGEnv visit(IfStatement ifStatement, CFGEnv aux) {
            CFGEnv joinNode = makeConditional(ifStatement.getCondition(), ifStatement.getIfBranch(), ifStatement.getElseBranch(), aux);
            // TODO: update joinNode (create defs in it, etc)
            return joinNode;
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
                throw new RuntimeException();
            }
            printAstNode(variableNode);
            Expression var = variableNode.getlValue();
            if (!(var instanceof Identifier)) throw new RuntimeException();
            Identifier id = (Identifier) var;

            // first process init, as it is evaluated first
            CFGEnv aux = variableNode.getInit().accept(exprVisitor, au);

            if (aux == null) {
                throw new RuntimeException();
            }

            CFGNode defNode = new CFGDef(variableNode, id);
            //aux.getAppendNode().getSuccessors().add(defNode);
            CFGEnv outEnv = CFGEnv.createOutCfgEnv(aux.getAppendNode(), defNode, aux.ssAEnv());

            //var.accept(exprVisitor, aux); // debug purposes only

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
    private CFGEnv makeConditional(Expression condition, Statement left, Statement right, CFGEnv aux) {
        if (aux.ssAEnv() == null) throw new RuntimeException();
        CFGEnv branchEnv = condition.accept(exprVisitor, aux);
        CFGJoin joinNode = new CFGJoin(branchEnv.getCopyOfSSAEnv()); // SSAEnv() should work as well

        CFGEnv inEnvLeft = branchEnv.copy();
        h.Helper.printDebug("input env: ", inEnvLeft.ssaEnv.id2last.toString());
        CFGEnv leftEnv = left.accept(stmtVisitor, inEnvLeft); // so each one can make changes to its CFGEnv.ssaEnv
        h.Helper.printDebug("modified env: ", inEnvLeft.ssaEnv.id2last.toString());
        CFGEnv inEnvRight = inEnvLeft.copy();
        CFGEnv rightEnv = right.accept(stmtVisitor, inEnvRight);

        SSAEnv mergedSSAEnv = SSAEnv.MergeSSAEnvs(branchEnv.ssAEnv(), inEnvLeft.ssAEnv(), inEnvRight.ssAEnv());
        return CFGEnv.createOutCfgEnv(new CFGNode[]{leftEnv.getAppendNode(), rightEnv.getAppendNode()}, joinNode, mergedSSAEnv);

    }
    public void processMain(FunctionExpression mainFunction) {
        exprVisitor.visit(mainFunction, null);
    }
    //public static CFGEnv DUMMY_ENV = CFGEnv.createInCfgEnv();
    public static void toDot(PrintWriter w, CFGNode root) {
        List<CFGNode> nodes = new LinkedList<>();
        HashSet<CFGNode> visited = new HashSet<>();
        Queue<CFGNode> q = new LinkedList<>();
        q.add(root);
        visited.add(root);
        while (!q.isEmpty()) {
            CFGNode n = q.remove();
            nodes.add(n);
            //visited.add(n);
            for (CFGNode succ : n.getSuccessors()) {
                if (!visited.contains(succ)) {
                    q.add(succ);
                    visited.add(succ);
                }
            }
        }
        h.Helper.printDebug("nodes size ", nodes.size() + " ");
        // TODO id(node)=node.hashCode() id [label="{ ... }"]->
        if (false) {
            for (CFGNode n : nodes) {
                w.print(n.getClass() + "::");
                w.println(n.hashCode() + " : " + (n.getAstNode() == null ? "E" : n.getAstNode().toString()));
            }
            w.println(root.getAstNode()==null?"E":root.getAstNode().toString());
        }


        w.println("digraph {");
        for (CFGNode n : nodes) {
            String s;

            s = Util.escString(n.toString());
            String src = "\"" + s + "\"";
            for (CFGNode succ : n.getSuccessors()) {
                //w.println(n.hashCode() + " -> " + succ.hashCode());
                w.println(src + " -> \"" + Util.escString(succ.toString()) +"\"");
            }


        }
        w.println("}");
    }
}
