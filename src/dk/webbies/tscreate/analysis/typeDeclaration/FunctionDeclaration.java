package dk.webbies.tscreate.analysis.typeDeclaration;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class FunctionDeclaration implements Declaration {
    private final String name;
    private final FunctionType type;

    public FunctionDeclaration(String name, FunctionType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public FunctionType getType() {
        return type;
    }
}
