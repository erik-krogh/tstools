package dk.webbies.tscreate.analysis.declarations;

import dk.webbies.tscreate.Util;
import dk.webbies.tscreate.analysis.declarations.types.*;

import java.util.List;

/**
 * Created by Erik Krogh Kristensen on 04-09-2015.
 */
public class DeclarationToStringVisitor implements DeclarationVisitor<Void> {
    private StringBuilder builder = new StringBuilder();
    private int ident = 0;

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
        if (declaration.getType() instanceof FunctionType) {
            FunctionType type = (FunctionType) declaration.getType();
            ident();
            write("declare function " + declaration.getName() + "(");
            List<FunctionType.Argument> args = type.getArguments();
            for (int i = 0; i < args.size(); i++) {
                FunctionType.Argument arg = args.get(i);
                write(arg.getName());
                write(": ");
                arg.getType().accept(new TypeVisitor());
                if (i != args.size() - 1) {
                    write(", ");
                }
            }
            write("): ");
            type.getReturnType().accept(new TypeVisitor());

            write(";\n");
        } else {
            ident();
            write("declare var ");
            write(declaration.getName());
            write(": ");
            declaration.getType().accept(new TypeVisitor());
            write(";\n");
        }

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
            write(") => ");
            functionType.getReturnType().accept(this);
            return null;
        }

        @Override
        public Void visit(PrimitiveDeclarationType primitive) {
            write(primitive.getPrettyString());
            return null;
        }

        @Override
        public Void visit(ObjectType objectType) {
            List<VariableDeclaration> decs = Util.cast(VariableDeclaration.class, objectType.getBlock().getDeclarations());
            if (decs.size() == 0) {
                write("{}");
            } else {
                write("{ \n");
                ident++;

                for (int i = 0; i < decs.size(); i++) {
                    VariableDeclaration dec = decs.get(i);
                    ident();
                    write(dec.getName());
                    write(": ");
                    dec.getType().accept(this);
                    if (i != decs.size() - 1) {
                        write(",");
                    }
                    write("\n");
                }
                ident--;
                ident();
                write("}");
            }
            return null;
        }

        @Override
        public Void visit(InterfaceType interfaceType) {
            write("I really dont know how to do interfaces yet");
            if (interfaceType.getFunction() != null) {
                interfaceType.getFunction().accept(this);
            }
            if (interfaceType.getObject() != null) {
                interfaceType.getObject().accept(this);
            }
            return null;
        }
    }
}
