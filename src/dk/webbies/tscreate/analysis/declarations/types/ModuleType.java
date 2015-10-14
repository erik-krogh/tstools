package dk.webbies.tscreate.analysis.declarations.types;

import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 14-10-2015.
 */
public class ModuleType implements DeclarationType {
    private final Map<String, DeclarationType> declarations;

    public ModuleType(Map<String, DeclarationType> declarations) {
        this.declarations = declarations;
    }

    public Map<String, DeclarationType> getDeclarations() {
        return declarations;
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
