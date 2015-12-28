package dk.webbies.tscreate.paser;

import dk.webbies.tscreate.util.Util;
import dk.webbies.tscreate.paser.AST.*;
import dk.webbies.tscreate.util.Helper;

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
        Helper.printDebug(String.valueOf(count), Helper.getText(stmt));
        Helper.printDebug("e" + count , ">>>>>>>>>>>>>>>>>>>>>");
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
            //Helper.printDebug("lhs, rhs: ", binOp.getLhs().toString() + ", " + binOp.getRhs().toString());
            if (false && au == null) {
                throw new RuntimeException(binOp.toString());
                // see the case for PLUS/MINUS/MULT;
            }
            printAstNode(binOp);
            switch (binOp.getOperator()) {
                case EQUAL:
                case PLUS_EQUAL:
                case MINUS_EQUAL:
                case MULT_EQUAL:
                {
                    // preparing for creating defnode: def(lhs)
                    Expression var = binOp.getLhs();
                    Identifier id;
                    if (var instanceof ThisExpression) return au;
                    if (var instanceof Identifier) {
                        id = (Identifier) var;
                    } else if (var instanceof MemberExpression || var instanceof DynamicAccessExpression) {
                        return binOp.getRhs().accept(exprVisitor, au);
                    }else throw new RuntimeException(var.getClass().toString());
                    // process rhs first
                    CFGEnv aux = binOp.getRhs().accept(exprVisitor, au);
                    if (aux == null) {
                        throw new RuntimeException();

                    }
                    CFGNode defNode = new CFGDef(binOp, id);
                    CFGEnv outEnv = CFGEnv.createOutCfgEnv(aux.getAppendNode(), defNode, aux.ssaEnv()); // pass aux.SSAEnv
                    //var.accept(exprVisitor, null); // debug purposes only

                    return outEnv;
                }
                case PLUS:
                case MINUS:
                case MULT:
                case EQUAL_EQUAL:
                case NOT_EQUAL_EQUAL:
                case LESS_THAN_EQUAL:
                case LESS_THAN:
                case GREATER_THAN:
                case GREATER_THAN_EQUAL:
                case INSTANCEOF:
                case AND:
                case OR:
                case EQUAL_EQUAL_EQUAL:
                {

                    // creates a CFGUseNode if necessary (otherwise use the one under construction)
                    boolean isTopUseNode = (useNodeUnderconstruction == null);
                    {if (isTopUseNode) Helper.printDebug("TOPNODE");
                    printAstNode(binOp);}
                    CFGUse currUseNode = makeUseNode(binOp);

                    Expression lhs = binOp.getLhs();
                    Expression rhs = binOp.getRhs();
                    if (lhs instanceof Identifier)
                        currUseNode.addUse((Identifier) lhs);
                    else
                        lhs.accept(exprVisitor, au); // we used to pass null as aux here! but hey, we want to pass ssaEnv (along with its aux)
                    if (rhs instanceof Identifier)
                        currUseNode.addUse((Identifier) rhs);
                    else
                        rhs.accept(exprVisitor, au); // we used to pass null as aux here!

                    if (isTopUseNode) {
                        CFGUse useNode = getCurrentAndResetUseNode();
                        assert (useNode == currUseNode);
                        if (au == null) throw new RuntimeException();
                        CFGEnv outEnv = CFGEnv.createOutCfgEnv(au.getAppendNode(), useNode, au.ssaEnv());
                        assert (useNodeUnderconstruction == null);
                        return outEnv;
                    }
                    return null;
                }
                default:
                    throw new RuntimeException("unhandled bin operator " + binOp + ", " + binOp.getOperator());
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
        public CFGEnv visit(CallExpression callExpression, CFGEnv aux) {//TODO: test
            printAstNode(callExpression);
            Expression expr = callExpression.getFunction();
            if (expr instanceof Identifier) {
                //assert (useNodeUnderconstruction == null);
                //makeUseNode(expr);
                aux = createUseEnv((Identifier) expr, aux);
            }
            expr.accept(exprVisitor, aux);
            for (Expression arg : callExpression.getArgs()) {
                if (arg instanceof Identifier) aux = createUseEnv((Identifier) arg, aux);
                else aux = arg.accept(exprVisitor,aux);
            }
            return aux;
        }

        @Override
        public CFGEnv visit(CommaExpression commaExpression, CFGEnv aux) {// TODO: what is comma expr?
            printAstNode(commaExpression);
            for (Expression expr : commaExpression.getExpressions()) {
                aux = expr.accept(exprVisitor, aux);
            }
            return aux;
        }

        @Override
        public CFGEnv visit(ConditionalExpression conditionalExpression, CFGEnv aux) { // TODO: test this case
            /*printAstNode(conditionalExpression);
            conditionalExpression.getCondition().accept(exprVisitor,null);
            conditionalExpression.getLeft().accept(exprVisitor,null);
            conditionalExpression.getRight().accept(exprVisitor,null);
            return null;*/

            return makeConditional(conditionalExpression.getCondition(), conditionalExpression.getLeft(), conditionalExpression.getRight(), aux);
        }

        @Override
        public CFGEnv visit(FunctionExpression functionExpression, CFGEnv aux) {
            // aux == null --> this is the main function
            printAstNode(functionExpression);

            CFGEnv inCfgEnv = CFGEnv.createInCfgEnv();
            //CFGJoin artificialJoin = new CFGJoin(inCfgEnv.getCopyOfSSAEnv()); // see paper Single pass generation of SSA ...
            CFGJoin artificialJoin = new CFGJoin(67);
            CFGNode entry = inCfgEnv.getAppendNode();
            functionExpression2CFGNode.put(functionExpression, entry);
            CFGEnv realExit =  functionExpression.getBody().accept(stmtVisitor, inCfgEnv);
            // we can process artificialJoin (mainly backup values) here if necessary (***)
            artificialJoin.ssaEnv = realExit.getCopyOfSSAEnv();
            return CFGEnv.createOutCfgEnv(new CFGNode[] {entry, realExit.getAppendNode()}, artificialJoin, realExit.ssaEnv()); // TODO:***
        }

         @Override
        public CFGEnv visit(Identifier identifier, CFGEnv aux) {
            printAstNode(identifier);
            /*if (useNodeUnderconstruction != null) {
                // we are making a use node (e.g. for cond in if (cond) ...)
                useNodeUnderconstruction.addUse(identifier);
                CFGUse useNode = getCurrentAndResetUseNode();
                if (aux == null) throw new RuntimeException();
                CFGEnv outEnv = CFGEnv.createOutCfgEnv(aux.getAppendNode(), useNode, aux.ssaEnv());
                assert (useNodeUnderconstruction == null);
                return outEnv;
            }*/
            return aux;
        }

        @Override
        public CFGEnv visit(MemberExpression memberExpression, CFGEnv aux) {
            printAstNode(memberExpression);
            Expression expr = memberExpression.getExpression();
            if (expr instanceof Identifier) {
                //assert (useNodeUnderconstruction == null);
                //makeUseNode(expr);
                return createUseEnv((Identifier) expr, aux);
            }

            return expr.accept(exprVisitor, aux);
        }

        @Override
        public CFGEnv visit(DynamicAccessExpression memberExpression, CFGEnv aux) {// TODO: a use of operand
            Helper.printDebug("dynamic_access: " , memberExpression.toString());
            printAstNode(memberExpression);
            Expression expr = memberExpression.getOperand();
            if (expr instanceof Identifier) {
                //assert (useNodeUnderconstruction == null);
                //makeUseNode(expr);
                return createUseEnv((Identifier) expr, aux);
            }

            return expr.accept(exprVisitor, aux);

        }

        @Override
        public CFGEnv visit(MethodCallExpression methodCallExpression, CFGEnv aux) {

            printAstNode(methodCallExpression);
            Expression expr = methodCallExpression.getMemberExpression();
            if (expr instanceof Identifier) {
                //assert (useNodeUnderconstruction == null);
                //makeUseNode(expr);
                aux = createUseEnv((Identifier) expr, aux);
            }


            for (Expression arg : methodCallExpression.getArgs()) {
                if (arg instanceof Identifier) {
                    aux = createUseEnv((Identifier) arg, aux);
                    continue;
                }
                aux = arg.accept(exprVisitor, aux);
            }
            aux = expr.accept(exprVisitor, aux);
            return aux;
        }

        @Override
        public CFGEnv visit(NewExpression newExpression, CFGEnv aux) {
            printAstNode(newExpression);

            aux = newExpression.getOperand().accept(exprVisitor, aux);
            for (Expression arg : newExpression.getArgs()) {
                if (arg instanceof Identifier) aux = createUseEnv((Identifier) arg, aux);
                else aux  = arg.accept(exprVisitor, aux);
            }
            return aux;
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
        public CFGEnv visit(UnaryExpression unaryExpression, CFGEnv aux) {//TODO: test
            printAstNode(unaryExpression);
            // creates a CFGUseNode if necessary (otherwise use the one under construction)
            boolean isTopUseNode = (useNodeUnderconstruction == null);
            CFGUse currUseNode = makeUseNode(unaryExpression);

            Expression expr = unaryExpression.getExpression();

            if (expr instanceof Identifier)
                currUseNode.addUse((Identifier) expr);
            else
                expr.accept(exprVisitor, null);

            if (isTopUseNode) {
                CFGUse useNode = getCurrentAndResetUseNode();
                assert (useNode == currUseNode);
                CFGEnv outEnv = CFGEnv.createOutCfgEnv(aux.getAppendNode(), useNode, aux.ssaEnv());
                assert (useNodeUnderconstruction == null);
                return outEnv;
            }


            unaryExpression.getExpression().accept(exprVisitor,null);
            return aux;
        }

        @Override
        public CFGEnv visit(UndefinedLiteral undefinedLiteral, CFGEnv aux) {
            printAstNode(undefinedLiteral);
            return aux;
        }

        @Override
        public CFGEnv visit(PhiNodeExpression phiNode, CFGEnv aux) {
            throw new RuntimeException();
        }

        @Override
        public CFGEnv visit(GetterExpression getter, CFGEnv aux) {//TODO:
            printAstNode(getter);
            return aux;
        }

        @Override
        public CFGEnv visit(SetterExpression setter, CFGEnv aux) { //TODO
            printAstNode(setter);
            return aux;
        }
    }

    class CFGStmtVisitor implements CFGStatementVisitor<CFGEnv> {
         @Override
        public CFGEnv visit(BlockStatement block, CFGEnv aux) {
            printAstNode(block);
            if (aux.ssaEnv() == null) throw new RuntimeException();
            for (Statement stmt : block.getStatements()) {
                aux = stmt.accept(stmtVisitor, aux);

                if (aux.ssaEnv() == null) throw new RuntimeException("exc: " + stmt);
            }
            return aux;
        }

        @Override
        public CFGEnv visit(BreakStatement breakStatement, CFGEnv aux) {//TODO:
            printAstNode(breakStatement);
            return aux;
        }

        @Override
        public CFGEnv visit(ContinueStatement continueStatement, CFGEnv aux) {//TODO:
            printAstNode(continueStatement);
            return aux;
        }

        @Override
        public CFGEnv visit(ExpressionStatement expressionStatement, CFGEnv aux) {
            printAstNode(expressionStatement);

            Expression expr = expressionStatement.getExpression();
            return expr.accept(exprVisitor,aux);

        }

        @Override
        public CFGEnv visit(ForStatement forStatement, CFGEnv aux) {//TODO: handle properly
            printAstNode(forStatement);
            return makeConditional(forStatement.getCondition(), forStatement.getBody(),new BlockStatement(forStatement.location, Collections.emptyList()), aux);
        }

        @Override
        public CFGEnv visit(IfStatement ifStatement, CFGEnv aux) {
            CFGEnv joinNode = makeConditional(ifStatement.getCondition(), ifStatement.getIfBranch(), ifStatement.getElseBranch(), aux);

            return joinNode;
        }

        @Override
        public CFGEnv visit(Return aReturn, CFGEnv aux) {
            printAstNode(aReturn);
            if (aReturn.getExpression() instanceof Identifier) return createUseEnv((Identifier) aReturn.getExpression(), aux);
            return aReturn.getExpression().accept(exprVisitor, aux);

        }

        @Override
        public CFGEnv visit(SwitchStatement switchStatement, CFGEnv aux) {//TODO:
            if (true) return aux;
            printAstNode(switchStatement);
            switchStatement.getExpression().accept(exprVisitor, null);
            for (Map.Entry<Expression, Statement> entry : switchStatement.getCases()) {
                entry.getKey().accept(exprVisitor, null);
                entry.getValue().accept(stmtVisitor,null);
            }
            return aux;
        }

        @Override
        public CFGEnv visit(ThrowStatement throwStatement, CFGEnv aux) {//TODO
            if (true) return aux;
            printAstNode(throwStatement);
            throwStatement.getExpression().accept(exprVisitor,null);
            return aux;
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
            CFGEnv outEnv = CFGEnv.createOutCfgEnv(aux.getAppendNode(), defNode, aux.ssaEnv());

            //var.accept(exprVisitor, aux); // debug purposes only

            return outEnv;
        }

        @Override
        public CFGEnv visit(WhileStatement whileStatement, CFGEnv aux) {// TODO: handle properly (soundly)
            printAstNode(whileStatement);
            //whileStatement.getCondition().accept(exprVisitor,null);
            //whileStatement.getBody().accept(stmtVisitor,null);
            return makeConditional(whileStatement.getCondition(), whileStatement.getBody(), new BlockStatement(whileStatement.location, Collections.emptyList()), aux);
        }

        @Override
        public CFGEnv visit(ForInStatement forinStatement, CFGEnv aux) {//TODO
            printAstNode(forinStatement);
            return aux;
            //return makeConditional(forinStatement.getCondition(), forStatement.getBody(),new BlockStatement(forStatement.location, Collections.emptyList()), aux);
        }

        @Override
        public CFGEnv visit(TryStatement tryStatement, CFGEnv aux) {
            printAstNode(tryStatement);

            return tryStatement.getTryBlock().accept(stmtVisitor, aux);
        }

        @Override
        public CFGEnv visit(CatchStatement catchStatement, CFGEnv aux) {
            printAstNode(catchStatement);
            return aux;
        }

        @Override
        public CFGEnv visit(LabeledStatement labeledStatement, CFGEnv aux) {
            printAstNode(labeledStatement);
            labeledStatement.getStatement().accept(stmtVisitor, aux);
            return aux;
        }
    }
    public CFGBuilder() {
        
    }
    private CFGEnv makeConditional(Expression condition, Statement left, Statement right, CFGEnv aux) {
        if (aux.ssaEnv() == null) throw new RuntimeException();
        if (condition instanceof Identifier) {
            assert (useNodeUnderconstruction == null);
            makeUseNode(condition);
        }
        CFGEnv branchEnv = condition.accept(exprVisitor, aux);
        //Helper.printDebug("inja branch env: ", branchEnv.ssaEnv().id2last.toString());
        CFGJoin joinNode = new CFGJoin(67);

        CFGEnv inEnvLeft = branchEnv.copy();
        CFGEnv leftEnv = left.accept(stmtVisitor, inEnvLeft); // so each one can make changes to its CFGEnv.ssaEnv

        CFGEnv inEnvRight = inEnvLeft.copy();
        CFGEnv rightEnv = right.accept(stmtVisitor, inEnvRight);

        Helper.printDebug("branch env: ", branchEnv.ssaEnv().id2last.toString());
        Helper.printDebug("left env: ", leftEnv.ssaEnv().id2last.toString());
        Helper.printDebug("right env: ", rightEnv.ssaEnv().id2last.toString());

        SSAEnv mergedSSAEnv = SSAEnv.MergeSSAEnvs(branchEnv.ssaEnv(), leftEnv.ssaEnv(), rightEnv.ssaEnv());
        Helper.printDebug("merged: ", mergedSSAEnv.id2last.toString());
        joinNode.ssaEnv = mergedSSAEnv.copy();

        return CFGEnv.createOutCfgEnv(new CFGNode[]{leftEnv.getAppendNode(), rightEnv.getAppendNode()}, joinNode, mergedSSAEnv);

    }
    private final CFGEnv createUseEnv(Identifier identifier, CFGEnv aux) {
        // we are making a use node (e.g. for cond in if (cond) ...)
        boolean isTopUseNode = (useNodeUnderconstruction == null);
        CFGUse currUseNode = makeUseNode(identifier);

        useNodeUnderconstruction.addUse(identifier);

        if (isTopUseNode) {
            CFGUse useNode = getCurrentAndResetUseNode();
            assert (useNode == currUseNode);
            if (aux == null) throw new RuntimeException();
            CFGEnv outEnv = CFGEnv.createOutCfgEnv(aux.getAppendNode(), useNode, aux.ssaEnv());
            assert (useNodeUnderconstruction == null);
            return outEnv;
        }
        return null;
    }

    private CFGEnv makeConditional(Expression condition, Expression left, Expression right, CFGEnv aux) {
        if (aux.ssaEnv() == null) throw new RuntimeException();
        CFGEnv branchEnv;
        if (condition instanceof Identifier) {
            branchEnv = createUseEnv((Identifier) condition, aux);
        } else {
            branchEnv = condition.accept(exprVisitor, aux);
        }
        //Helper.printDebug("inja branch env: ", branchEnv.ssaEnv().id2last.toString());
        CFGJoin joinNode = new CFGJoin(67);

        CFGEnv inEnvLeft = branchEnv.copy();
        CFGEnv leftEnv = left.accept(exprVisitor, inEnvLeft); // so each one can make changes to its CFGEnv.ssaEnv

        CFGEnv inEnvRight = inEnvLeft.copy();
        CFGEnv rightEnv = right.accept(exprVisitor, inEnvRight);

        Helper.printDebug("branch env: ", branchEnv.ssaEnv().id2last.toString());
        Helper.printDebug("left env: ", leftEnv.ssaEnv().id2last.toString());
        Helper.printDebug("right env: ", rightEnv.ssaEnv().id2last.toString());

        SSAEnv mergedSSAEnv = SSAEnv.MergeSSAEnvs(branchEnv.ssaEnv(), leftEnv.ssaEnv(), rightEnv.ssaEnv());
        Helper.printDebug("merged: ", mergedSSAEnv.id2last.toString());
        joinNode.ssaEnv = mergedSSAEnv.copy();

        return CFGEnv.createOutCfgEnv(new CFGNode[]{leftEnv.getAppendNode(), rightEnv.getAppendNode()}, joinNode, mergedSSAEnv);

    }

    public void processMain(FunctionExpression mainFunction) {
        exprVisitor.visit(mainFunction, null);
    }
    /*private final void makeUseNodeIfIdentifier(Expression expr) {
        if (expr instanceof Identifier) {
            assert (useNodeUnderconstruction == null);
            makeUseNode(expr);
        }
    }*/
    private static String toDot(String clusterName, CFGNode root) {
        StringBuilder ret = new StringBuilder();

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
        Helper.printDebug("nodes size ", nodes.size() + " ");
        // TODO id(node)=node.hashCode() id [label="{ ... }"]->
        if (false) {
            for (CFGNode n : nodes) {
                ret.append(n.getClass() + "::");   //w.print(n.getClass() + "::");
                ret.append(n.hashCode() + " : " + (n.getAstNode() == null ? "E" : n.getAstNode().toString()));  //w.println(n.hashCode() + " : " + (n.getAstNode() == null ? "E" : n.getAstNode().toString()));
            }
            ret.append(root.getAstNode()==null?"E":root.getAstNode().toString()); //w.println(root.getAstNode()==null?"E":root.getAstNode().toString());
        }


        ret.append("subgraph " +clusterName +" {");//w.println("digraph {");
        ret.append("node[shape=record]");//w.println("node[shape=record]");
        ret.append("rankdir=TD");//w.println("rankdir=TD");
        for (CFGNode n : nodes) {
            String s;

            s = Util.escString(n.toString());
            String src = "\"" + s + "\"";
            for (CFGNode succ : n.getSuccessors()) {
                //w.println(n.hashCode() + " -> " + succ.hashCode());
                ret.append(src + " -> \"" + Util.escString(succ.toString()) +"\"");//w.println(src + " -> \"" + Util.escString(succ.toString()) +"\"");
            }


        }
        ret.append("}");//w.println("}");
        return ret.toString();
    }

    public void toDot(PrintWriter w) {
        w.println("digraph {\n" +
                "compound=true");
        int nFunc = 0;
        for (Map.Entry<FunctionExpression,CFGNode> func_node : functionExpression2CFGNode.entrySet()) {
           String cluster = toDot("clutster_" + (nFunc++), func_node.getValue());
            w.println(cluster);
        }
        w.println("}");
    }

}
