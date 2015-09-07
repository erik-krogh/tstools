package dk.webbies.tscreate.analysis.declarations;

import java.util.List;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class DeclarationBlock implements Declaration {
    private List<Declaration> declarations;

    public DeclarationBlock(List<Declaration> declarations) {
        this.declarations = declarations;
    }

    public List<Declaration> getDeclarations() {
        return declarations;
    }

    @Override
    public <T> T accept(DeclarationVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
