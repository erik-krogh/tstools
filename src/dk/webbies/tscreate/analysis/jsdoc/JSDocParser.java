package dk.webbies.tscreate.analysis.jsdoc;

import com.google.gson.Gson;
import com.google.javascript.jscomp.newtypes.Declaration;
import com.google.javascript.jscomp.parsing.parser.trees.Comment;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.analysis.unionFind.HasPrototypeNode;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.util.Pair;
import dk.webbies.tscreate.util.Util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Collections.*;

/**
 * Created by erik1 on 29-04-2016.
 */
public class JSDocParser {
    private final Snap.Obj globalObject;
    private final TypeAnalysis typeAnalysis;
    private DeclarationParser.NativeClassesMap nativeClasses;
    private TypeReducer combiner;

    public JSDocParser(Snap.Obj globalObject, TypeAnalysis typeAnalysis, DeclarationParser.NativeClassesMap nativeClasses, TypeReducer typeReducer) {
        this.globalObject = globalObject;
        this.typeAnalysis = typeAnalysis;
        this.nativeClasses = nativeClasses;
        combiner = typeReducer;
    }

    public FunctionType parseFunctionDoc(Snap.Obj closure, Comment comment) {
        List<FunctionType.Argument> arguments = new ArrayList<>();
        DeclarationType ret = null;

        List<Tag> tags = parseComment(comment.value);
        int argCounter = 0;
        for (Tag tag : tags) {
            switch (tag.title.toUpperCase()) {
                case "PARAM":
                    String argName = tag.name;
                    if (argName == null) { // TODO: If argName contains ".", then collect into object.
                        argName = "arg" + argCounter++;
                    }
                    if (argName.contains(".")) {
                        argName = argName.replace(".", "");
                    }
                    arguments.add(new FunctionType.Argument(argName, toDeclarationType(tag.type, closure)));
                    break;
                case "RETURN":
                case "RETURNS":
//                    assert ret == null; // There is a JSDoc standard, but it doesn't seem that anyone read that.
                    ret = toDeclarationType(tag.type, closure);
                    break;
                case "STATIC":case "CLASS":case "CONSTRUCTOR":case "LICENSE":case "PRIVATE":case "EVENT":case "MEMBEROF":case "PROTECTED":case "CONSTANT":case "EXTENDS":case "EXAMPLE":case "FIRES":case "SEE":case "NAME":case "NAMESPACE":
                    break;
                default:
//                    throw new RuntimeException("Don't know how to handle: " + tag.title);
                    break;
            }
        }
        if (ret == null) {
//            System.err.println("Return: null");
            ret = PrimitiveDeclarationType.Void(EMPTY_SET);
        }

        return new FunctionType(closure.function.astNode, ret, arguments, EMPTY_SET);
    }

    public DeclarationType parseMemberDoc(Snap.Obj closure, Comment comment) {
        DeclarationType ret = null;

        List<Tag> tags = parseComment(comment.value);
        for (Tag tag : tags) {
            switch (tag.title.toUpperCase()) {
                case "MEMBER":case "TYPE":
                    assert ret == null;
                    ret = toDeclarationType(tag.type, closure);
                    break;
                case "METHOD":
                    ret = new FunctionType(PrimitiveDeclarationType.Void(Collections.EMPTY_SET), Collections.EMPTY_LIST, Collections.EMPTY_SET, Collections.EMPTY_LIST);
                    break;
                case "PRIVATE":case "DEFAULT":case "SEE":case "READONLY":case "MEMBEROF":case "LINK":case "EXAMPLE":
                    break;
                case "PARAM":
                    ret = parseFunctionDoc(closure, comment);
                    break;
                default:
                    break;

            }
        }
        return ret;
    }

    public static boolean isClass(FunctionExpression functionExpression) {
        if (functionExpression == null) {
            return false;
        }
        if (functionExpression.jsDoc != null) {
            return parseComment(functionExpression.jsDoc.value).stream().map(feature -> feature.title.toUpperCase()).anyMatch(title -> title.equals("CLASS") || title.equals("CONSTRUCTOR"));
        }
        return false;
    }

