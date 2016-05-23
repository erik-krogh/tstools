package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

import java.util.Arrays;

/**
 * Created by erik1 on 23-05-2016.
 */
public class RegExpExpression extends Expression {
    private final String value;
    private final NewExpression newExp;

    public RegExpExpression(SourceRange loc, String value) {
        super(loc);
        this.value = value;

        String regExp = value.substring(1, value.length() - 1);
        this.newExp = new NewExpression(location, new Identifier(location, "RegExp"), Arrays.asList(new StringLiteral(location, regExp)));
    }

    public String getValue() {
        return value;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public NewExpression toNewExpression() {
        return this.newExp;
    }
}
