package dk.webbies.tscreate.paser;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;

/**
 * Created by erik1 on 01-09-2015.
 */
public class NumberLiteral extends Expression {
    private double number;

    public NumberLiteral(SourceRange location, double number) {
        super(location);
        this.number = number;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
