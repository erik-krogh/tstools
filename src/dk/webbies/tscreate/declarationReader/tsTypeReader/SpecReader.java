package dk.webbies.tscreate.declarationReader.tsTypeReader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import dk.webbies.tscreate.declarationReader.tsTypeReader.types.*;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class SpecReader {

    private final Type global;
    private final TypeNameTree namedTypes;

    public SpecReader() {
        Spec spec = read("file");
        this.namedTypes = spec.getTypes();
        InterfaceType global = makeEmptySyntheticInterfaceType();
        global.getDeclaredProperties().putAll(flattenTypeNameTree(spec.getGlobals()));
        this.global = global;
    }

    public static void main(String[] args) {
        SpecReader reader = new SpecReader();
        Spec spec = reader.read("file");
        System.out.println(spec);
    }

    /**
     * Flattens a tree already flat tree. Exceptions will be thrown if the tree is not flat...
     */
    private static Map<String, Type> flattenTypeNameTree(TypeNameTree tree) {
        // should be type safe...
        Map<String, Type> map = new HashMap<>();
        ((Node) tree).children.forEach((k, v) -> map.put(k, ((Leaf) v).getType()));
        return map;
    }

    public TypeNameTree getNamedTypes() {
        return namedTypes;
    }

    public static Spec read(String script) {
        GsonBuilder builder = new GsonBuilder();
        TypeResolver typeResolver = new TypeResolver();
        builder.registerTypeAdapter(Spec.class, new SpecAdapter(typeResolver));
        builder.registerTypeAdapter(Type.class, new TypeIDAdapter(typeResolver));
        builder.registerTypeAdapter(TypeNameTree.class, new TypeNameTreeAdapter(typeResolver));
        return builder.create().fromJson(script, Spec.class);
    }

    public Type getGlobal() {
        return global;
    }

    /**
     * Convinience method for creting synthetic instances of the InterfaceType.
     * Simplifies some implementation-cases.
     */
    public static InterfaceType makeEmptySyntheticInterfaceType() {
        InterfaceType interfaceType = new InterfaceType();
        interfaceType.setBaseTypes(Lists.newArrayList());
        interfaceType.setDeclaredCallSignatures(Lists.newArrayList());
        interfaceType.setDeclaredConstructSignatures(Lists.newArrayList());
        interfaceType.setDeclaredNumberIndexType(null);
        interfaceType.setDeclaredProperties(Maps.newHashMap());
        interfaceType.setDeclaredStringIndexType(null);
        interfaceType.setTypeParameters(Lists.newArrayList());
        return interfaceType;
    }

    private interface TypeNameTree {

    }

    private static class TypeNameTreeAdapter implements JsonDeserializer<TypeNameTree> {
        private final TypeResolver resolver;

        private TypeNameTreeAdapter(TypeResolver resolver) {
            this.resolver = resolver;
        }

        private TypeNameTree deserialize(JsonElement jsonElement) {
            if (jsonElement.isJsonPrimitive()) {
                return new Leaf(this.resolver.resolve(jsonElement.getAsInt()));
            }
            if (jsonElement.isJsonObject()) {
                Map<String, TypeNameTree> children = new HashMap<>();
                JsonObject object = jsonElement.getAsJsonObject();
                object.entrySet().forEach(e -> children.put(e.getKey(), deserialize(e.getValue())));
                return new Node(children);
            }
            throw new RuntimeException("Unhandled case: " + jsonElement);
        }

        @Override
        public TypeNameTree deserialize(JsonElement jsonElement, java.lang.reflect.Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return deserialize(jsonElement);
        }
    }

    private static class SpecAdapter implements JsonDeserializer<Spec> {

        private final TypeResolver typeResolver;

        public SpecAdapter(TypeResolver typeResolver) {
            this.typeResolver = typeResolver;
        }

        @Override
        public Spec deserialize(JsonElement jsonElement, java.lang.reflect.Type type, JsonDeserializationContext ctx) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray data = jsonObject.get("data").getAsJsonArray();
            for (int id = 0; id < data.size(); id++) {
                this.typeResolver.register(id, deserializeUnresolvedType(data.get(id), ctx));
            }
            this.typeResolver.resolveAll();
            TypeNameTree globals = ctx.deserialize(jsonObject.get("globals"), TypeNameTree.class);
            TypeNameTree types = ctx.deserialize(jsonObject.get("types"), TypeNameTree.class);
            return new Spec(globals, types);
        }

        private Type deserializeUnresolvedType(JsonElement jsonElement, JsonDeserializationContext ctx) {
            JsonObject object = jsonElement.getAsJsonObject();
            TypeKind kind = TypeKind.valueOf(object.get("kind").getAsString());
            switch (kind) {
                case Any:
                case String:
                case Number:
                case Boolean:
                case Void:
                case Undefined:
                case Null:
                case Enum:
                    return ctx.deserialize(object, SimpleType.class);
                case Union:
                    return ctx.deserialize(object, UnionType.class);
                case Interface:
                    return ctx.deserialize(object, InterfaceType.class);
                case TypeParameter:
                    return ctx.deserialize(object, TypeParameterType.class);
                case Class:
                    return ctx.deserialize(object, ClassType.class);
                case Reference:
                    return ctx.deserialize(object, ReferenceType.class);
                case Generic:
                    return ctx.deserialize(object, GenericType.class);
                case Tuple:
                    return ctx.deserialize(object, TupleType.class);
                case Anonymous:
                    return ctx.deserialize(object, AnonymousType.class);
                default:
                    throw new RuntimeException("Unhandled case: " + kind);
            }
        }
    }

    private static class TypeIDAdapter implements JsonDeserializer<Type> {
        private final TypeResolver resolver;

        private TypeIDAdapter(TypeResolver resolver) {
            this.resolver = resolver;
        }

        @Override
        public Type deserialize(JsonElement jsonElement, java.lang.reflect.Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.resolver.resolve(jsonElement.getAsInt());
        }
    }

    public static class Spec {
        private TypeNameTree globals;
        private TypeNameTree types;

        public Spec(TypeNameTree globals, TypeNameTree types) {
            this.globals = globals;
            this.types = types;
        }

        @Override
        public String toString() {
            return "Spec{" +
                    "globals=" + globals +
                    ", types=" + types +
                    '}';
        }

        public TypeNameTree getGlobals() {
            return globals;
        }

        public TypeNameTree getTypes() {
            return types;
        }
    }

    private static class Leaf implements TypeNameTree {
        private final Type type;

        public Leaf(Type type) {
            this.type = type;
        }

        public Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Leaf{" +
                    "type=" + type +
                    '}';
        }
    }

    private static class Node implements TypeNameTree {
        private final Map<String, TypeNameTree> children;

        private Node(Map<String, TypeNameTree> children) {
            this.children = children;
        }

        public Map<String, TypeNameTree> getChildren() {
            return children;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "children=" + children +
                    '}';
        }
    }
}

