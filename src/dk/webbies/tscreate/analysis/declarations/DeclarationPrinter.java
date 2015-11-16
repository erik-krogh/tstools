package dk.webbies.tscreate.analysis.declarations;

import dk.webbies.tscreate.analysis.declarations.types.UnionDeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by Erik Krogh Kristensen on 04-09-2015.
 */
public class DeclarationPrinter {
    private final Map<DeclarationType, Integer> countMap;
    private final OutputStream out;
    private final Map<String, DeclarationType> declarations;

    private final List<InterfaceType> interfacesToPrint = new ArrayList<>();
    private final List<ClassType> classesToPrint = new ArrayList<>();
    private boolean finishing = false;
    private int ident = 0;

    // This is the functions or objects that are somehow recursive (or shows up in multiple locations).
    // That we therefore print as an interface instead.
    private final Map<DeclarationType, InterfaceType> printsAsInterface = new HashMap<>();

    public DeclarationPrinter(OutputStream out, Map<String, DeclarationType> declarations) {
        this.out = out;
        this.declarations = declarations;

        DeclarationTypeUseCounter useCounter = new DeclarationTypeUseCounter();
        for (DeclarationType type : declarations.values()) {
            type.accept(useCounter);
        }

        // This countmap mostly exists so that every type is visisted, and thus resolved (as in, resolving CombinationTypes and UnresolvedDeclarationsTypes).
        this.countMap = useCounter.getCountMap();
        this.countMap.forEach((type, count) -> {
            if (count > 1 && type instanceof FunctionType) {
                InterfaceType interfaceType = new InterfaceType("function_" + InterfaceType.counter++);
                interfaceType.function = (FunctionType) type;
                printsAsInterface.put(type, interfaceType);
            }
            if (type instanceof UnnamedObjectType) {
                InterfaceType interfaceType = new InterfaceType();
                interfaceType.object = (UnnamedObjectType) type;
                printsAsInterface.put(type, interfaceType);
            }
        });
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

    private void writeName(String str) {
        if (str.matches("[a-zA-Z_$][0-9a-zA-Z_$]*")) {
            write(str);
        } else {
            write("\"" + str + "\"");
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

        if (interfacesToPrint.size() > 0 || classesToPrint.size() > 0) {
            finish();
        }
    }

    public void print() {
        if (ident != 0) {
            throw new RuntimeException("Can only print top-level declarations with this method");
        }
        this.declarations.forEach(this::printDeclaration);
        finish();
    }

    private void printDeclaration(String name, DeclarationType type) {
        printDeclaration(name, type, "declare");
    }

    private void printDeclaration(String name, DeclarationType type, String prefix) {
        if (type instanceof FunctionType) {
            FunctionType functionType = (FunctionType) type;
            ident();
            write(prefix + " function " + name + "(");
            List<FunctionType.Argument> args = functionType.getArguments();
            printArguments(args);
            write("): ");
            functionType.getReturnType().accept(new TypeVisitor());

            write(";\n");
        } else if (type instanceof UnnamedObjectType) {
            UnnamedObjectType module = (UnnamedObjectType) type;
            ident();
            write(prefix + " module " + name + " {\n");
            ident++;

            for (Map.Entry<String, DeclarationType> entry : module.getDeclarations().entrySet()) {
                printDeclaration(entry.getKey(), entry.getValue(), "export");
            }

            ident--;
            writeln("}");
        } else {
            ident();
            write(prefix + " var ");
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
            printFunction(functionType, false);
            return null;
        }

        private void printFunction(FunctionType functionType, boolean insideInterface) {
            if (!insideInterface && printsAsInterface.containsKey(functionType)) {
                printsAsInterface.get(functionType).accept(this);
            } else {
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
                if (insideInterface) {
                    write(") : ");
                } else {
                    write(") => ");
                }
                functionType.getReturnType().accept(this);
            }
        }

        @Override
        public Void visit(PrimitiveDeclarationType primitive) {
            write(primitive.getPrettyString());
            return null;
        }

        @Override
        public Void visit(UnnamedObjectType objectType) {
            if (printsAsInterface.containsKey(objectType)) {
                return printsAsInterface.get(objectType).accept(this);
            } else {
                InterfaceType interfaceType = new InterfaceType();
                interfaceType.object = objectType;
                printsAsInterface.put(objectType, interfaceType);
                return interfaceType.accept(this);
            }
        }

        @Override
        public Void visit(InterfaceType interfaceType) {
            if (finishing) {
                finishing = false;
                writeln("interface " + interfaceType.name + " {");
                ident++;
                if (interfaceType.getFunction() != null) {
                    ident();
                    printFunction(interfaceType.getFunction(), true);
                    write(";\n");
                }
                // [s: string]: PropertyDescriptor;
                if (interfaceType.getDynamicAccess() != null) {
                    ident();
                    write("[");
                    if (interfaceType.getDynamicAccess().getLookupType().resolve() == PrimitiveDeclarationType.NUMBER) {
                        write("index: number");
                    } else {
                        write("s: string");
                    }
                    write("]: ");
                    interfaceType.getDynamicAccess().getReturnType().accept(this);
                    write(";\n");

                }
                if (interfaceType.getObject() != null) {
                    interfaceType.getObject().getDeclarations().forEach(this::printObjectField);
                }
                ident--;
                writeln("}");
                write("\n");
                finishing = true;
            } else {
                write(interfaceType.name);
                interfacesToPrint.add(interfaceType);
            }

            return null;
        }

        private void printObjectField(String name, DeclarationType type) {
            ident();
            writeName(name);
            write(": ");
            type.accept(this);
            write(";\n");
        }

        @Override
        public Void visit(UnionDeclarationType union) {
            List<DeclarationType> types = union.getTypes();
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
            switch (namedObjectType.getName()) {
                case "Array":
                    write("Array<any>");
                    break;
                case "NodeListOf":
                    write("NodeListOf<any>");
                    break;
                default:
                    write(namedObjectType.getName());
                    break;
            }
            return null;
        }

        @Override
        public Void visit(ClassType classType) {
            if (finishing) {
                finishing = false;
                // First an constructor interface.
                writeln("interface " + classType.getName() + "Constructor {");
                ident++;
                ident();
                write("new (");
                printArguments(classType.getConstructorType().getArguments());
                write(") : " + classType.getName() + "\n");

                classType.getStaticFields().forEach(this::printObjectField);

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
                classType.getPrototypeFields().entrySet().stream().filter(this.filterSuperclassFields(classType.getSuperClass())).forEach((entry) -> {
                    this.printObjectField(entry.getKey(), entry.getValue());
                });

                ident--;
                writeln("}");
                write("\n");
                finishing = true;
            } else {
                write(classType.getName() + "Constructor");
                classesToPrint.add(classType);
            }

            return null;
        }

        private Predicate<Map.Entry<String, DeclarationType>> filterSuperclassFields(ClassType superClass) {
            Set<String> fieldsInSuper = new HashSet<>();
            while (superClass != null) {
                fieldsInSuper.addAll(superClass.getPrototypeFields().keySet());
                superClass = superClass.getSuperClass();
            }
            return (entry) -> !fieldsInSuper.contains(entry.getKey());
        }

        @Override
        public Void visit(ClassInstanceType instanceType) {
            write(instanceType.getClazz().getName());
            classesToPrint.add(instanceType.getClazz());
            return null;
        }
    }
}
