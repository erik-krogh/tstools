package dk.webbies.tscreate.jsnapconvert;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dk.webbies.tscreate.paser.NodeTransverse;
import dk.webbies.tscreate.paser.Function;
import dk.webbies.tscreate.paser.Program;

import java.io.*;
import java.util.*;

public class JSNAPConverter {

    public static void main(String[] args) throws IOException {
        Program emptyProgram = new Program(0, Collections.EMPTY_LIST);
        Snap.Obj pixiSnap = getStateDumpFromFile("lib/tscheck/tests/pixi.js.jsnap", emptyProgram);

        Snap.Obj pixiUnique = extractUnique(pixiSnap);

        Map<String, Snap.Value> heap = makePrettyHeap("", pixiSnap, new HashSet<Snap.Obj>());
        System.out.println(pixiSnap);
    }

    public static Snap.Obj extractUnique(Snap.Obj librarySnap) throws IOException {
        Snap.Obj domSnap = getStateDumpFromFile("src/dk/webbies/tscreate/jsnapconvert/onlyDom.jsnap", new Program(0, Collections.EMPTY_LIST));
        return extractUnique(librarySnap, domSnap);
    }

    public static Snap.Obj extractUnique(Snap.Obj librarySnap, Snap.Obj domSnap) {
        // Keys for the objects in the library, that are also in the DOM.
        Set<Integer> libraryKeysInDom = getKeysSharedWithDom(librarySnap, domSnap, new HashSet<>());

        Snap.Obj result = extractUnique(librarySnap, domSnap, libraryKeysInDom, new Snap.Obj(), new HashMap<>());

        return result;
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

    public static Snap.Obj getStateDumpFromFile(String path, Program program) throws IOException {
        String jsnapRaw = readFile(path);
        return getStateDump(jsnapRaw, program);
    }

    public static Snap.Obj getStateDump(String jsnapRaw, Program program) {
        // TODO: MarkNatives.
        GsonBuilder builder = new GsonBuilder();
        List<Function> functions = getFunctions(program);
        builder.registerTypeAdapterFactory(new KeyTypeFactory());
        Gson gson = builder.create();
        Snap.StateDump stateDump = gson.fromJson(jsnapRaw, Snap.StateDump.class);
        resolveFunctions(stateDump, functions);
        resolveKeys(stateDump);

        return stateDump.getGlobal();
    }

    public static List<Function> getFunctions(Program program) {
        FunctionExtractor functionExtrator = new FunctionExtractor();
        program.accept(functionExtrator);
        return functionExtrator.getFunctions();
    }

    private static class FunctionExtractor extends NodeTransverse {
        List<Function> functions = new ArrayList<>();
        {
            functions.add(null);
        }
        @Override
        public Void visit(Function function) {
            functions.add(function);
            return super.visit(function);
        }

        public List<Function> getFunctions() {
            return functions;
        }
    }

    public static List<Snap.Value> lookup(Snap.Value value, String key) {
        if (key.isEmpty()) {
            return new ArrayList<>(Arrays.asList(value));
        }
        if (value instanceof Snap.Obj) {
            Snap.Obj obj = (Snap.Obj) value;
            String[] split = key.split("\\.");
            String index = split[0];
            ArrayList<Snap.Value> result = new ArrayList<>();
            for (Snap.Property property : obj.properties) {
                if (property.name.startsWith(index)) {
                    StringBuilder newKey = new StringBuilder();
                    for (int i = 1; i < split.length; i++) {
                        newKey.append(split[i]).append(".");
                    }
                    if (newKey.length() > 0) {
                        newKey.deleteCharAt(newKey.length() - 1);
                    }
                    result.addAll(lookup(property.value, newKey.toString()));
                }
            }

            return result;
        }
        return new ArrayList<>();
    }

    public static List<Map.Entry<String, Snap.Value>> getSubHeap(Map<String,Snap. Value> heap, String searchString) {
        ArrayList<Map.Entry<String, Snap.Value>> entries = new ArrayList<>(getSubHeapMap(heap, searchString).entrySet());
        entries.sort((o1, o2) -> o1.getKey().length() - o2.getKey().length());
        return entries;
    }

    private static Map<String, Snap.Value> getSubHeapMap(Map<String, Snap.Value> heap, String searchString) {
        HashMap<String, Snap.Value> result = new HashMap<>();
        heap.entrySet().stream().filter(entry -> entry.getKey().contains(searchString)).forEach(entry -> {
            result.put(entry.getKey(), entry.getValue());
        });

        return result;
    }

    private static Map<String, Snap.Value> makePrettyHeap(String prefix, Snap.Obj obj, Set<Snap.Obj> seen) {
        HashMap<String, Snap.Value> map = new HashMap<>();
        if (seen.contains(obj)) { // Yep, no equals or hashCode. That is because everything is cyclic, so that stuff doesn't work well.
            return new HashMap<>();
        }
        seen.add(obj);

        obj.properties.forEach(property -> {
            map.put(prefix + property.name, property.value);
        });


        obj.properties.forEach(property -> {
            if (property.value instanceof Snap.Obj) {
                Snap.Obj subValue = (Snap.Obj) property.value;

                if (subValue.env != null) {
                    map.put(prefix + property.name + ".[ENV]", subValue.env);
                    map.putAll(makePrettyHeap(prefix + property.name + ".[ENV].", (Snap.Obj) subValue.env, seen));
                }
                if (subValue.prototype != null) {
                    map.put(prefix + property.name + ".prototype", subValue.prototype);
                    map.putAll(makePrettyHeap(prefix + property.name + ".prototype.", (Snap.Obj) subValue.prototype, seen));
                }

                map.putAll(makePrettyHeap(prefix + property.name + ".", subValue, seen));
            }
        });

        return map;
    }

    private static void resolveFunctions(Snap.StateDump stateDump, List<Function> functions) {
        for (Snap.Obj obj : stateDump.heap) {
            if (obj == null || obj.function == null) {
                continue;
            }
            Snap.Function function = obj.function;
            if (function.type.equals("user")) {
                int id = Integer.parseInt(function.id);
                function.astNode = functions.get(id);
            }
        }
    }

    private static void resolveKeys(Snap.StateDump stateDump) {
        for (int i = 1; i < stateDump.heap.size(); i++) {
            Snap.Obj obj = stateDump.heap.get(i);
            obj.key = i;
        }
        stateDump.heap.forEach(obj -> {
            if (obj == null || obj.properties == null) {
                return;
            }
            obj.properties.forEach(prop -> {
                if (prop.value instanceof Snap.Obj) {
                    int key = ((Snap.Obj) prop.value).key;
                    Snap.Obj real = stateDump.heap.get(key);
                    prop.value = real;
                }
                if (prop.value instanceof Snap.Obj) {
                    Snap.Obj propValue = (Snap.Obj) prop.value;
                    if (propValue.env instanceof Snap.Obj) {
                        int key = ((Snap.Obj) propValue.env).key;
                        Snap.Obj real = stateDump.heap.get(key);
                        propValue.env = real;
                    }
                    if (propValue.prototype instanceof Snap.Obj) {
                        int key = ((Snap.Obj) propValue.prototype).key;
                        Snap.Obj real = stateDump.heap.get(key);
                        propValue.prototype = real;
                    }
                }
            });
        });
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
                        return null;
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
                    // TODO: When does this happen?
                    if (reader.peek() == JsonToken.BOOLEAN) {
                        reader.nextBoolean();
                        key = 0;
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