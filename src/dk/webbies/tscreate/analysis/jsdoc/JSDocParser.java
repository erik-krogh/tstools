package dk.webbies.tscreate.analysis.jsdoc;

import com.google.gson.Gson;
import com.google.javascript.jscomp.parsing.parser.trees.Comment;
import com.sun.org.apache.xalan.internal.utils.FeatureManager;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.analysis.unionFind.HasPrototypeNode;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.util.Util;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.*;

/**
 * Created by erik1 on 29-04-2016.
 */
public class JSDocParser {
    private final Snap.Obj globalObject;
    private final TypeAnalysis typeAnalysis;
    private DeclarationParser.NativeClassesMap nativeClasses;

    public JSDocParser(Snap.Obj globalObject, TypeAnalysis typeAnalysis, DeclarationParser.NativeClassesMap nativeClasses) {
        this.globalObject = globalObject;
        this.typeAnalysis = typeAnalysis;
        this.nativeClasses = nativeClasses;
    }

    public FunctionType parseFunctionDoc(Snap.Obj closure, Comment comment) {
        List<FunctionType.Argument> arguments = new ArrayList<>();
        DeclarationType ret = null;

        List<Tag> tags = parseComment(comment.value);
        for (Tag tag : tags) {
            switch (tag.title.toUpperCase()) {
                case "PARAM":
                    arguments.add(new FunctionType.Argument(tag.name, toDeclarationType(tag.type, closure)));
                    break;
                case "RETURN":
                case "RETURNS":
                    assert ret == null;
                    ret = toDeclarationType(tag.type, closure);
                    break;
                case "STATIC":case "CLASS":case "CONSTRUCTOR":
                    break;
                default:
                    throw new RuntimeException("Don't know how to handle: " + tag.title);
            }
        }
        if (ret == null) {
            System.err.println("Return: null");
            ret = PrimitiveDeclarationType.Void(EMPTY_SET);
        }

        return new FunctionType(ret, arguments, EMPTY_SET);
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
            return null;
        }
        switch (type.type) {
            case "NameExpression":
                DeclarationType dec = fromHeapValue(type.name, closure);
                if (dec != null) {
                    return dec;
                }
                switch (type.name.toLowerCase()) {
                    case "number":
                        return PrimitiveDeclarationType.Number(EMPTY_SET);
                    case "string":
                        return PrimitiveDeclarationType.String(EMPTY_SET);
                    case "void":
                        return PrimitiveDeclarationType.Void(EMPTY_SET);
                    case "any":
                        return PrimitiveDeclarationType.Any(EMPTY_SET);
                    case "boolean":
                    case "bool":
                        return PrimitiveDeclarationType.Boolean(EMPTY_SET);
                }
                throw new RuntimeException("Don't know how to handle the name: " + type.name);
            case "TypeApplication":
                if (type.expression.type.equals("NameExpression") && type.expression.name.equals("Array")) {
                    return new NamedObjectType("Array", false, toDeclarationType(type.expression, closure));
                }
                throw new RuntimeException("Don't know how to handle this typeApplication, expression: " + type.expression.type);
            default:
                throw new RuntimeException("Don't know how to handle: " + type.type.toUpperCase());
        }

    }

    private DeclarationType fromHeapValue(String type, Snap.Obj closure) {
        if (type == null) {
            return null;
        }
        Snap.Obj env = closure.env;
        while (env != null) {
            try {
                Snap.Value value = JSNAPUtil.lookupRecursive(env, type);
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
                " */";
        List<Tag> tags = parseComment(docExample);
        System.out.println(tags);
    }

    private static List<Tag> parseComment(String docExample) {
        try {
            return new Gson().fromJson(Util.runNodeScript("node_modules/doctrine-cli/index.js --unwrap --sloppy", docExample), JSDoc.class).tags;
        } catch (IOException e) {
            throw new RuntimeException();
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
        public String name;

    }
}
