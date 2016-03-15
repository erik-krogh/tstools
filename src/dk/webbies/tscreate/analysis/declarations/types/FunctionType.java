package dk.webbies.tscreate.analysis.declarations.types;

import java.util.List;
import java.util.Set;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class FunctionType extends DeclarationType {
    private DeclarationType returnType;
    private List<Argument> arguments;
    public int minArgs = Integer.MAX_VALUE;

    public FunctionType(DeclarationType returnType, List<Argument> arguments, Set<String> names) {
        super(names);
        this.returnType = returnType;
        this.arguments = arguments;
    }

    public DeclarationType getReturnType() {
        return returnType;
    }

    public void setReturnType(DeclarationType returnType) {
        this.returnType = returnType;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <T, A> T accept(DeclarationTypeVisitorWithArgument<T, A> visitor, A argument) {
        return visitor.visit(this, argument);
    }

    public static class Argument {
        private String name;
        private DeclarationType type;

        public Argument(String name, DeclarationType type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public DeclarationType getType() {
            return type;
        }

        public void setType(DeclarationType type) {
            this.type = type;
        }
    }
}
