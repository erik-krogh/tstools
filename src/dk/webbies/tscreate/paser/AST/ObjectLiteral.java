package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

import java.util.List;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class ObjectLiteral extends Expression {
    private List<Property> properties;
    ObjectLiteral(SourceRange location, List<Property> properties) {
        super(location);
        this.properties = properties;
    }

    public List<Property> getProperties() {
        return properties;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public static final class Property {
        public final String name;
        public final Expression expression;

        public Property(String name, Expression expression) {
            this.name = name;
            this.expression = expression;
        }
    }
}
