package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.IdentifierToken;
import com.google.javascript.jscomp.parsing.parser.LiteralToken;
import com.google.javascript.jscomp.parsing.parser.Token;
import com.google.javascript.jscomp.parsing.parser.TokenType;
import com.google.javascript.jscomp.parsing.parser.trees.*;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.JavaScriptParser;

import java.util.*;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.Util.cast;

/**
 * Created by Erik Krogh Kristensen on 04-09-2015.
 */
public class AstTransformer {

    public static AstNode convert(ParseTree tree) {
        SourceRange loc = tree.location;
        if (tree instanceof VariableStatementTree) {
            VariableStatementTree variables = (VariableStatementTree) tree;
            List<Statement> statements = variables.declarations.declarations.stream().map(AstTransformer::convert).map(JavaScriptParser::toStatement).collect(Collectors.toList());
            return new BlockStatement(loc, statements);
        } else if (tree instanceof VariableDeclarationTree) {
            VariableDeclarationTree variable = (VariableDeclarationTree) tree;
            Expression initialize;
            if (variable.initializer != null) {
                initialize = (Expression) convert(variable.initializer);
            } else {
                initialize = new UnaryExpression(loc, Operator.VOID, new NumberLiteral(loc, 0));
            }
            return new VariableNode(variable.location, (Expression)convert(variable.lvalue), initialize);
        } else if (tree instanceof VariableDeclarationListTree) {
            VariableDeclarationListTree list = (VariableDeclarationListTree) tree;
            return new BlockStatement(loc, cast(Statement.class, list.declarations.stream().map(AstTransformer::convert).collect(Collectors.toList())));
        } else if (tree instanceof IdentifierExpressionTree) {
            IdentifierExpressionTree id = (IdentifierExpressionTree) tree;
            return new Identifier(id.location, id.identifierToken.value);
        } else if (tree instanceof LiteralExpressionTree) {
            Token literal = ((LiteralExpressionTree) tree).literalToken;
            if (literal instanceof IdentifierToken) {
                return new Identifier(loc, ((IdentifierToken) literal).value);
            } else if (literal instanceof LiteralToken){
                String value = ((LiteralToken) literal).value;
                if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                    return new StringLiteral(loc, value.substring(1, value.length() - 1));
                } else if (value.matches("[0-9]*.?[0-9]*")) {
                    if (value.startsWith("0x")) {
                        return new NumberLiteral(loc, Long.parseLong(value.substring(2, value.length()), 16));
                    } else {
                        return new NumberLiteral(loc, Double.parseDouble(value));
                    }
                } else if (value.startsWith("0x")) {
                    return new NumberLiteral(loc, Long.parseLong(value.toLowerCase().substring(2, value.length()), 16));
                } else if (value.startsWith("/")) {
                    String regExp = value.substring(1, value.length() - 1);
                    return new NewExpression(loc, new Identifier(loc, "RegExp"), Arrays.asList(new StringLiteral(loc, regExp)));
                } else if (value.substring(0, 1).matches("[0-9]")) {
                    return new NumberLiteral(loc, Double.valueOf(value));
                } else {
                    throw new RuntimeException("Could not recognize literal: " + value);
                }
            } else {
                switch (literal.type) {
                    case TRUE: return new BooleanLiteral(loc, true);
                    case FALSE: return new BooleanLiteral(loc, false);
                    case NULL: return new NullLiteral(loc);
                    default:
                        throw new RuntimeException("Dont know this kind of literal type: " + literal.type);
                }
            }
        } else if (tree instanceof FunctionDeclarationTree) {
            FunctionDeclarationTree funcDec = (FunctionDeclarationTree) tree;
            List<Identifier> arguments = cast(Identifier.class, funcDec.formalParameterList.parameters.stream().map(AstTransformer::convert).collect(Collectors.toList()));
            AstNode body = convert(funcDec.functionBody);
            Identifier name = null;
            if (funcDec.name != null) {
                name = new Identifier(funcDec.name.location, funcDec.name.value);
            }
            return new FunctionExpression(loc, name, (BlockStatement) body, arguments);
        } else if (tree instanceof BlockTree) {
            BlockTree block = (BlockTree) tree;
            List<Statement> statements = block.statements.stream().map(AstTransformer::convert).map(JavaScriptParser::toStatement).collect(Collectors.toList());
            return new BlockStatement(loc, statements);
        } else if (tree instanceof ReturnStatementTree) {
            Expression expression = null;
            ReturnStatementTree aReturn = (ReturnStatementTree) tree;
            if (aReturn.expression != null) {
                expression = (Expression) AstTransformer.convert(aReturn.expression);
            } else {
                expression = new UnaryExpression(loc, Operator.VOID, new NumberLiteral(loc, 0));
            }
            return new Return(loc, expression);
        } else if (tree instanceof BinaryOperatorTree) {
            BinaryOperatorTree biOp = (BinaryOperatorTree) tree;
            return new BinaryExpression(loc, (Expression)convert(biOp.left), (Expression)convert(biOp.right), convertOperator(biOp.operator));
        } else if (tree instanceof ParenExpressionTree) {
            return convert(((ParenExpressionTree) tree).expression);
        } else if (tree instanceof CallExpressionTree) {
            CallExpressionTree call = (CallExpressionTree) tree;
            List<Expression> arguments = cast(Expression.class, call.arguments.arguments.stream().map(AstTransformer::convert).collect(Collectors.toList()));
            Expression operand = (Expression) convert(call.operand);
            if (operand instanceof MemberExpression) {
                MemberExpression memberExpression = (MemberExpression) operand;
                return new MethodCallExpression(loc, memberExpression, arguments);
            } else {
                return new CallExpression(loc, operand, arguments);
            }
        } else if (tree instanceof ExpressionStatementTree) {
            ExpressionStatementTree statement = (ExpressionStatementTree) tree;
            return new ExpressionStatement(loc, (Expression) convert(statement.expression));
        } else if (tree instanceof IfStatementTree) {
            IfStatementTree ifTree = (IfStatementTree) tree;
            Statement elseBranch = null;
            if (ifTree.elseClause != null) {
                elseBranch = (Statement) convert(ifTree.elseClause);
            } else {
                elseBranch = new BlockStatement(ifTree.condition.location, Collections.EMPTY_LIST);
            }
            return new IfStatement(loc, (Expression) convert(ifTree.condition), (Statement) convert(ifTree.ifClause), elseBranch);
        } else if (tree instanceof ObjectLiteralExpressionTree) {
            ObjectLiteralExpressionTree object = (ObjectLiteralExpressionTree) tree;
            LinkedHashMap<String, Expression> properties = new LinkedHashMap<>();
            cast(PropertyNameAssignmentTree.class, object.propertyNameAndValues).stream().forEach(prop -> {
                Token name = prop.name;
                if (name.type == TokenType.IDENTIFIER) {
                    properties.put(name.asIdentifier().value, (Expression) convert(prop.value));
                } else if (name.type == TokenType.STRING){
                    String value = name.asLiteral().value;
                    properties.put(value.substring(1, value.length() - 1), (Expression) convert(prop.value));
                } else if (name.type == TokenType.NUMBER){
                    String value = name.asLiteral().value;
                    properties.put(value, (Expression) convert(prop.value));
                }
            });
            return new ObjectLiteral(loc, properties);
        } else if (tree instanceof MemberExpressionTree) {
            MemberExpressionTree member = (MemberExpressionTree) tree;
            return new MemberExpression(loc, member.memberName.toString(), (Expression) convert(member.operand));
        } else if (tree instanceof UnaryExpressionTree) {
            UnaryExpressionTree unOp = (UnaryExpressionTree) tree;
            return new UnaryExpression(loc, convertOperator(unOp.operator), (Expression)convert(unOp.operand));
        } else if (tree instanceof ThisExpressionTree) {
            return new ThisExpression(loc);
        } else if (tree instanceof NewExpressionTree) {
            NewExpressionTree newExp = (NewExpressionTree) tree;
            List<Expression> arguments = new ArrayList<>();
            if (newExp.arguments != null) {
                arguments = cast(Expression.class, newExp.arguments.arguments.stream().map(AstTransformer::convert).collect(Collectors.toList()));
            }
            Expression operand = (Expression) convert(newExp.operand);
            return new NewExpression(loc, operand, arguments);
        } else if (tree instanceof SwitchStatementTree) {
            SwitchStatementTree switchStatement = (SwitchStatementTree) tree;
            List<Map.Entry<Expression, Statement>> cases = switchStatement.caseClauses.stream().filter(clause -> clause instanceof CaseClauseTree).map(clause -> (CaseClauseTree) clause).map(AstTransformer::convertCaseClause).collect(Collectors.toList());

            Optional<ParseTree> defaultClauseTreeOptional = switchStatement.caseClauses.stream().filter(clause -> clause instanceof DefaultClauseTree).findAny();
            BlockStatement defaultClause = new BlockStatement(loc, Collections.EMPTY_LIST);
            if (defaultClauseTreeOptional.isPresent()) {
                DefaultClauseTree defaultClauseTree = (DefaultClauseTree) defaultClauseTreeOptional.get();
                defaultClause = new BlockStatement(defaultClauseTree.location, cast(Statement.class, defaultClauseTree.statements.stream().map(AstTransformer::convert).collect(Collectors.toList())));
            }

            return new SwitchStatement(loc, (Expression) convert(switchStatement.expression), cases, defaultClause);
        } else if (tree instanceof ConditionalExpressionTree) {
            ConditionalExpressionTree cond = (ConditionalExpressionTree) tree;
            return new ConditionalExpression(loc, (Expression) convert(cond.condition), (Expression) convert(cond.left), (Expression) convert(cond.right));
        } else if (tree instanceof ForStatementTree) {
            ForStatementTree forStatement = (ForStatementTree) tree;
            AstNode initializeAst = convert(forStatement.initializer);
            Statement initialize;
            if (initializeAst instanceof Statement) {
                initialize = (Statement) initializeAst;
            } else {
                initialize = new ExpressionStatement(loc, (Expression) initializeAst);
            }
            return new ForStatement(loc, initialize, (Expression) convert(forStatement.condition), (Expression) convert(forStatement.increment), (Statement) convert(forStatement.body));
        } else if (tree instanceof WhileStatementTree) {
            WhileStatementTree whileStatement = (WhileStatementTree) tree;
            return new WhileStatement(loc, (Expression) convert(whileStatement.condition), (Statement) convert(whileStatement.body));
        } else if (tree instanceof PostfixExpressionTree) {
            PostfixExpressionTree post = (PostfixExpressionTree) tree;
            Operator operator;
            switch (post.operator.type) {
                case PLUS_PLUS: operator = Operator.PLUS_PLUS; break;
                case MINUS_MINUS: operator = Operator.MINUS_MINUS; break;
                default:throw new RuntimeException("Unknown operator for postfix operator: " + post.operator);
            }
            return new UnaryExpression(loc, operator, (Expression)convert(post.operand));
        } else if (tree instanceof MemberLookupExpressionTree) {
            MemberLookupExpressionTree memberLook = (MemberLookupExpressionTree) tree;
            Expression operand = (Expression) convert(memberLook.operand);
            Expression lookupKey = (Expression) convert(memberLook.memberExpression);
            return new MemberLookupExpression(loc, operand, lookupKey);
        } else if (tree instanceof CommaExpressionTree) {
            CommaExpressionTree commaExp = (CommaExpressionTree) tree;
            return new CommaExpression(loc, cast(Expression.class, commaExp.expressions.stream().map(AstTransformer::convert).collect(Collectors.toList())));
        } else if (tree instanceof NullTree) {
            return new NullLiteral(loc);
        } else if (tree instanceof ArrayLiteralExpressionTree) {
            ArrayLiteralExpressionTree array = (ArrayLiteralExpressionTree) tree;
            List<AstNode> arguments = array.elements.stream().map(AstTransformer::convert).collect(Collectors.toList());

            // Adding this special to the arguments, so that the further analysis doesn't think that the arguments are actual arguments to the Array Constructor.
            arguments.add(0, new UnaryExpression(loc, Operator.VOID, new NumberLiteral(loc, 0)));

            return new NewExpression(loc, new Identifier(loc, "Array"), cast(Expression.class, arguments));
        } else if (tree instanceof ContinueStatementTree) {
            return new ContinueStatement(loc);
        } else if (tree instanceof BreakStatementTree) {
            return new BreakStatement(loc);
        } else if (tree instanceof ThrowStatementTree) {
            ThrowStatementTree throwStatement = (ThrowStatementTree) tree;
            return new ThrowStatement(loc, (Expression) convert(throwStatement.value));
        } else if (tree instanceof ForInStatementTree) {
            ForInStatementTree forIn = (ForInStatementTree) tree;
            AstNode initializerNode = convert(forIn.initializer);
            if (initializerNode instanceof Expression) {
                initializerNode = new ExpressionStatement(loc, (Expression) initializerNode);
            }
            return new ForInStatement(loc, (Statement) initializerNode, (Expression) convert(forIn.collection), (Statement) convert(forIn.body));
        } else if (tree instanceof TryStatementTree) {
            TryStatementTree tryStatement = (TryStatementTree) tree;
            BlockStatement finallyBlock;
            if (tryStatement.finallyBlock != null) {
                finallyBlock = (BlockStatement) convert(tryStatement.finallyBlock);
            } else {
                finallyBlock = new BlockStatement(loc, Collections.EMPTY_LIST);
            }
            return new TryStatement(loc, (Statement) convert(tryStatement.body), (CatchStatement) convert(tryStatement.catchBlock), finallyBlock);
        } else if (tree instanceof CatchTree) {
            CatchTree catchTree = (CatchTree) tree;
            return new CatchStatement(loc, (Identifier) convert(catchTree.exception), (Statement)convert(catchTree.catchBody));
        } else if (tree instanceof FinallyTree) {
            FinallyTree finallyTree = (FinallyTree) tree;
            return convert(finallyTree.block);
        } else if (tree instanceof DoWhileStatementTree) {
            DoWhileStatementTree doWhile = (DoWhileStatementTree) tree;
            return new WhileStatement(loc, (Expression) convert(doWhile.condition), (Statement) convert(doWhile.body));
        } else if (tree instanceof EmptyStatementTree) {
            return new BlockStatement(loc, Collections.EMPTY_LIST);
        }

