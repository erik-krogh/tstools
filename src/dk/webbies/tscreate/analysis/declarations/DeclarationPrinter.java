package dk.webbies.tscreate.analysis.declarations;

import dk.webbies.tscreate.analysis.declarations.types.UnionDeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.jsnap.Snap;
import fj.F;
import fj.pre.Ord;
import fj.pre.Ordering;

import java.util.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Created by Erik Krogh Kristensen on 04-09-2015.
 */
// FIXME: Make it so that classes can be printed as classes, and to get the constructor type, typeof "blab" is then used.
public class DeclarationPrinter {
    private final Map<String, DeclarationType> declarations;
    private Map<ClassType, String> classNames;
    private DeclarationParser.NativeClassesMap nativeClasses;

    private final List<InterfaceType> interfacesToPrint = new ArrayList<>();
    private final List<ClassType> classesToPrint = new ArrayList<>();
    private boolean finishing = false;
    private int ident = 0;

    // This is the functions or objects that are somehow recursive (or shows up in multiple locations).
    // That we therefore print as an interface instead.
    private final Map<DeclarationType, InterfaceType> printsAsInterface = new HashMap<>();

    public DeclarationPrinter(Map<String, DeclarationType> declarations, DeclarationParser.NativeClassesMap nativeClasses) {
        this.declarations = declarations;
        this.nativeClasses = nativeClasses;
    }

    private void addPrintAsInterface(DeclarationType type) {
        type = type.resolve();
        if (type instanceof FunctionType) {
            FunctionType func = (FunctionType) type;
            InterfaceType inter = new InterfaceType("function_" + InterfaceType.interfaceCounter++);
            inter.function = func;
            printsAsInterface.put(func, inter);
        } else if (type instanceof UnnamedObjectType) {
            UnnamedObjectType object = (UnnamedObjectType) type;
            InterfaceType inter = new InterfaceType();
            inter.object = object;
            printsAsInterface.put(object, inter);
        } else if (type instanceof DynamicAccessType) {
            DynamicAccessType dynamic = (DynamicAccessType) type;
            InterfaceType inter = new InterfaceType();
            inter.dynamicAccess = dynamic;
            printsAsInterface.put(dynamic, inter);
        } else {
            throw new RuntimeException();
        }
    }

    private void writeln(StringBuilder builder, String str) {
        ident(builder);
        write(builder, str);
        write(builder, "\n");
    }

    private void ident(StringBuilder builder) {
        for (int i = 0; i < ident; i++) {
            write(builder, "\t");
        }
    }

    private void write(StringBuilder builder, String str) {
        builder.append(str);
    }

    private static Set<String> keyWords = new HashSet<>(Arrays.asList("set get abstract arguments boolean break byte case catch char class const continue debugger default delete do double else enum eval export extends false final finally float for function goto if implements import in instanceof int interface let long native new null package private protected public return short static super switch synchronized this throw throws transient true try typeof var void volatile while with yield".split(" ")));
    private void writeName(StringBuilder builder, String str) {
        if (str.matches("[a-zA-Z_$][0-9a-zA-Z_$]*") && !keyWords.contains(str)) {
            write(builder, str);
        } else {
            write(builder, "\"" + str + "\"");
        }
    }

    private Set<InterfaceType> printedInterfaces = new HashSet<>();
    private Set<ClassType> printedClasses = new HashSet<>();

    private void finish(StringBuilder outerBuilder) {
        finishing = true;

        while (classesToPrint.size() > 0) {
            ArrayList<ClassType> copy = new ArrayList<>(classesToPrint);
            classesToPrint.clear();
            for (ClassType classType : copy) {
                printInterface(outerBuilder, classType, printedClasses);
            }
        }


        while (interfacesToPrint.size() > 0) {
            ArrayList<InterfaceType> copy = new ArrayList<>(interfacesToPrint);
            interfacesToPrint.clear();
            for (InterfaceType type : copy) {
                printInterface(outerBuilder, type, printedInterfaces);
            }
        }

        if (interfacesToPrint.size() > 0 || classesToPrint.size() > 0) {
            finish(outerBuilder);
        }
    }

