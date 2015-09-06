package dk.webbies.tscreate.paser;

import com.google.javascript.jscomp.parsing.parser.IdentifierToken;
import com.google.javascript.jscomp.parsing.parser.LiteralToken;
import com.google.javascript.jscomp.parsing.parser.Token;
import com.google.javascript.jscomp.parsing.parser.trees.*;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;

import java.util.*;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.Util.*;

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
            return new VariableNode(variable.location, convert(variable.lvalue), convert(variable.initializer));
        } else if (tree instanceof IdentifierExpressionTree) {
            IdentifierExpressionTree id = (IdentifierExpressionTree) tree;
            return new Identifier(id.location, id.identifierToken.value);
        } else if (tree instanceof LiteralExpressionTree) {
            Token literal = ((LiteralExpressionTree) tree).literalToken;
            if (literal instanceof IdentifierToken) {
                return new Identifier(loc, ((IdentifierToken) literal).value);
            } else if (literal instanceof LiteralToken){
                String value = ((LiteralToken) literal).value;
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    return new StringLiteral(loc, value.substring(1, value.length() - 1));
                } else if (value.matches("[0-9]*.?[0-9]*")) {
                    return new NumberLiteral(loc, Double.parseDouble(value));
                } else {
                    throw new RuntimeException("Could not recognize literal: " + value);
                }
            } else {
                switch (literal.type) {
                    case TRUE: return new BooleanLiteral(loc, true);
                    case FALSE: return new BooleanLiteral(loc, false);
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
            return new Return(loc, (Expression) AstTransformer.convert(((ReturnStatementTree) tree).expression));
        } else if (tree instanceof BinaryOperatorTree) {
            BinaryOperatorTree biOp = (BinaryOperatorTree) tree;
            return new BinaryExpression(loc, (Expression)convert(biOp.left), (Expression)convert(biOp.right), convertOperator(biOp.operator));
        } else if (tree instanceof ParenExpressionTree) {
            return convert(((ParenExpressionTree) tree).expression);
        } else if (tree instanceof CallExpressionTree) {
            CallExpressionTree call = (CallExpressionTree) tree;
            List<Expression> arguments = cast(Expression.class, call.arguments.arguments.stream().map(AstTransformer::convert).collect(Collectors.toList()));
            Expression operand = (Expression) convert(call.operand);
            return new CallExpression(loc, operand, arguments);
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
            Map<String, Expression> properties = new HashMap<>();
            cast(PropertyNameAssignmentTree.class, object.propertyNameAndValues).stream().forEach(prop -> {
                properties.put(prop.name.asIdentifier().value, (Expression) convert(prop.value));
            });
            return new ObjectLiteral(loc, properties);
        } else if (tree instanceof MemberExpressionTree) {
            MemberExpressionTree member = (MemberExpressionTree) tree;
            return new MemberExpression(loc, member.memberName.toString(), (Expression) convert(member.operand));
        }

        throw new RuntimeException("Cannot yet handle that kind of expression: " + tree.getClass().getName());
    }

    private static Operator convertOperator(Token operator) {
        switch (operator.type) {
            case MINUS: return Operator.MINUS;
            case STAR: return Operator.MULT;
            case SLASH: return Operator.DIV;
            case PERCENT: return Operator.MOD;
            case PLUS: return Operator.PLUS;
            case EQUAL: return Operator.ASSIGN;
            default:
                throw new RuntimeException("Dont know the operator: " + operator.type);
        }
    }
}
