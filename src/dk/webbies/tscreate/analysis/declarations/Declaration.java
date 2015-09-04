package dk.webbies.tscreate.analysis.declarations;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public interface Declaration {
    <T> T accept(DeclarationVisitor<T> visitor);
}
