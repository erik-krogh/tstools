package dk.webbies.tscreate.analysis.declarations;

import com.google.javascript.jscomp.parsing.parser.trees.Comment;
import dk.au.cs.casa.typescript.types.GenericType;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.util.Tuple3;
import dk.webbies.tscreate.util.Util;
import fj.F;
import fj.pre.Ord;
import fj.pre.Ordering;

import java.util.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 04-09-2015.
 */
public class DeclarationPrinter {
    private final Map<String, DeclarationType> declarations;
    public Map<DeclarationType, String> declarationNames;
    private DeclarationParser.NativeClassesMap nativeClasses;

    private final List<InterfaceDeclarationType> interfacesToPrint = new ArrayList<>();
    private final List<ClassType> classesToPrint = new ArrayList<>();
    private boolean finishing = false;
    private int ident = 0;

    // This is the functions or objects that are somehow recursive (or shows up in multiple locations).
    // That we therefore print as an interface instead.
    private final Map<DeclarationType, InterfaceDeclarationType> printsAsInterface = new HashMap<>();
    private Options options;

    public DeclarationPrinter(Map<String, DeclarationType> declarations, DeclarationParser.NativeClassesMap nativeClasses, Options options) {
        this.declarations = declarations;
        this.nativeClasses = nativeClasses;
        this.options = options;
    }

