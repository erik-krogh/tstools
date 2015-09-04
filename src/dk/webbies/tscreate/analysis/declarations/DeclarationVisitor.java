package dk.webbies.tscreate.analysis.declarations;

/**
 * Created by Erik Krogh Kristensen on 04-09-2015.
 */
public interface DeclarationVisitor<T> {
    T visit(DeclarationBlock block);

    T visit(VariableDeclaration declaration);
}