    private <T extends DeclarationType> void printInterface(StringBuilder outerBuilder, T type, Set<T> printed) {
        if (!printed.contains(type)) {
            printed.add(type);
            StringBuilder builder;
            while (true) {
                try {
                    finishing = true;
                    type.accept(new TypeVisitor(), new VisitorArg(builder = new StringBuilder(), emptySet(), null));
                    break;
                } catch (GotCyclic e) {
                    ident = 0;
                    addPrintAsInterface(e.type);
                }
            }
            outerBuilder.append(builder);
        }
    }

    public String print() {
        if (ident != 0) {
            throw new RuntimeException("Can only print top-level declarations with this method");
        }
        StringBuilder builder;
        while (true) {
            builder = new StringBuilder();
            this.classNames = new ClassNameFinder(declarations, printsAsInterface).getClassNames();
            try {
                for (Map.Entry<String, DeclarationType> entry : this.declarations.entrySet()) {
                    String name = entry.getKey();
                    DeclarationType type = entry.getValue();
                    printDeclaration(new VisitorArg(builder, emptySet(), name), name, type);
                }
            } catch (GotCyclic e) {
                ident = 0;
                addPrintAsInterface(e.type);
                printedClasses.clear();
                continue;
            }
            break;
        }


        finish(builder);
        return builder.toString();
    }

    private void printDeclaration(VisitorArg arg, String name, DeclarationType type) {
        printDeclaration(arg, name, type, "declare");
    }

    @SuppressWarnings("Duplicates")
    private void printDeclaration(VisitorArg arg, String name, DeclarationType type, String prefix) {
        type = type.resolve();
        if ((type instanceof FunctionType || type instanceof UnnamedObjectType) && printsAsInterface.containsKey(type)) {
            type = printsAsInterface.get(type);
        } else {
            if (arg.contains(type)) {
                throw new GotCyclic(type);
            }
        }
        if (type instanceof FunctionType) {
            arg = arg.cons(type, true);
            FunctionType functionType = (FunctionType) type;
            ident(arg.builder);
            write(arg.builder, prefix + " function " + name + "(");
            List<FunctionType.Argument> args = functionType.getArguments();
            printArguments(arg, args);
            write(arg.builder, "): ");
            functionType.getReturnType().accept(new TypeVisitor(), arg);

            write(arg.builder, ";\n");
        } else if (type instanceof UnnamedObjectType) {
            arg = arg.cons(type, false);
            UnnamedObjectType module = (UnnamedObjectType) type;
            ident(arg.builder);
            write(arg.builder, prefix + " module ");
            writeName(arg.builder, name);
            write(arg.builder, " {\n");
            ident++;

            for (Map.Entry<String, DeclarationType> entry : module.getDeclarations().entrySet()) {
                printDeclaration(arg.addPath(entry.getKey()), entry.getKey(), entry.getValue(), "export");
            }

            ident--;
            writeln(arg.builder, "}");
        } else if (type instanceof ClassType && arg.path.equals(classNames.get(type))) {
            ClassType clazz = (ClassType) type;
            if (printedClasses.contains(clazz)) {
                throw new RuntimeException();
            }
            printedClasses.add(clazz);

            ident(arg.builder);
            write(arg.builder, prefix + " class " + name);
            if (clazz.getSuperClass() != null) {
                write(arg.builder, " extends ");
                if (clazz.getSuperClass() instanceof ClassType) {
                    ClassType superClass = (ClassType) clazz.getSuperClass();
                    if (classNames.containsKey(superClass)) {
                        write(arg.builder, classNames.get(superClass));
                    } else {
                        write(arg.builder, superClass.getName());
                        classesToPrint.add(superClass);
                    }
                } else if (clazz.getSuperClass() instanceof NamedObjectType) {
                    write(arg.builder, ((NamedObjectType) clazz.getSuperClass()).getName());
                } else {
                    throw new RuntimeException();
                }
            }
            write(arg.builder, " {\n");
            ident++;
            ident(arg.builder);
            write(arg.builder, "constructor (");
            printArguments(arg, clazz.getConstructorType().getArguments());
            write(arg.builder, ");\n");

            Predicate<Map.Entry<String, DeclarationType>> notInSuperClassStatic = notStaticInSuperClassTest(clazz.getSuperClass());
            for (Map.Entry<String, DeclarationType> entry : clazz.getStaticFields().entrySet()) {
                if (notInSuperClassStatic.test(entry)) {
                    printObjectField(arg, entry.getKey(), entry.getValue(), new TypeVisitor(), "static");
                }
            }

            Predicate<Map.Entry<String, DeclarationType>> notInSuperClass = notInSuperClassTest(clazz.getSuperClass());
            for (Map.Entry<String, DeclarationType> entry : clazz.getPrototypeFields().entrySet()) {
                if (notInSuperClass.test(entry)) {
                    this.printObjectField(arg, entry.getKey(), entry.getValue(), new TypeVisitor());
                }
            }

            ident--;
            writeln(arg.builder, "}");
        } else {
            arg = arg.cons(type, false);
            ident(arg.builder);
            write(arg.builder, prefix + " var ");
            write(arg.builder, name);
            write(arg.builder, ": ");
            type.accept(new TypeVisitor(), arg);
            write(arg.builder, ";\n");
        }
    }

