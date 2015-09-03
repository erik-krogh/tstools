package dk.webbies.tscreate.analysis.typeDeclaration;

import java.util.List;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class DeclarationBlock {
    private List<Declaration> declarations;

    public DeclarationBlock(List<Declaration> declarations) {
        this.declarations = declarations;
    }
}