    private void addPrintAsInterface(fj.data.List<DeclarationType> functionalTypesList, DeclarationType singleType) {
        DeclarationType type = null;

        if (functionalTypesList != null) {
            List<DeclarationType> types = new ArrayList<>();
            functionalTypesList.forEach(types::add);


            for (DeclarationType candidate : types) {
                if (candidate instanceof FunctionType || candidate instanceof UnnamedObjectType || candidate instanceof DynamicAccessType) {
                    type = candidate;
                    break;
                }
            }
            assert type != null;
        } else {
            type = singleType;
        }

        type = type.resolve();
        if (type instanceof FunctionType) {
            FunctionType func = (FunctionType) type;
            InterfaceDeclarationType inter = new InterfaceDeclarationType("function_" + InterfaceDeclarationType.interfaceCounter++, type.getNames());
            inter.setFunction(func);
            printsAsInterface.put(func, inter);
        } else if (type instanceof UnnamedObjectType) {
            UnnamedObjectType object = (UnnamedObjectType) type;
            InterfaceDeclarationType inter = new InterfaceDeclarationType(type.getNames());
            inter.setObject(object);
            printsAsInterface.put(object, inter);
        } else if (type instanceof DynamicAccessType) {
            DynamicAccessType dynamic = (DynamicAccessType) type;
            InterfaceDeclarationType inter = new InterfaceDeclarationType(type.getNames());
            inter.setDynamicAccess(dynamic);
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
        for (int i = 0; i < ident * 4; i++) {
            write(builder, " ");
        }
    }

    private void write(StringBuilder builder, String str) {
        builder.append(str);
    }

    private static Set<String> keyWords = new HashSet<>(Arrays.asList("abstract arguments boolean break byte case catch char class const continue debugger default delete do double else enum eval export extends false final finally float for function if implements import in instanceof int interface let long native new null package private protected public return short static super switch synchronized this throw throws transient true try typeof var void volatile while with yield var".split(" ")));
    private void writeName(StringBuilder builder, String str) {
        if (validName(str)) {
            write(builder, str);
        } else {
            write(builder, "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"") + "\"");
        }
    }

    private static final Predicate<String> validNameRegexp = Pattern.compile("[a-zA-Z_$][0-9a-zA-Z_$]*").asPredicate();
    private boolean validName(String str) {
        return validNameRegexp.test(str) && !keyWords.contains(str);
    }

    private Set<InterfaceDeclarationType> printedInterfaces = new HashSet<>();
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
            ArrayList<InterfaceDeclarationType> copy = new ArrayList<>(interfacesToPrint);
            interfacesToPrint.clear();
            for (InterfaceDeclarationType type : copy) {
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
                    type.accept(new TypeVisitor(), new VisitorArg(builder = new StringBuilder(), fj.data.List.nil(), null));
                    break;
                } catch (GotCyclic e) {
                    ident = 0;
                    addPrintAsInterface(e.types, e.singleType);
                }
            }
            outerBuilder.append(builder);
        }
    }

    public String print() {
        System.out.println("Printing declarations");

        if (ident != 0) {
            throw new RuntimeException("Can only print top-level declarations with this method");
        }
        ident = 0;
        return innerPrint();
    }

    private String innerPrint() {
        StringBuilder builder;
        while (true) {
            builder = new StringBuilder();
            this.declarationNames = new ClassNameFinder(declarations, printsAsInterface).getDeclarationNames();
            if (options.neverPrintModules) {
                this.declarationNames.clear();
            }
            try {
                for (Map.Entry<String, DeclarationType> entry : this.declarations.entrySet()) {
                    DeclarationType type = entry.getValue();
                    String name = entry.getKey();
                    if (!validName(name)) {
                        System.err.println("Skipping printing global declaration: \"" + name + "\", because I couldn't print it properly.");
                        continue;
                    }
                    printDeclaration(new VisitorArg(builder, fj.data.List.nil(), name), name, type);
                }
            } catch (GotCyclic e) {
                ident = 0;
                addPrintAsInterface(e.types, e.singleType);
                this.printedClasses.clear();
                this.printedInterfaces.clear();
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

    private void printDeclaration(VisitorArg arg, String name, DeclarationType type, String prefix) {
        type = type.resolve();
        if ((type instanceof FunctionType || type instanceof UnnamedObjectType) && printsAsInterface.containsKey(type)) {
            type = printsAsInterface.get(type);
        }
        if (exportNameBlacklist.contains(name) || !validName(name)) {
            throw new GotCyclic(arg.getSeen().cons(type));
        }

        if (type instanceof FunctionType && !this.declarationNames.containsKey(type)) {
            throw new RuntimeException();
        }

        StringBuilder builder = arg.builder;
        if (type instanceof FunctionType && arg.path.equals(declarationNames.get(type))) {
            if (options.neverPrintModules) {
                throw new GotCyclic(type);
            }
            arg = arg.cons(type, true);
            FunctionType functionType = (FunctionType) type;
            ident(builder);
            write(builder, prefix + " function " + name + "(");
            List<FunctionType.Argument> args = functionType.getArguments();
            printArguments(arg, args, functionType.minArgs);
            write(builder, "): ");
            functionType.getReturnType().accept(new TypeVisitor(), arg);

            write(builder, ";\n");
        } else if (type instanceof UnnamedObjectType && arg.path.equals(declarationNames.get(type))) {
            if (options.neverPrintModules) {
                throw new GotCyclic(type);
            }
            arg = arg.cons(type, false);
            UnnamedObjectType module = (UnnamedObjectType) type;
            ident(builder);
            write(builder, prefix + " module ");
            writeName(builder, name);
            write(builder, " {\n");
            ident++;

            for (Map.Entry<String, DeclarationType> entry : module.getDeclarations().entrySet().stream().sorted(Util::compareStringEntry).collect(Collectors.toList())) {
                printDeclaration(arg.addPath(entry.getKey()), entry.getKey(), entry.getValue(), "export");
            }

            ident--;
            writeln(builder, "}");
        } else if (type instanceof ClassType && arg.path.equals(declarationNames.get(type))) {
            if (options.neverPrintModules) {
                throw new GotCyclic(type);
            }
            ClassType clazz = (ClassType) type;
            if (printedClasses.contains(clazz)) {
                throw new RuntimeException();
            }
            printedClasses.add(clazz);

            printClass(arg, name, prefix, builder, clazz);
        } else {
            arg = arg.cons(type, false);
            ident(builder);
            write(builder, prefix + " var ");
            write(builder, name);
            write(builder, ": ");
            type.accept(new TypeVisitor(), arg);
            write(builder, ";\n");
        }
    }

    private void printClass(VisitorArg arg, String name, String prefix, StringBuilder builder, ClassType clazz) {
        ident(builder);
        if (prefix.equals("")) {
            write(builder, "class " + name);
        } else {
            write(builder, prefix + " class " + name);
        }
        if (clazz.getSuperClass() != null) {
            printExtends(clazz, arg);
        }
        write(builder, " {\n");
        ident++;

        if (!clazz.getConstructorType().getAstNodes().isEmpty()) {
            FunctionExpression func = clazz.getConstructorType().getAstNodes().iterator().next();
            if (func.jsDoc != null) {
                printJSDoc(func.jsDoc.value, arg);
            }
        }
        ident(builder);
        write(builder, "constructor (");
        printArguments(arg, clazz.getConstructorType().getArguments(), clazz.getConstructorType().minArgs);
        write(builder, ");\n");

        Predicate<String> notStaticInSuperClass = notStaticInSuperClassTest(clazz.getSuperClass());
        for (Map.Entry<String, DeclarationType> entry : clazz.getStaticFields().entrySet().stream().sorted(Util::compareStringEntry).collect(Collectors.toList())) {
            if (notStaticInSuperClass.test(entry.getKey()) && !entry.getKey().toLowerCase().equals("constructor")) {
                printObjectField(arg, entry.getKey(), entry.getValue(), new TypeVisitor(), "static");
            }
        }

        Map<String, String> memberDocs = new HashMap<>();
        if (clazz.getConstructorType().getAstNodes().size() == 1) {
            FunctionExpression func = clazz.getConstructorType().getAstNodes().iterator().next();
            func.memberJsDocs.entrySet().forEach(entry -> memberDocs.put(entry.getKey(), entry.getValue().value));
        }

        Predicate<String> notInSuperClass = notInSuperClassTest(clazz.getSuperClass());
        for (Map.Entry<String, DeclarationType> entry : clazz.getPrototypeFields().entrySet().stream().sorted(Util::compareStringEntry).collect(Collectors.toList())) {
            if (notInSuperClass.test(entry.getKey())) {
                String memberDoc = memberDocs.get(entry.getKey());
                if (memberDoc != null) {
                    printObjectField(arg, entry.getKey(), entry.getValue(), new TypeVisitor(), null, memberDoc);
                } else {
                    printObjectField(arg, entry.getKey(), entry.getValue(), new TypeVisitor());
                }
            }
        }

        ident--;
        writeln(builder, "}");
    }

    public String printType(DeclarationType type, String typePath) {
        return printType(type, 0, typePath);
    }

    Map<Tuple3<DeclarationType, Integer, String>, String> printedTypeCache = new HashMap<>();
    public String printType(DeclarationType type, int indentationLevel, String typePath) {
        try {
            Tuple3<DeclarationType, Integer, String> cacheKey = new Tuple3<>(type, indentationLevel, typePath);
            if (printedTypeCache.containsKey(cacheKey)) {
                return printedTypeCache.get(cacheKey);
            }
            if (typePath != null && !typePath.isEmpty()) {
                assert typePath.startsWith("window.");
                typePath = Util.removePrefix(typePath, "window.");
            }

            type = type.resolve();
            ident += indentationLevel;
            finishing = false;
            StringBuilder builder = new StringBuilder();

            printedClasses.clear();
            printedInterfaces.clear();

            VisitorArg arg = new VisitorArg(builder, fj.data.List.nil(), "");
            if (type instanceof ClassType) {
                ClassType clazz = (ClassType) type;
                // TODO: If typeof expression, then print it as such.
                String classPath = declarationNames.get(clazz);
                if (typePath == null || (classPath != null && classPath.startsWith(typePath))) {
                    printClass(arg, clazz.getName(), "", builder, clazz);
                } else {
                    type.accept(new TypeVisitor(), arg);
                }
            } else if (printsAsInterface.containsKey(type)) {
                InterfaceDeclarationType interfaceType = printsAsInterface.get(type);
                printInterface(builder, interfaceType, new HashSet<>());
            } else if (type instanceof FunctionType) {
                printFunction(new TypeVisitor(), arg, (FunctionType) type, false);
            } else if (type instanceof UnnamedObjectType) {
                if (declarationNames.containsKey(type)) {
                    String path = declarationNames.get(type);
                    printDeclaration(new VisitorArg(builder, arg.seen, path), lastPart(path), type);
                } else {
                    printObjectTypeBig(new TypeVisitor(), arg, (UnnamedObjectType) type);
                }
            } else if (type instanceof InterfaceDeclarationType) {
                printInterface(builder, type, new HashSet<>());
            } else {
                type.accept(new TypeVisitor(), arg);
            }

            ident -= indentationLevel;

            String result = builder.toString();
            printedTypeCache.put(cacheKey, result);
            return result;
        } catch (GotCyclic e) {
            return "any"; // Happens in some corner-cases with array-indexers. Where the printer just prints "any", but the type is actually something complicated.
        }
    }

    private String lastPart(String typePath) {
        if (typePath.indexOf('.') == -1) {
            return typePath;
        }
        return typePath.substring(typePath.lastIndexOf('.') + 1, typePath.length());
    }

    private static final Set<String> exportNameBlacklist = new HashSet<>(Arrays.asList("function", "delete", "var", "with"));

    private void printArguments(VisitorArg visitorArg, List<FunctionType.Argument> args, Integer minArgs) {
        List<String> names = new ArrayList<>();
        args.stream().map(FunctionType.Argument::getName).forEach(orgName -> {
            String name = orgName;
            int counter = 1;
            while (names.contains(name)) {
                name = orgName + counter++;
            }
            names.add(name);
        });

        if (minArgs == null) {
            minArgs = Integer.MAX_VALUE;
        }

        if (options.allArgumentsAreOptional) {
            minArgs = 0;
        }

        for (int i = 0; i < args.size(); i++) {
            FunctionType.Argument arg = args.get(i);
            write(visitorArg.builder, names.get(i));
            if (i >= minArgs) {
                write(visitorArg.builder, "?");
            }
            write(visitorArg.builder, ": ");
            arg.getType().accept(new TypeVisitor(), visitorArg);
            if (i != args.size() - 1) {
                write(visitorArg.builder, ", ");
            }
        }
    }

    private static final class VisitorArg {
        final StringBuilder builder;
        final String path;
        private fj.data.List<DeclarationType> seen;

        VisitorArg(StringBuilder builder, fj.data.List<DeclarationType> seen, String path) {
            this.builder = builder;
            this.seen = seen;
            this.path = path;
        }

        VisitorArg cons(DeclarationType type, boolean nullPath) {
            String path = this.path;
            if (nullPath) {
                path = null;
            }
            return new VisitorArg(builder, seen.cons(type), path);
        }

        VisitorArg addPath(String propName) {
            if (this.path == null) {
                return this;
            }
            return new VisitorArg(builder, seen, this.path.isEmpty() ? propName : this.path + "." + propName);
        }

        boolean contains(DeclarationType type) {
            return seen.exists(otherType -> Objects.equals(otherType, type));
        }

        public fj.data.List<DeclarationType> getSeen() {
            return seen;
        }
    }

    private final class GotCyclic extends RuntimeException {
        final fj.data.List<DeclarationType> types;
        final DeclarationType singleType;

        private GotCyclic(fj.data.List<DeclarationType> types) {
            assert !types.isEmpty();
            this.types = types;
            this.singleType = null;
        }

        private GotCyclic(DeclarationType type) {
            this.singleType = type;
            this.types = null;
        }
    }

    private void printObjectField(VisitorArg arg, String name, DeclarationType type, DeclarationTypeVisitorWithArgument<Void, VisitorArg> visitor) {
        printObjectField(arg, name, type, visitor, null);
    }

    private void printObjectField(VisitorArg arg, String name, DeclarationType type, DeclarationTypeVisitorWithArgument<Void, VisitorArg> visitor, String prefix) {
        if (type instanceof FunctionType && ((FunctionType)type).getAstNodes().size() == 1) {
            FunctionExpression function = ((FunctionType) type).getAstNodes().iterator().next();
            if (function.jsDoc != null) {
                String jsdoc = function.jsDoc.value;
                printObjectField(arg, name, type, visitor, prefix, jsdoc);
                return;
            }
        }
        printObjectField(arg, name, type, visitor, prefix, null);
    }

    private void printObjectField(VisitorArg arg, String name, DeclarationType type, DeclarationTypeVisitorWithArgument<Void, VisitorArg> visitor, String prefix, String jsDoc) {
        if (jsDoc != null) {
            printJSDoc(jsDoc, arg);
        }
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

    private void printJSDoc(String comment, VisitorArg arg) {
        String[] lines = comment.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            line = line.trim();
            if (i != 0) {
                line = " " + line;
            }
            writeln(arg.builder, line);
        }
    }

    private void printFunction(TypeVisitor visitor, VisitorArg arg, FunctionType functionType, boolean insideInterface) {
        if (!insideInterface && printsAsInterface.containsKey(functionType)) {
            printsAsInterface.get(functionType).accept(visitor, arg);
        } else {
            if (arg.contains(functionType)) {
                throw new GotCyclic(arg.getSeen());
            }

            arg = arg.cons(functionType, true);

            write(arg.builder, "(");
            printArguments(arg, functionType.getArguments(), functionType.minArgs);
            if (insideInterface) {
                write(arg.builder, ") : ");
            } else {
                write(arg.builder, ") => ");
            }
            functionType.getReturnType().accept(visitor, arg);
        }
    }

    public void printObjectTypeBig(TypeVisitor visitor, VisitorArg arg, UnnamedObjectType objectType) {
        StringBuilder builder = arg.builder;
        VisitorArg subArg = new VisitorArg(builder, arg.seen, arg.path);
        writeln(builder, "{");

        ident++;
        List<String> keys = objectType.getDeclarations().keySet().stream().sorted(String::compareTo).collect(Collectors.toList());
        for (int i = 0; i < keys.size(); i++) {
            String name = keys.get(i);
            ident(builder);
            writeName(builder, name);
            write(builder, ": ");
            DeclarationType type = objectType.getDeclarations().get(name);
            type.accept(visitor, subArg.addPath(name));
            if (i != keys.size() - 1) {
                write(builder, ", ");
            }
            write(builder, "\n");
        }
        ident--;
        writeln(builder, "}");
    }

    public void printObjectType(TypeVisitor visitor, VisitorArg arg, UnnamedObjectType objectType) {
        StringBuilder builder = new StringBuilder();
        VisitorArg subArg = new VisitorArg(builder, arg.seen, arg.path);
        write(builder, "{");
        List<String> keys = objectType.getDeclarations().keySet().stream().sorted(String::compareTo).collect(Collectors.toList());
        for (int i = 0; i < keys.size(); i++) {
            String name = keys.get(i);
            writeName(builder, name);
            write(builder, ": ");
            DeclarationType type = objectType.getDeclarations().get(name);
            type.accept(visitor, subArg.addPath(name));
            if (i != keys.size() - 1) {
                write(builder, ", ");
            }
        }
        write(builder, "}");
        String declarationsString = builder.toString();
        if (declarationsString.contains("\n") || declarationsString.length() > 50) {
            throw new GotCyclic(arg.getSeen());
        } else {
            arg.builder.append(declarationsString);
        }
    }

    private class TypeVisitor implements DeclarationTypeVisitorWithArgument<Void, VisitorArg> {

        @Override
        public Void visit(FunctionType functionType, VisitorArg arg) {
            if (declarationNames.containsKey(functionType)) {
                write(arg.builder, "typeof " + declarationNames.get(functionType));
            } else {
                printFunction(this, arg, functionType, false);
            }
            return null;
        }


        @Override
        public Void visit(PrimitiveDeclarationType primitive, VisitorArg arg) {
            write(arg.builder, primitive.getPrettyString());
            return null;
        }

        @Override
        public Void visit(UnnamedObjectType objectType, VisitorArg arg) {
            if (declarationNames.containsKey(objectType)) {
                write(arg.builder, "typeof " + declarationNames.get(objectType));
                return null;
            }
            if (printsAsInterface.containsKey(objectType)) {
                return printsAsInterface.get(objectType).accept(this, arg);
            } else {
                if (arg.contains(objectType)) {
                    throw new GotCyclic(arg.getSeen());
                }

                arg = arg.cons(objectType, false);

                printObjectType(this, arg, objectType);
                return null;
            }
        }

        @Override
        public Void visit(InterfaceDeclarationType interfaceType, VisitorArg arg) {
            if (finishing) {
                finishing = false;
                if (!interfaceType.getNames().isEmpty()) {
                    writeln(arg.builder, "// Seen as: " + interfaceType.getNames().stream().sorted().collect(Collectors.joining(", ")));
                }
                writeln(arg.builder, "interface " + interfaceType.name + " {");
                ident++;
                if (interfaceType.getFunction() != null) {
                    ident(arg.builder);
                    printFunction(this, arg, interfaceType.getFunction(), true);
                    write(arg.builder, ";\n");
                }
                // [s: string]: PropertyDescriptor;
                if (interfaceType.getDynamicAccess() != null) {
                    boolean isNumberIndexer = interfaceType.getDynamicAccess().isNumberIndexer();

                    if (isNumberIndexer || options.printStringIndexers) {
                        ident(arg.builder);
                        write(arg.builder, "[");
                        if (isNumberIndexer) {
                            write(arg.builder, "index: number");
                        } else {
                            write(arg.builder, "s: string");
                        }
                        write(arg.builder, "]: ");
                        interfaceType.getDynamicAccess().getReturnType().accept(this, arg);
                        write(arg.builder, ";\n");
                    }
                }
                if (interfaceType.getObject() != null) {
                    interfaceType.getObject().getDeclarations().entrySet().stream().sorted(Util::compareStringEntry).forEach(entry -> printObjectField(arg, entry.getKey(), entry.getValue(), this));
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
        public Void visit(NamedObjectType named, VisitorArg arg) {
            String name = named.getName();
            switch (name) {
                case "String":
                case "Boolean":
                case "Number":
                    new NamedObjectType(name.toLowerCase(), false).accept(this, arg);
                    break;
                case "Array":
                    printArray(arg, named.getIndexType());
                    break;
                case "NodeListOf":
                    write(arg.builder, "NodeListOf<any>");
                    break;
                default:
                    Type type = nativeClasses.typeFromName(name);
                    if (type != null && type instanceof GenericType) {
                        GenericType generic = (GenericType) type;
                        int typeArgs = generic.getTypeArguments().size();
                        write(arg.builder, name);
                        if (typeArgs > 0) {
                            write(arg.builder, "<");
                            for (int i = 0; i < typeArgs; i++) {
                                write(arg.builder, "any");
                                if (i != typeArgs - 1) {
                                    write(arg.builder, ", ");
                                }
                            }
                            write(arg.builder, ">");
                        }
                    } else {
                        write(arg.builder, name);
                    }
                    break;
            }
            return null;
        }

        private void printArray(VisitorArg arg, DeclarationType indexType) {
            if (indexType == null) {
                arg.builder.append("Array<any>");
                return;
            }
            indexType = indexType.resolve();
            if (indexType instanceof PrimitiveDeclarationType) {
                PrimitiveDeclarationType prim = (PrimitiveDeclarationType) indexType;
                if (prim.getType() == PrimitiveDeclarationType.Type.STRING_OR_NUMBER || prim.getType() == PrimitiveDeclarationType.Type.VOID) {
                    arg.builder.append("Array<any>");
                } else {
                    arg.builder.append(prim.getPrettyString());
                    arg.builder.append("[]");
                }
            } else if (indexType instanceof UnionDeclarationType) {
                arg.builder.append("Array<any>");
            } else if (indexType instanceof NamedObjectType) {
                if (arg.contains(indexType)) {
                    arg.builder.append("any");
                } else {
                    // Stopping if we just have arrays of arrays. Just printing is as any[][]; and stopping it there.
                    if (((NamedObjectType) indexType).getName().equals("Array") && ((NamedObjectType) indexType).getIndexType() != null && ((NamedObjectType) indexType).getIndexType().resolve() instanceof NamedObjectType && ((NamedObjectType) ((NamedObjectType) indexType).getIndexType().resolve()).getName().equals("Array")) {
                        arg.builder.append("any[][]");
                    } else {
                        arg.builder.append("Array<");
                        indexType.accept(this, arg.cons(indexType, true));
                        arg.builder.append(">");
                    }
                }
            } else if (indexType instanceof UnnamedObjectType || indexType instanceof FunctionType || indexType instanceof DynamicAccessType) {
                if (printsAsInterface.containsKey(indexType)) {
                    arg.builder.append("Array<");
                    indexType.accept(this, arg);
                    arg.builder.append(">");
                } else {
                    throw new GotCyclic(indexType);
                }
            } else if (indexType instanceof InterfaceDeclarationType || indexType instanceof ClassInstanceType) {
                indexType.accept(this, arg);
                arg.builder.append("[]");
            } else if (indexType instanceof ClassType) {
                // This doesn't make sense.
                arg.builder.append("any[]");
            } else {
                throw new RuntimeException("Havn't considered arrays of " + indexType.getClass().getSimpleName() + " yet!");
            }
        }

        @Override
        public Void visit(ClassType classType, VisitorArg arg) {
            if (finishing) {
                finishing = false;
                // First an constructor interface.
                if (!classType.getConstructorType().getAstNodes().isEmpty()) {
                    FunctionExpression func = classType.getConstructorType().getAstNodes().iterator().next();
                    if (func.jsDoc != null) {
                        printJSDoc(func.jsDoc.value, arg);
                    }
                }
                writeln(arg.builder, "interface " + classType.getName() + "Constructor {");
                ident++;
                ident(arg.builder);
                write(arg.builder, "new (");
                printArguments(arg, classType.getConstructorType().getArguments(), classType.getConstructorType().minArgs);
                write(arg.builder, ") : " + classType.getName() + "\n");

                classType.getStaticFields().entrySet().stream().sorted(Util::compareStringEntry).forEach(entry -> printObjectField(arg, entry.getKey(), entry.getValue(), this));

                ident--;
                writeln(arg.builder, "}");
                write(arg.builder, "\n");

                writeln(arg.builder, "// Seen as: " + classType.getNames().stream().sorted().collect(Collectors.joining(", ")));
                ident(arg.builder);
                write(arg.builder, "interface " + classType.getName());
                if (classType.getSuperClass() != null) {
                    printExtends(classType, arg);
                }
                write(arg.builder, " {\n");

                Map<String, String> memberDocs = new HashMap<>();
                if (classType.getConstructorType().getAstNodes().size() == 1) {
                    FunctionExpression func = classType.getConstructorType().getAstNodes().iterator().next();
                    func.memberJsDocs.entrySet().forEach(entry -> memberDocs.put(entry.getKey(), entry.getValue().value));
                }

                ident++;
                Predicate<String> notInSuperClassTest = notInSuperClassTest(classType.getSuperClass());
                classType.getPrototypeFields().entrySet().stream().sorted(Util::compareStringEntry).filter((entry) -> notInSuperClassTest.test(entry.getKey())).forEach((entry) -> {
                    printObjectField(arg, entry.getKey(), entry.getValue(), this, null, memberDocs.get(entry.getKey()));
                });

                ident--;
                writeln(arg.builder, "}");
                write(arg.builder, "\n");
                finishing = true;
            } else {
                if (declarationNames.containsKey(classType)) {
                    write(arg.builder, "typeof " + declarationNames.get(classType));
                } else {
                    write(arg.builder, classType.getName() + "Constructor");
                    classesToPrint.add(classType);
                }
            }

            return null;
        }

        @Override
        public Void visit(ClassInstanceType instanceType, VisitorArg arg) {
            if (declarationNames.containsKey(instanceType.getClazz())) {
                write(arg.builder, declarationNames.get(instanceType.getClazz()));
            } else {
                write(arg.builder, instanceType.getClazz().getName());
                classesToPrint.add(instanceType.getClazz());
            }
            return null;
        }
    }

    private void printExtends(ClassType classType, VisitorArg arg) {
        write(arg.builder, " extends ");
        if (classType.getSuperClass() instanceof ClassType) {
            ClassType superClass = (ClassType) classType.getSuperClass();
            if (declarationNames.containsKey(superClass)) {
                write(arg.builder, declarationNames.get(superClass));
            } else {
                write(arg.builder, superClass.getName());
                classesToPrint.add(superClass);
            }
        } else if (classType.getSuperClass() instanceof NamedObjectType) {
            String name = ((NamedObjectType) classType.getSuperClass()).getName();
            if (name.equals("Array")) {
                name = "Array<any>";
            }
            write(arg.builder, name);
        } else {
            throw new RuntimeException();
        }
    }


    private Predicate<String> notStaticInSuperClassTest(DeclarationType superClass) {
        Set<String> staticInSuper = ClassType.getStaticFieldsInclSuper(superClass, nativeClasses);
        return (name) -> !staticInSuper.contains(name);
    }

    private Predicate<String> notInSuperClassTest(DeclarationType superClass) {
        Set<String> fieldsInSuper = ClassType.getFieldsInclSuper(superClass, nativeClasses);
        return (name) -> !fieldsInSuper.contains(name);
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
