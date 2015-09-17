package dk.webbies.tscreate.analysis.declarations;

import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class VariableDeclaration implements Declaration {
    private String name;
    private DeclarationType type;

    public VariableDeclaration(String name, DeclarationType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public DeclarationType getType() {
        return type;
    }

    @Override
    public <T> T accept(DeclarationVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public void setType(DeclarationType type) {
        this.type = type;
    }
}
