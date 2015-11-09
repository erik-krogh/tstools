package dk.webbies.tscreate.jsnap;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dk.webbies.tscreate.util.Util;
import dk.webbies.tscreate.paser.AST.BlockStatement;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.paser.AST.Identifier;
import dk.webbies.tscreate.paser.AST.NodeTransverse;

import java.io.*;
import java.util.*;

public class JSNAPUtil {

    public static String getJsnapRaw(String path) throws IOException {
        return Util.getCachedOrRun(path + ".jsnap", new File(path), "lib/jsnap/jsnap.js --createInstances " + path);
    }

    private static String getEmptyJSNAP() throws IOException {
        File checkAgainst = new File("lib/jsnap/node_modules/phantomjs/lib/phantom");
        return Util.getCachedOrRun("onlyDom.jsnap", checkAgainst, "lib/jsnap/jsnap.js");
    }

    public static Snap.Obj extractUnique(Snap.Obj librarySnap) throws IOException {
        FunctionExpression emptyProgram = new FunctionExpression(null, new Identifier(null, ":program"), new BlockStatement(null, Collections.EMPTY_LIST), Collections.EMPTY_LIST);
        Snap.Obj domSnap = JSNAPUtil.getStateDump(JSNAPUtil.getEmptyJSNAP(), emptyProgram);
        return extractUnique(librarySnap, domSnap);
    }

    public static Snap.Obj extractUnique(Snap.Obj librarySnap, Snap.Obj domSnap) {
        // Keys for the objects in the library, that are also in the DOM.
        Set<Integer> libraryKeysInDom = getKeysSharedWithDom(librarySnap, domSnap, new HashSet<>());

        return extractUnique(librarySnap, domSnap, libraryKeysInDom, new Snap.Obj(), new HashMap<>());
    }

    private static Snap.Obj extractUnique(Snap.Obj librarySnap, Snap.Obj domSnap, Set<Integer> libraryKeysInDom, Snap.Obj result, Map<Integer, Snap.Obj> alreadyCreated) {
        result.env = librarySnap.env;
        result.prototype = librarySnap.prototype;
        result.key = librarySnap.key;
        result.function = librarySnap.function;
        result.properties = new ArrayList<>();
        for (Snap.Property property : librarySnap.properties) {
            if (domSnap.getProperty(property.name) == null) {
                if (property.value != null && property.value instanceof Snap.Obj) {
                    Snap.Obj obj = (Snap.Obj) property.value;
                    if (!libraryKeysInDom.contains(obj.key)) {
                        if (alreadyCreated.containsKey(obj.key)) {
                            property.value = alreadyCreated.get(obj.key);
                        } else {
                            Snap.Obj subResult = new Snap.Obj();
                            alreadyCreated.put(obj.key, subResult);
                            property.value = extractUnique(obj, new Snap.Obj(), libraryKeysInDom, subResult, alreadyCreated);
                        }
                        result.properties.add(property);
                    }
                } else {
                    result.properties.add(property);
                }
            }
        }
        return result;
    }

    private static Set<Integer> getKeysSharedWithDom(Snap.Obj librarySnap, Snap.Obj domSnap, HashSet<Integer> result) {
        if (librarySnap.properties == null) {
            return Collections.EMPTY_SET;
        }
        for (Snap.Property property : librarySnap.properties) {
            if (property == null || property.value == null) {
                continue;
            }
            if (domSnap.getProperty(property.name) != null && property.value instanceof Snap.Obj) {
                Snap.Value domValue = domSnap.getProperty(property.name).value;
                if (domValue instanceof Snap.Obj) {
                    int key = ((Snap.Obj) property.value).key;
                    if (!result.contains(key)) {
                        result.add(key);
                        getKeysSharedWithDom((Snap.Obj) property.value, (Snap.Obj) domValue, result);
                    }
                }
            }
        }


        return result;
    }

    public static Snap.Obj getStateDumpFromFile(String path, FunctionExpression program) throws IOException {
        String jsnapRaw = readFile(path);
        return getStateDump(jsnapRaw, program);
    }

    public static Snap.Obj getStateDump(String jsnapRaw, FunctionExpression program) {
        GsonBuilder builder = new GsonBuilder();
        List<FunctionExpression> functions = getFunctions(program);
        builder.registerTypeAdapterFactory(new KeyTypeFactory());
        Gson gson = builder.create();
        Snap.StateDump stateDump = gson.fromJson(jsnapRaw, Snap.StateDump.class);
        resolveFunctions(stateDump, functions);
        resolveKeys(stateDump);

        return stateDump.getGlobal();
    }

