package dk.webbies.tscreate.analysis.declarations.types;

import dk.webbies.tscreate.analysis.declarations.DeclarationBlock;

/**
 * Created by Erik Krogh Kristensen on 06-09-2015.
 */
public class ObjectType implements DeclarationType {
    private DeclarationBlock block;

    public ObjectType(DeclarationBlock declarationBlock) {
        this.block = declarationBlock;
    }

    public DeclarationBlock getBlock() {
        return block;
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}