    private void printArguments(VisitorArg visitorArg, List<FunctionType.Argument> args) {
        for (int i = 0; i < args.size(); i++) {
            FunctionType.Argument arg = args.get(i);
            write(visitorArg.builder, arg.getName());
            write(visitorArg.builder, ": ");
            arg.getType().accept(new TypeVisitor(), visitorArg);
            if (i != args.size() - 1) {
                write(visitorArg.builder, ", ");
            }
        }
    }

    private static final class VisitorArg {
        final StringBuilder builder;
        final fj.data.Set<DeclarationType> seen;
        final String path;

        VisitorArg(StringBuilder builder, fj.data.Set<DeclarationType> seen, String path) {
            this.builder = builder;
            this.seen = seen;
            this.path = path;
        }

        VisitorArg cons(DeclarationType type, boolean nullPath) {
            String path = this.path;
            if (nullPath) {
                path = null;
            }
            return new VisitorArg(builder, seen.insert(type), path);
        }

        VisitorArg addPath(String propName) {
            if (this.path == null) {
                return this;
            }
            return new VisitorArg(builder, seen, this.path.isEmpty() ? propName : this.path + "." + propName);
        }

        boolean contains(DeclarationType type) {
            return seen.member(type);
        }
    }

    private static final class GotCyclic extends RuntimeException {
        final DeclarationType type;

        private GotCyclic(DeclarationType type) {
            this.type = type;
        }
    }

    private void printObjectField(VisitorArg arg, String name, DeclarationType type, DeclarationTypeVisitorWithArgument<Void, VisitorArg> visitor) {
        printObjectField(arg, name, type, visitor, null);
    }

    private void printObjectField(VisitorArg arg, String name, DeclarationType type, DeclarationTypeVisitorWithArgument<Void, VisitorArg> visitor, String prefix) {
        ident(arg.builder);
        if (prefix != null) {
            write(arg.builder, prefix);
            write(arg.builder, " ");
        }
        writeName(arg.builder, name);
        write(arg.builder, ": ");
        type.accept(visitor, arg);
        write(arg.builder, ";\n");
    }


    private class TypeVisitor implements DeclarationTypeVisitorWithArgument<Void, VisitorArg> {

        @Override
        public Void visit(FunctionType functionType, VisitorArg arg) {
            printFunction(arg, functionType, false);
            return null;
        }

        private void printFunction(VisitorArg arg, FunctionType functionType, boolean insideInterface) {
            if (!insideInterface && printsAsInterface.containsKey(functionType)) {
                printsAsInterface.get(functionType).accept(this, arg);
            } else {
                if (arg.contains(functionType)) {
                    throw new GotCyclic(functionType);
                }
                arg = arg.cons(functionType, true);

                write(arg.builder, "(");
                List<FunctionType.Argument> args = functionType.getArguments();
                for (int i = 0; i < args.size(); i++) {
                    FunctionType.Argument argument = args.get(i);
                    write(arg.builder, argument.getName());
                    write(arg.builder, ": ");
                    argument.getType().accept(this, arg);
                    if (i != args.size() - 1) {
                        write(arg.builder, ", ");
                    }
                }
                if (insideInterface) {
                    write(arg.builder, ") : ");
                } else {
                    write(arg.builder, ") => ");
                }
                functionType.getReturnType().accept(this, arg);
            }
        }

