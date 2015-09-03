package dk.webbies.tscreate.analysis.typeDeclaration;

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
}
