package dk.webbies.tscreate.analysis.declarations.types;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public interface DeclarationType {
    <T> T accept(DeclarationTypeVisitor<T> visitor);
}