        @Override
        public Void visit(PrimitiveDeclarationType primitive, VisitorArg arg) {
            write(arg.builder, primitive.getPrettyString());
            return null;
        }

        @Override
        public Void visit(UnnamedObjectType objectType, VisitorArg arg) {
            if (printsAsInterface.containsKey(objectType)) {
                return printsAsInterface.get(objectType).accept(this, arg);
            } else {
                if (arg.contains(objectType)) {
                    throw new GotCyclic(objectType);
                }
                arg = arg.cons(objectType, false);

                StringBuilder builder = new StringBuilder();
                VisitorArg subArg = new VisitorArg(builder, arg.seen, arg.path);
                write(builder, "{");
                ArrayList<String> keys = new ArrayList<>(objectType.getDeclarations().keySet());
                for (int i = 0; i < keys.size(); i++) {
                    String name = keys.get(i);
                    writeName(builder, name);
                    write(builder, ": ");
                    DeclarationType type = objectType.getDeclarations().get(name);
                    type.accept(this, subArg.addPath(name));
                    if (i != keys.size() - 1) {
                        write(builder, ", ");
                    }
                }
                write(builder, "}");
                String declarationsString = builder.toString();
                if (declarationsString.contains("\n") || declarationsString.length() > 50) {
                    throw new GotCyclic(objectType);
                } else {
                    arg.builder.append(declarationsString);
                }
                return null;
            }
        }

        @Override
        public Void visit(InterfaceType interfaceType, VisitorArg arg) {
            if (finishing) {
                finishing = false;
                writeln(arg.builder, "interface " + interfaceType.name + " {");
                ident++;
                if (interfaceType.getFunction() != null) {
                    ident(arg.builder);
                    printFunction(arg, interfaceType.getFunction(), true);
                    write(arg.builder, ";\n");
                }
                // [s: string]: PropertyDescriptor;
                if (interfaceType.getDynamicAccess() != null) {
                    ident(arg.builder);
                    write(arg.builder, "[");
                    DeclarationType resolvedLookup = interfaceType.getDynamicAccess().getLookupType().resolve();
                    if (resolvedLookup instanceof PrimitiveDeclarationType && ((PrimitiveDeclarationType)resolvedLookup).getType() == PrimitiveDeclarationType.Type.NUMBER) {
                        write(arg.builder, "index: number");
                    } else {
                        write(arg.builder, "s: string");
                    }
                    write(arg.builder, "]: ");
                    interfaceType.getDynamicAccess().getReturnType().accept(this, arg);
                    write(arg.builder, ";\n");

                }
                if (interfaceType.getObject() != null) {
                    interfaceType.getObject().getDeclarations().forEach((name, type) -> printObjectField(arg, name, type, this));
                }
                ident--;
                writeln(arg.builder, "}");
                write(arg.builder, "\n");
                finishing = true;
            } else {
                write(arg.builder, interfaceType.name);
                interfacesToPrint.add(interfaceType);
            }

            return null;
        }

        @Override
        public Void visit(UnionDeclarationType union, VisitorArg arg) {
            List<DeclarationType> types = union.getTypes();
            for (int i = 0; i < types.size(); i++) {
                DeclarationType type = types.get(i);
                if (type.resolve() instanceof FunctionType) {
                    write(arg.builder, "(");
                    type.accept(this, arg);
                    write(arg.builder, ")");
                } else {
                    type.accept(this, arg);
                }
                if (i != types.size() - 1) {
                    write(arg.builder, " | ");
                }
            }
            return null;
        }

        @Override
        public Void visit(NamedObjectType namedObjectType, VisitorArg arg) {
            switch (namedObjectType.getName()) {
                case "Array":
                    write(arg.builder, "Array<any>");
                    break;
                case "NodeListOf":
                    write(arg.builder, "NodeListOf<any>");
                    break;
                default:
                    write(arg.builder, namedObjectType.getName());
                    break;
            }
            return null;
        }

