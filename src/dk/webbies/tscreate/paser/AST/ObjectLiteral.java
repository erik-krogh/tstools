package dk.webbies.tscreate.paser.AST;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;
import dk.webbies.tscreate.paser.ExpressionVisitor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class ObjectLiteral extends Expression {
    private LinkedHashMap<String, Expression> properties;
    ObjectLiteral(SourceRange location, LinkedHashMap<String, Expression> properties) {
        super(location);
        this.properties = properties;
    }

    public LinkedHashMap<String, Expression> getProperties() {
        return properties;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
    @Override
    public <T> T accept(CFGExpressionVisitor<T> visitor, T aux) {
        return visitor.visit(this, aux);
    }

}
