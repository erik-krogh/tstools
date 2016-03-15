package dk.webbies.tscreate.analysis.declarations.types;

import java.util.Map;
import java.util.Set;

/**
 * Created by Erik Krogh Kristensen on 06-09-2015.
 */
public class UnnamedObjectType extends ObjectType {
    private Map<String, DeclarationType> declarations;

    public UnnamedObjectType(Map<String, DeclarationType> declarations, Set<String> names) {
        super(names);
        this.declarations = declarations;
    }

    public Map<String, DeclarationType> getDeclarations() {
        return declarations;
    }

    public void setDeclarations(Map<String, DeclarationType> declarations) {
        this.declarations = declarations;
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <T, A> T accept(DeclarationTypeVisitorWithArgument<T, A> visitor, A argument) {
        return visitor.visit(this, argument);
    }
}