        @Override
        public Void visit(ClassType classType, VisitorArg arg) {
            if (finishing) {
                finishing = false;
                // First an constructor interface.
                writeln(arg.builder, "interface " + classType.getName() + "Constructor {");
                ident++;
                ident(arg.builder);
                write(arg.builder, "new (");
                printArguments(arg, classType.getConstructorType().getArguments());
                write(arg.builder, ") : " + classType.getName() + "\n");

                classType.getStaticFields().forEach((name, type) -> printObjectField(arg, name, type, this));

                ident--;
                writeln(arg.builder, "}");
                write(arg.builder, "\n");

                ident(arg.builder);
                write(arg.builder, "interface " + classType.getName());
                if (classType.getSuperClass() != null) {
                    write(arg.builder, " extends ");
                    if (classType.getSuperClass() instanceof ClassType) {
                        ClassType superClass = (ClassType) classType.getSuperClass();
                        write(arg.builder, superClass.getName());
                        classesToPrint.add(superClass);
                    } else if (classType.getSuperClass() instanceof NamedObjectType) {
                        write(arg.builder, ((NamedObjectType) classType.getSuperClass()).getName());
                    } else {
                        throw new RuntimeException();
                    }
                }
                write(arg.builder, " {\n");


                ident++;
                classType.getPrototypeFields().entrySet().stream().filter(notInSuperClassTest(classType.getSuperClass())).forEach((entry) -> {
                    printObjectField(arg, entry.getKey(), entry.getValue(), this);
                });

                ident--;
                writeln(arg.builder, "}");
                write(arg.builder, "\n");
                finishing = true;
            } else {
                if (classNames.containsKey(classType)) {
                    write(arg.builder, "typeof " + classNames.get(classType));
                } else {
                    write(arg.builder, classType.getName() + "Constructor");
                    classesToPrint.add(classType);
                }
            }

            return null;
        }

        @Override
        public Void visit(ClassInstanceType instanceType, VisitorArg arg) {
            if (classNames.containsKey(instanceType.getClazz())) {
                write(arg.builder, classNames.get(instanceType.getClazz()));
            } else {
                write(arg.builder, instanceType.getClazz().getName());
                classesToPrint.add(instanceType.getClazz());
            }
            return null;
        }
    }

    private Predicate<Map.Entry<String, DeclarationType>> notInSuperClassTest(DeclarationType superClass) {
        Set<String> fieldsInSuper = new HashSet<>();
        while (superClass != null) {
            if (superClass instanceof ClassType) {
                fieldsInSuper.addAll(((ClassType)superClass).getPrototypeFields().keySet());
                superClass = ((ClassType)superClass).getSuperClass();
            } else if (superClass instanceof NamedObjectType) {
                Snap.Obj proto = nativeClasses.prototypeFromName(((NamedObjectType) superClass).getName()); // FIXME: GO after the type instead of the object.
                while (proto != null && proto != proto.prototype) {
                    fieldsInSuper.addAll(proto.getPropertyMap().keySet());
                    proto = proto.prototype;
                }
                break;
            } else {
                throw new RuntimeException();
            }
        }
        return (entry) -> !fieldsInSuper.contains(entry.getKey());
    }

    private Predicate<Map.Entry<String, DeclarationType>> notStaticInSuperClassTest(DeclarationType superClass) {
        Set<String> fieldsInSuper = new HashSet<>();
        while (superClass != null) {
            if (superClass instanceof ClassType) {
                fieldsInSuper.addAll(((ClassType)superClass).getStaticFields().keySet());
                superClass = ((ClassType)superClass).getSuperClass();
            } else if (superClass instanceof NamedObjectType) {
                throw new RuntimeException();
            } else {
                throw new RuntimeException();
            }
        }
        return (entry) -> !fieldsInSuper.contains(entry.getKey());
    }

    // Functional set things
    private static final Ord<DeclarationType> ordering = Ord.ord(new F<DeclarationType, F<DeclarationType, Ordering>>() {
        public F<DeclarationType, Ordering> f(DeclarationType one) {
            return two -> {
                int x = Integer.compare(one.counter, two.counter);
                return x < 0 ? Ordering.LT : (x == 0 ? Ordering.EQ : Ordering.GT);
            };
        }
    });

    static fj.data.Set<DeclarationType> emptySet() {
        return fj.data.Set.empty(ordering);
    }
}
