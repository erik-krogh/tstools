package dk.webbies.tscreate.paser;


import jdk.nashorn.internal.ir.*;
import jdk.nashorn.internal.parser.Parser;
import jdk.nashorn.internal.parser.TokenType;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.options.Options;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JavaScript parser.
 * Using the nashorn parser build into the JVM (1.8+).
 * Converting to my own format, for simpler handling later.
 */
public class JavaScriptParser {
    private String script;
    private String name;

    public JavaScriptParser(String name, String script) {
        this.script = script;
        this.name = name;
    }

    public Program parse() {
        Options options = new Options("nashorn");
        options.set("anon.functions", true);
        options.set("parse.only", true);
        options.set("scripting", true);

        ErrorManager errors = new ErrorManager();
        Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());
        Source source = Source.sourceFor(this.name, this.script);
        Parser parser = new Parser(context.getEnv(), source, errors);
        FunctionNode functionNode = parser.parse();

        Program program = convert(functionNode);

        return program;
    }

    private Program convert(FunctionNode functionNode) {
        return new Program(functionNode.getLineNumber(), convert(functionNode.getBody()).getStatements());
    }

    private BlockStatement convert(jdk.nashorn.internal.ir.Block body) {
        List<Statement> statements = body.getStatements().stream().map(this::convert).collect(Collectors.toList());
        return new BlockStatement(body.getFirstStatementLineNumber(), statements);
    }

    private Statement convert(jdk.nashorn.internal.ir.Statement statement) {
        if (statement instanceof ReturnNode) {
            ReturnNode returnNode = (ReturnNode) statement;
            return convert(returnNode);
        } else if (statement instanceof jdk.nashorn.internal.ir.ExpressionStatement) {
            return new ExpressionStatement(statement.getLineNumber(), convert(((jdk.nashorn.internal.ir.ExpressionStatement) statement).getExpression()));
        } else if (statement instanceof VarNode) {
            VarNode var = (VarNode) statement;
            return new VariableNode(var.getLineNumber(), convertIdent(var.getName()), convert(var.getInit()));
        }
        else {
            throw new RuntimeException("Cannot yet handle statements of :" + statement.getClass());
        }
    }

    private Identifier convertIdent(IdentNode ident) {
        return new Identifier(ident.getName());
    }

    private Return convert(ReturnNode returnNode) {
        return new Return(returnNode.getLineNumber(), convert(returnNode.getExpression()));
    }

    private Expression convert(jdk.nashorn.internal.ir.Expression expression) {
        if (expression instanceof BinaryNode) {
            BinaryNode binaryNode = (BinaryNode) expression;
            return new BinaryExpression(convert(binaryNode.lhs()), convert(binaryNode.rhs()), tokenTypeToOp(binaryNode.tokenType()));
        } else if (expression instanceof AccessNode) {
            AccessNode accessNode = (AccessNode) expression;
            return new MemberExpression(accessNode.getProperty(), convert(accessNode.getBase()));
        } else if (expression instanceof IdentNode) {
            IdentNode identNode = (IdentNode) expression; // TODO: Trace to declaration, Symbol?
            return new Identifier(identNode.getPropertyName());
        } else if (expression instanceof FunctionNode) {
            FunctionNode functionNode = (FunctionNode) expression;
            List<Identifier> arguments = functionNode.getParameters().stream().map(identNode -> new Identifier(identNode.getName())).collect(Collectors.toList());
            return new Function(functionNode.getName(), convert(functionNode.getBody()), arguments);
        } else if (expression instanceof LiteralNode) {
            LiteralNode literal = (LiteralNode) expression;
            if (literal instanceof LiteralNode.PrimitiveLiteralNode) {
                LiteralNode.PrimitiveLiteralNode primitive = (LiteralNode.PrimitiveLiteralNode) literal;
                return convertPrimitive(primitive);
            } else if (literal instanceof LiteralNode.ArrayLiteralNode) {
                LiteralNode.ArrayLiteralNode array = (LiteralNode.ArrayLiteralNode) literal;
                throw new RuntimeException("Cannot handle array litterals yet");
            }
        }
        throw new RuntimeException("Cannot yet handle expression: " + expression.getClass());
    }

    private Expression convertPrimitive(LiteralNode.PrimitiveLiteralNode primitive) {
        // Private classes, one of the reasons for the conversion.
        if (primitive.getClass().toString().contains("BooleanLiteralNode")) {
            return new BooleanLiteral(primitive.getBoolean());
        } else if (primitive.getClass().toString().contains("NumberLiteralNode")) {
            return new NumberLiteral(primitive.getNumber());
        } else if (primitive.getClass().toString().contains("StringLiteralNode")) {
            return new StringLiteral(primitive.getString());
        } else if (primitive.getClass().toString().contains("NullLiteralNode")) {
            return new NullLiteral();
        } else if (primitive.getClass().toString().contains("UndefinedLiteralNode")) {
            return new UndefinedLiteral();
        } else {
            throw new RuntimeException("This should never happen");
        }
    }

    private Operation tokenTypeToOp(TokenType tokenType) {
        switch (tokenType) {
            case ADD:
                return Operation.ADD;
            case ASSIGN:
                return Operation.ASSIGN;
            default:
                throw new RuntimeException("Cannot yet handle operation: " + tokenType.toString());
        }
    }
}
