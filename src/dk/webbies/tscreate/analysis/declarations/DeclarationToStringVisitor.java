package dk.webbies.tscreate.analysis.declarations;

import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationTypeVisitor;
import dk.webbies.tscreate.analysis.declarations.types.FunctionType;
import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;

import java.util.List;

/**
 * Created by Erik Krogh Kristensen on 04-09-2015.
 */
public class DeclarationToStringVisitor implements DeclarationVisitor<Void> {
    private StringBuilder builder = new StringBuilder();
    private int ident = 0;
    private TypeVisitor typeVisitor = new TypeVisitor();

    public String getResult() {
        return builder.toString();
    }

    private void writeln(String str) {
        ident();
        builder.append(str);
        builder.append("\n");
    }

    private void ident() {
        for (int i = 0; i < ident; i++) {
            builder.append("\t");
        }
    }

    private void write(String str) {
        builder.append(str);
    }

    @Override
    public Void visit(DeclarationBlock block) {
        if (ident == 0) {
            block.getDeclarations().forEach(dec -> dec.accept(this));
        } else {
            writeln("{");
            ident++;
            block.getDeclarations().forEach(dec -> dec.accept(this));
            ident--;
            writeln("}");
        }

        return null;
    }

    @Override
    public Void visit(VariableDeclaration declaration) {
        ident();
        write("var ");
        write(declaration.getName());
        write(": ");
        declaration.getType().accept(typeVisitor);
        write(";\n");
        return null;
    }

    private class TypeVisitor implements DeclarationTypeVisitor<Void> {
        @Override
        public Void visit(FunctionType functionType) {
            write("(");
            List<FunctionType.Argument> args = functionType.getArguments();
            for (int i = 0; i < args.size(); i++) {
                FunctionType.Argument arg = args.get(i);
                write(arg.getName());
                write(": ");
                arg.getType().accept(this);
                if (i != args.size() - 1) {
                    write(", ");
                }
            }
            write(") : ");
            functionType.getReturnType().accept(this);
            return null;
        }

        @Override
        public Void visit(PrimitiveDeclarationType primitive) {
            write(primitive.getPrettyString());
            return null;
        }
    }
}
