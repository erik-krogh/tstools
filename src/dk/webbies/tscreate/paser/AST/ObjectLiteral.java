package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class ObjectLiteral extends Expression {
    private Map<String, Expression> properties;
    ObjectLiteral(SourceRange location, Map<String, Expression> properties) {
        super(location);
        this.properties = properties;
    }

    public Map<String, Expression> getProperties() {
        return properties;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