    private DeclarationType toDeclarationType(Type type, Snap.Obj closure) {
        if (type == null || type.type == null) {
            return PrimitiveDeclarationType.Void(Collections.EMPTY_SET);
        }
        switch (type.type) {
            case "NameExpression":
                DeclarationType dec = fromHeapValue(type.name, closure);
                if (dec != null) {
                    return dec;
                }
                switch (type.name.toLowerCase()) {
                    case "number":
                    case "int":
                        return PrimitiveDeclarationType.Number(EMPTY_SET);
                    case "string":
                        return PrimitiveDeclarationType.String(EMPTY_SET);
                    case "void":
                        return PrimitiveDeclarationType.Void(EMPTY_SET);
                    case "any":
                    case "null":
                        return PrimitiveDeclarationType.Any(EMPTY_SET);
                    case "boolean":
                    case "bool":
                    case "true":
                    case "false":
                        return PrimitiveDeclarationType.Boolean(EMPTY_SET);
                    case "array":
                        return new NamedObjectType("Array", false);
                    case "functon":
                    case "function":
                        return new FunctionType(null, PrimitiveDeclarationType.Void(EMPTY_SET), EMPTY_LIST, EMPTY_SET);
                    case "object":
                        return new NamedObjectType("Object", false);
                    case "domelement":
                        return new NamedObjectType("Element", false);
                    case "date":
                        return new NamedObjectType("Date", false);
                }
                System.err.println("Don't know how to handle the name: " + type.name);
                return PrimitiveDeclarationType.Void(EMPTY_SET);
            case "TypeApplication":
                if (type.expression.type.equals("NameExpression") && type.expression.name.toLowerCase().equals("array") && type.applications != null && !type.applications.isEmpty()) {
                    return new NamedObjectType("Array", false, toDeclarationType(type.applications.iterator().next(), closure));
                }
                if (type.expression.type.equals("NameExpression")) {
                    return new NamedObjectType(type.expression.name, false);
                }
                throw new RuntimeException("Don't know how to handle this typeApplication, expression: " + type.expression.type);
            case "UnionType":
                return new UnionDeclarationType(type.elements.stream().map(subtype -> toDeclarationType(subtype, closure)).collect(Collectors.toList()));
            case "OptionalType": // TODO: Handle?
                return toDeclarationType(type.expression, closure);
            case "RestType": // TODO: Handle?
                return toDeclarationType(type.expression, closure);
            case "NullLiteral": // In TypeScript, anything can be null, so this type doesn't make sense in TypeScript.
            case "UndefinedLiteral":
                return PrimitiveDeclarationType.Void(EMPTY_SET);
            case "AllLiteral":
                return PrimitiveDeclarationType.Any(EMPTY_SET);
            case "NonNullableType":
            case "NullableType":
                return toDeclarationType(type.expression, closure);
            case "FunctionType":
                AtomicInteger argCounter = new AtomicInteger(0);
                List<FunctionType.Argument> args = type.params.stream().map(arg -> toDeclarationType(arg, closure)).map(arg -> new FunctionType.Argument("arg" + argCounter.incrementAndGet(), arg)).collect(Collectors.toList());
                DeclarationType returnType = toDeclarationType(type.result, closure);
                if (returnType == null) {
                    returnType = PrimitiveDeclarationType.Void(EMPTY_SET);
                }
                return new FunctionType(null, returnType, args, EMPTY_SET);
            case "RecordType":
                Map<String, DeclarationType> fields = type.fields.stream().map(field -> new Pair<>(field.key, toDeclarationType(field.value, closure))).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
                return new UnnamedObjectType(fields, EMPTY_SET);
            case "ArrayType": // Really a tuple from TypeScript, but I can't export that as of now.
                return new NamedObjectType("Array", false, new CombinationType(combiner, type.elements.stream().map(subtype -> toDeclarationType(subtype, closure)).collect(Collectors.toList())));
            default:
                throw new RuntimeException("Don't know how to handle: " + type.type);
        }

    }