        throw new RuntimeException("Cannot yet handle that kind of expression: " + tree.getClass().getName());
    }

    private static Map.Entry<Expression, Statement> convertCaseClause(CaseClauseTree caseClause) {
        BlockStatement statement = new BlockStatement(caseClause.location, cast(Statement.class, caseClause.statements.stream().map(AstTransformer::convert).collect(Collectors.toList())));
        return new AbstractMap.SimpleEntry<>((Expression) convert(caseClause.expression), statement);
    }

    private static Operator convertOperator(Token operator) {
        switch (operator.type) {
            case MINUS: return Operator.MINUS;
            case STAR: return Operator.MULT;
            case SLASH: return Operator.DIV;
            case PERCENT: return Operator.MOD;
            case PLUS: return Operator.PLUS;

            case MINUS_EQUAL: return Operator.MINUS_EQUAL;
            case LEFT_SHIFT_EQUAL: return Operator.MINUS_EQUAL; // TODO:
            case STAR_EQUAL: return Operator.MULT_EQUAL;
            case SLASH_EQUAL: return Operator.DIV_EQUAL;
            case PERCENT_EQUAL: return Operator.MOD_EQUAL;
            case PLUS_EQUAL: return Operator.PLUS_EQUAL;

            case EQUAL: return Operator.EQUAL;
            case NOT_EQUAL: return Operator.NOT_EQUAL;
            case EQUAL_EQUAL: return Operator.EQUAL_EQUAL;
            case NOT_EQUAL_EQUAL: return Operator.NOT_EQUAL_EQUAL;
            case EQUAL_EQUAL_EQUAL: return Operator.EQUAL_EQUAL_EQUAL;

            case INSTANCEOF: return Operator.INSTANCEOF;
            case BANG: return Operator.NOT;
            case TYPEOF: return Operator.TYPEOF;
            case AND: return Operator.AND;
            case OR: return Operator.OR;
            case VOID: return Operator.VOID;

            case LESS_EQUAL: return Operator.LESS_THAN_EQUAL;
            case OPEN_ANGLE: return Operator.LESS_THAN;
            case GREATER_EQUAL: return Operator.GREATER_THAN_EQUAL;
            case CLOSE_ANGLE: return Operator.GREATER_THAN;

            case MINUS_MINUS: return Operator.MINUS_MINUS;
            case PLUS_PLUS: return Operator.PLUS_PLUS;

            case IN: return Operator.IN;
            case DELETE: return Operator.DELETE;

            case AMPERSAND: return Operator.BITWISE_AND;
            case BAR: return Operator.BITWISE_OR;
            case CARET: return Operator.BITWISE_XOR;
            case TILDE: return Operator.BITWISE_NOT;
            case LEFT_SHIFT: return Operator.LEFT_SHIFT;
            case RIGHT_SHIFT: return Operator.RIGHT_SHIFT;
            case UNSIGNED_RIGHT_SHIFT: return Operator.UNSIGNED_RIGHT_SHIFT;
            case RIGHT_SHIFT_EQUAL: return Operator.RIGHT_SHIFT_EQUAL;
            case UNSIGNED_RIGHT_SHIFT_EQUAL: return Operator.UNSIGNED_RIGHT_SHIFT_EQUAL;


            default:
                throw new RuntimeException("Dont know the operator: " + operator.type);
        }
    }
}
