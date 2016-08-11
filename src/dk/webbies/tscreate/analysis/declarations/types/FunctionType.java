package dk.webbies.tscreate.analysis.declarations.types;

import dk.webbies.tscreate.paser.AST.FunctionExpression;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class FunctionType extends DeclarationType {
    private Set<FunctionExpression> astNodes;
    private DeclarationType returnType;
    private List<Argument> arguments;
    public int minArgs = Integer.MAX_VALUE;

    public FunctionType(FunctionExpression astNode, DeclarationType returnType, List<Argument> arguments, Set<String> names) {
        super(names);
        if (astNode != null) {
            this.astNodes = new HashSet<>(Collections.singletonList(astNode));
        } else {
            this.astNodes = Collections.EMPTY_SET;
        }
        this.returnType = returnType;
        this.arguments = arguments;
    }

    public FunctionType(DeclarationType returnType, List<Argument> arguments, Set<String> names, List<FunctionExpression> astNodes) {
        super(names);
        this.astNodes = new HashSet<>(astNodes);
        this.returnType = returnType;
        this.arguments = arguments;
    }

    public Collection<FunctionExpression> getAstNodes() {
        return astNodes;
    }

    public DeclarationType getReturnType() {
        return returnType;
    }

    public void setReturnType(DeclarationType returnType) {
        this.returnType = returnType;
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
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