    private DeclarationType fromHeapValue(String path, Snap.Obj closure) {
        if (path == null) {
            return null;
        }
        Snap.Obj env = closure.env;
        while (env != null) {
            try {
                Snap.Value value = JSNAPUtil.lookupRecursive(env, path);
                if (value instanceof Snap.Obj && ((Snap.Obj) value).function != null) {
                    Snap.Obj obj = (Snap.Obj) value;
                    return typeAnalysis.getTypeFactory().getType(new HasPrototypeNode(typeAnalysis.getSolver(), (Snap.Obj) obj.getProperty("prototype").value));
                } else {
                    return typeAnalysis.getTypeFactory().getType(typeAnalysis.getHeapFactory().fromValue(value));
                }
            } catch (AssertionError | RuntimeException ignored) { }


            env = env.env;
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        String docExample = "/**\n\n" +
                " * A short hand way of creating a movieclip from an array of frame ids\r\n" +
                " *\n\n" +
                " * @static\n\n" +
                " * @param {string[]} the array of frames ids the movieclip will use as its texture frames\n\n" +
                " */=";
        List<Tag> tags = parseComment(docExample);
        System.out.println(tags.size());
        tags = parseComment(docExample);
        System.out.println(tags.size());
        tags = parseComment(docExample);
        System.out.println(tags.size());
        tags = parseComment(docExample);
        System.out.println(tags.size());
    }

    private static List<Tag> parseComment(String doc) {
        try {
            return new Gson().fromJson(NodeRunner.getInstance().parseComment(doc), JSDoc.class).tags;
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private static final class NodeRunner {
        static NodeRunner instance = null;
        private final Process process;
        private final FlushableStreamGobbler inputGobbler;

        public synchronized static NodeRunner getInstance() throws IOException {
            if (instance == null) {
                instance = new NodeRunner();
            }
            return instance;
        }

        private NodeRunner() throws IOException {
            process = Runtime.getRuntime().exec("node " + "node_modules/doctrine-cli/index.js --unwrap --sloppy --multiple");
            inputGobbler = new FlushableStreamGobbler(process.getInputStream());
            new Util.StreamGobbler(process.getErrorStream(), new CountDownLatch(1));
        }

        synchronized String parseComment(String doc) throws IOException {
            process.getOutputStream().write(doc.getBytes());
            process.getOutputStream().write(0);
            process.getOutputStream().flush();

            try {
                return inputGobbler.getResult();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class FlushableStreamGobbler extends Thread {
        BufferedInputStream is;
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

        private FlushableStreamGobbler(InputStream is) {
            this.is = new BufferedInputStream(is);
            this.start();
        }

        public String getResult() throws InterruptedException {
            return queue.take();
        }

        @Override
        public void run() {
            try {
                StringBuilder builder = new StringBuilder();
                while (true) {
                    char character = (char) is.read();
//                    System.out.println("Read: " + Character.toString(character));
                    if (Character.toString(character).equals("\n")) {
                        queue.put(builder.toString());
                        builder = new StringBuilder();
                    } else {
                        builder.append(character);
                    }
                }
            } catch (InterruptedException | IOException ioe) {
                ioe.printStackTrace();
                throw new RuntimeException(ioe);
            }
        }
    }


    public static final class JSDoc {
        public String description;
        public List<Tag> tags;
    }

    public static final class Tag {
        public String title;
        public String description;
        public String name;
        public Type type;
    }

    public static final class Type {
        public String type;
        public Type expression;
        public List<Type> applications;
        public List<Type> params;
        public Type result;
        public List<Type> elements;
        public String name;
        public List<FieldType> fields;
    }

    public static final class FieldType {
        public String key;
        public Type value;
    }
}
