package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.trees.Comment;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 01-09-2015.
 */
public class FunctionExpression extends Expression {
    public Identifier name;
    final BlockStatement body;
    final List<Identifier> arguments;
    public Map<String, Identifier> declarations;

    public Comment jsDoc;
    public Map<String, Comment> memberJsDocs = new HashMap<>();

    public FunctionExpression(SourceRange location, Identifier name, BlockStatement body, List<Identifier> arguments) {
        super(location);
        this.name = name;
        this.body = body;
        this.arguments = arguments;
    }

    public BlockStatement getBody() {
        return body;
    }

    public List<Identifier> getArguments() {
        return arguments;
    }

    public Identifier getName() {
        return name;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