    public static List<FunctionExpression> getFunctions(FunctionExpression program) {
        FunctionExtractor functionExtrator = new FunctionExtractor();
        program.accept(functionExtrator);
        return functionExtrator.getFunctions();
    }

    public static Map<String, Snap.Property> createPropertyMap(Snap.Obj obj) {
        HashMap<String, Snap.Property> result = new HashMap<>();
        obj.properties.forEach(prop -> result.put(prop.name, prop));
        if (obj.prototype != null) {
            result.putAll(createPropertyMap(obj.prototype));
        }
        return result;
    }

    private static class FunctionExtractor implements NodeTransverse<Void> {
        List<FunctionExpression> functions = new ArrayList<>();
        @Override
        public Void visit(FunctionExpression function) {
            functions.add(function);
            return NodeTransverse.super.visit(function);
        }

        public List<FunctionExpression> getFunctions() {
            return functions;
        }
    }

    private static void resolveFunctions(Snap.StateDump stateDump, List<FunctionExpression> functions) {
        for (Snap.Obj obj : stateDump.heap) {
            if (obj == null || obj.function == null) {
                continue;
            }
            Snap.Function function = obj.function;
            if (function.type.equals("user")) {
                int id = Integer.parseInt(function.id);
                function.astNode = functions.get(id);
            } else if (function.type.equals("bind")) {
                function.target = stateDump.heap.get((function.target).key);
                if (!function.target.function.type.equals("user")) {
                    throw new RuntimeException();
                }
            }
        }
    }

    private static void resolveKeys(Snap.StateDump stateDump) {
        for (int i = 1; i < stateDump.heap.size(); i++) {
            Snap.Obj obj = stateDump.heap.get(i);
            obj.key = i;
        }
        for (Snap.Obj obj : stateDump.heap) {
            if (obj == null || obj.properties == null) {
                continue;
            }
            if (obj.function != null && obj.function.instance != null) {
                obj.function.instance = stateDump.heap.get(obj.function.instance.key);
            }
            for (Snap.Property prop : obj.properties) {
                if (prop.value instanceof Snap.Obj) {
                    prop.value = stateDump.heap.get(((Snap.Obj) prop.value).key);

                    Snap.Obj propValue = (Snap.Obj) prop.value;
                    if (propValue.env != null) {
                        propValue.env = stateDump.heap.get((propValue.env).key);
                    }
                    if (propValue.prototype != null) {
                        propValue.prototype = stateDump.heap.get((propValue.prototype).key);
                    }
                }
                if (prop.get != null && prop.get instanceof Snap.Obj) {
                    prop.get = stateDump.heap.get(((Snap.Obj) prop.get).key);
                }
                if (prop.set != null && prop.set instanceof Snap.Obj) {
                    prop.set = stateDump.heap.get(((Snap.Obj) prop.set).key);
                }
            }
        }

    }

    private static String readFile(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append("\n");
        }
        return builder.toString();
    }

    private static class KeyTypeFactory implements TypeAdapterFactory {
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            Class<T> rawType = (Class<T>) type.getRawType();
            if (!rawType.equals(Snap.Value.class)) {
                return null;
            }

            return new TypeAdapter<T>() {
                public void write(JsonWriter out, T value) throws IOException {
                    throw new RuntimeException("Cannot write these now");
                }

                public T read(JsonReader reader) throws IOException {
                    if (reader.peek() == JsonToken.NULL) {
                        reader.nextNull();
                        return (T) new Snap.NullConstant();
                    }
                    if (reader.peek() == JsonToken.STRING) {
                        return (T) new Snap.StringConstant(reader.nextString());
                    }
                    if (reader.peek() == JsonToken.BOOLEAN) {
                        return (T) new Snap.BooleanConstant(reader.nextBoolean());
                    }
                    if (reader.peek() == JsonToken.NUMBER) {
                        return (T) new Snap.NumberConstant(reader.nextDouble());
                    }

                    reader.beginObject();
                    reader.nextName();
                    int key;
                    if (reader.peek() == JsonToken.BOOLEAN) {
                        reader.nextBoolean();
                        reader.endObject();
                        return (T) new Snap.UndefinedConstant();
                    } else {
                        key = reader.nextInt();
                    }

                    reader.endObject();

                    Snap.Obj obj = new Snap.Obj();
                    obj.key = key;
                    return (T) obj;
                }
            };
        }

        private String toLowercase(Object o) {
            return o.toString().toLowerCase(Locale.US);
        }
    }
}