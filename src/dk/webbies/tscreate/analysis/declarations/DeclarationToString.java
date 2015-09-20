package dk.webbies.tscreate.analysis.declarations;

import dk.webbies.tscreate.Util;
import dk.webbies.tscreate.analysis.declarations.types.UnionDeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 04-09-2015.
 */
public class DeclarationToString {
    private OutputStream out;
    private int ident = 0;

    private List<InterfaceType> interfacesToPrint = new ArrayList<>();
    private List<ClassType> classesToPrint = new ArrayList<>();
    private boolean finishing = false;

    public DeclarationToString(OutputStream out) {
        this.out = out;
    }

    private void writeln(String str) {
        ident();
        write(str);
        write("\n");
    }

    private void ident() {
        for (int i = 0; i < ident; i++) {
            write("\t");
        }
    }

    private void write(String str) {
        try {
            out.write(str.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<InterfaceType> printedInterfaces = new HashSet<>();
    private Set<ClassType> printedClasses = new HashSet<>();

    private void finish() {
        finishing = true;

        while (classesToPrint.size() > 0) {
            ArrayList<ClassType> copy = new ArrayList<>(classesToPrint);
            classesToPrint.clear();
            for (ClassType classType : copy) {
                if (!printedClasses.contains(classType)) {
                    printedClasses.add(classType);
                    classType.accept(new TypeVisitor());
                }
            }
        }


        while (interfacesToPrint.size() > 0) {
            ArrayList<InterfaceType> copy = new ArrayList<>(interfacesToPrint);
            interfacesToPrint.clear();
            for (InterfaceType type : copy) {
                if (!printedInterfaces.contains(type)) {
                    printedInterfaces.add(type);
                    type.accept(new TypeVisitor());
                }
            }
        }
    }

    public void print(Map<String, DeclarationType> declarations) {
        if (ident == 0) {
            declarations.forEach(this::printDeclaration);
            finish();
        } else {
            writeln("{");
            ident++;
            declarations.forEach(this::printDeclaration);
            ident--;
            writeln("}");
        }
    }

    private void printDeclaration(String name, DeclarationType type) {
        if (type instanceof FunctionType) {
            FunctionType functionType = (FunctionType) type;
            ident();
            write("declare function " + name + "(");
            List<FunctionType.Argument> args = functionType.getArguments();
            printArguments(args);
            write("): ");
            functionType.getReturnType().accept(new TypeVisitor());

            write(";\n");
        } else {
            ident();
            write("declare var ");
            write(name);
            write(": ");
            type.accept(new TypeVisitor());
            write(";\n");
        }
    }

    private void printArguments(List<FunctionType.Argument> args) {
        for (int i = 0; i < args.size(); i++) {
            FunctionType.Argument arg = args.get(i);
            write(arg.getName());
            write(": ");
            arg.getType().accept(new TypeVisitor());
            if (i != args.size() - 1) {
                write(", ");
            }
        }
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
        public Void visit(UnnamedObjectType objectType) {
            Map<String, DeclarationType> decs = objectType.getDeclarations();
            if (decs.size() == 0) {
                write("{}");
            } else {
                write("{ \n");
                ident++;

                ArrayList<Map.Entry<String, DeclarationType>> declarationList = new ArrayList<>(decs.entrySet());
                for (int i = 0; i < declarationList.size(); i++) {
                    Map.Entry<String, DeclarationType> dec = declarationList.get(i);
                    ident();
                    write(dec.getKey());
                    write(": ");
                    dec.getValue().accept(this);
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
            if (finishing) {
                write("\n");
                finishing = false;
                writeln("interface " + interfaceType.name + " {");
                ident++;
                // TODO: Just wrong.
                if (interfaceType.getFunction() != null) {
                    interfaceType.getFunction().accept(this);
                }
                if (interfaceType.getObject() != null) {
                    interfaceType.getObject().accept(this);
                }
                ident--;
                writeln("}");
                finishing = true;
            } else {
                write(interfaceType.name);
                interfacesToPrint.add(interfaceType);
            }

            return null;
        }

        @Override
        public Void visit(UnionDeclarationType union) {
            ArrayList<DeclarationType> types = union.getTypes();
            for (int i = 0; i < types.size(); i++) {
                DeclarationType type = types.get(i);
                type.accept(this);
                if (i != types.size() - 1) {
                    write(" | ");
                }
            }
            return null;
        }

        @Override
        public Void visit(NamedObjectType namedObjectType) {
            write(namedObjectType.getName());
            return null;
        }

        @Override
        public Void visit(ClassType classType) {
            if (finishing) {
                // First an constructor interface.
                writeln("interface " + classType.getName() + "Constructor {");
                ident++;
                ident();
                write("new (");
                printArguments(classType.getConstructorType().getArguments());
                write(") : " + classType.getName() + "\n");
                ident--;
                writeln("}");
                write("\n");

                ident();
                write("interface " + classType.getName());
                if (classType.getSuperClass() != null) {
                    write(" extends ");
                    write(classType.getSuperClass().getName());
                    classesToPrint.add(classType.getSuperClass());
                }
                write(" {\n");


                ident++;
                classType.getProperties().forEach((name, type) -> {
                    ident();
                    write(name + ": ");
                    type.accept(this);
                    write("\n");
                });

                ident--;
                writeln("}");
            } else {
                write(classType.getName() + "Constructor");
                classesToPrint.add(classType);
            }

            return null;
        }

        @Override
        public Void visit(ClassInstanceType instanceType) {
            write(instanceType.getClazz().getName());
            classesToPrint.add(instanceType.getClazz());
            return null;
        }
    }
}
