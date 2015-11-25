package dk.webbies.tscreate.jsnap;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.util.Util;
import dk.webbies.tscreate.paser.AST.BlockStatement;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.paser.AST.Identifier;
import dk.webbies.tscreate.paser.AST.NodeTransverse;

import java.io.*;
import java.util.*;

public class JSNAPUtil {

    public static String getJsnapRaw(String path, Options options, List<String> dependencies) throws IOException {
        return getJsnapRaw(path, options, path + ".jsnap", path, dependencies);
    }

    private static String getEmptyJSNAP(Options options, List<String> dependencies) throws IOException {
        if (options.runtime == Options.Runtime.CHROME) {
            return getJsnapRaw("", options, "onlyDom.jsnap", "lib/selenium", dependencies);
        } else {
            return getJsnapRaw("", options, "onlyDom.jsnap", "lib/jsnap/node_modules/phantomjs/lib/phantom", dependencies);
        }
    }

    private static String getJsnapRaw(String scriptPath, Options options, String cachePath, String checkAgainst, List<String> dependencies) throws IOException {
        String jsnapPath = "lib/jsnap/jsnap.js";
        if (options.createInstances) {
            jsnapPath += " --createInstances";
            cachePath += ".createdInstances";
        }
        for (String dependency : dependencies) {
            jsnapPath += " --dependency " + dependency;
            cachePath += "+" + dependency + "";
        }

        List<File> filesToCheckAgainst = new ArrayList<>();
        if (checkAgainst != null) {
            filesToCheckAgainst.add(new File(checkAgainst));
        }
        dependencies.stream().map(File::new).forEach(filesToCheckAgainst::add);

        if (options.runtime == Options.Runtime.CHROME) {

            String instrumented = Util.getCachedOrRunNode(cachePath + ".instrumented", filesToCheckAgainst, jsnapPath + " --onlyInstrument " + scriptPath);

            return Util.getCachedOrRun(cachePath + ".selinium", filesToCheckAgainst, () -> {
                return SeleniumDriver.executeScript(instrumented);
            });
        } else if (options.runtime == Options.Runtime.NODE || options.runtime == Options.Runtime.PHANTOM) {
            String nodeArgs;
            switch (options.runtime) {
                case PHANTOM: nodeArgs = jsnapPath + " --runtime browser " + scriptPath; break;
                case NODE: nodeArgs = jsnapPath + " --runtime node " + scriptPath; break;
                default:
                    throw new RuntimeException("Dont know runtime: " + options.runtime);
            }
            return Util.getCachedOrRunNode(cachePath, filesToCheckAgainst, nodeArgs);
        } else {
            throw new RuntimeException("Unknwon runtime environment " + options.runtime);
        }
    }

    public static Snap.Obj extractUnique(Snap.Obj librarySnap, Options options, List<String> dependencies) throws IOException {
        FunctionExpression emptyProgram = new FunctionExpression(null, new Identifier(null, ":program"), new BlockStatement(null, Collections.EMPTY_LIST), Collections.EMPTY_LIST);
        Snap.Obj domSnap = JSNAPUtil.getStateDump(JSNAPUtil.getEmptyJSNAP(options, dependencies), emptyProgram);
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
        if (librarySnap.properties == null) {
            librarySnap.properties = Collections.EMPTY_LIST;
        }
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
        if (obj.prototype != null && obj != obj.prototype) {
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
                function.target = getHeapObject(stateDump, (function.target).key);
                if (!function.target.function.type.equals("user")) {
                    throw new RuntimeException();
                }
            }
        }
    }

    private static void resolveKeys(Snap.StateDump stateDump) {
        for (int i = 1; i < stateDump.heap.size(); i++) {
            Snap.Obj obj = stateDump.heap.get(i);
            if (obj == null) {
                continue;
            }
            obj.key = i;
        }
        for (Snap.Obj obj : stateDump.heap) {
            resolveKeys(stateDump, obj);
        }
        List<Snap.Obj> allObjects = JSNAPUtil.getAllObjects(stateDump.getGlobal());
        for (Snap.Obj obj : allObjects) {
            resolveKeys(stateDump, obj);
        }
        allObjects = JSNAPUtil.getAllObjects(stateDump.getGlobal());
        for (Snap.Obj obj : allObjects) {
            resolveKeys(stateDump, obj);
        }
        allObjects = JSNAPUtil.getAllObjects(stateDump.getGlobal());
        for (Snap.Obj obj : allObjects) {
            resolveKeys(stateDump, obj);
        }

    }

    private static void resolveKeys(Snap.StateDump stateDump, Snap.Obj obj) {
        if (obj == null || obj.properties == null) {
            return;
        }
        if (obj.function != null && obj.function.instance != null) {
            int key = obj.function.instance.key;
            obj.function.instance = getHeapObject(stateDump, key);
        }
        if (obj.env != null) {
            obj.env = getHeapObject(stateDump, obj.env.key);
        }
        for (Snap.Property prop : obj.properties) {
            if (prop.value instanceof Snap.Obj) {
                prop.value = getHeapObject(stateDump, ((Snap.Obj) prop.value).key);

                Snap.Obj propValue = (Snap.Obj) prop.value;
                if (propValue.env != null) {
                    propValue.env = getHeapObject(stateDump, (propValue.env).key);
                }
                if (propValue.prototype != null) {
                    propValue.prototype = getHeapObject(stateDump, (propValue.prototype).key);
                }
            }
            if (prop.get != null && prop.get instanceof Snap.Obj) {
                prop.get = getHeapObject(stateDump, ((Snap.Obj) prop.get).key);
            }
            if (prop.set != null && prop.set instanceof Snap.Obj) {
                prop.set = getHeapObject(stateDump, ((Snap.Obj) prop.set).key);
            }
        }
    }

    private static Snap.Obj getHeapObject(Snap.StateDump stateDump, int key) {
        Snap.Obj result = stateDump.heap.get(key);
        if (result == null) { // Happens in Chrome.
            Snap.Obj obj = new Snap.Obj();
            obj.properties = new ArrayList<>();
            stateDump.heap.set(key, obj);
            return obj;
        }
        return result;
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
                        if (reader.peek() == JsonToken.NUMBER) {
                            key = reader.nextInt();
                        } else if (reader.peek() == JsonToken.STRING) {
                            key = Integer.parseInt(reader.nextString());
                        } else if (reader.peek() == JsonToken.BEGIN_OBJECT){
                            // FIXME This happens in three.js, it is an object with __jsnapHiddenProp__ values defined on it. Properly something to do with the ES6 getters and setters.
                            // There are also some closures, where env.properties == null.
                            reader.beginObject();
                            reader.nextName();
                            reader.nextBoolean();
                            reader.nextName();
                            reader.nextString();
                            reader.endObject();

                            key = 1;
                        } else {
                            throw new RuntimeException("Did really not expect " + reader.peek());
                        }
                    }

                    reader.endObject();

                    Snap.Obj obj = new Snap.Obj();
                    obj.key = key;
                    return (T) obj;
                }
            };
        }
    }

    public static List<Snap.Obj> getAllObjects(Snap.Obj root) {
        return getAllObjects(root, new HashSet<>());
    }

    private static List<Snap.Obj> getAllObjects(Snap.Obj obj, Set<Snap.Obj> seen) {
        if (seen.contains(obj)) {
            return Collections.EMPTY_LIST;
        }

        seen.add(obj);

        ArrayList<Snap.Obj> result = new ArrayList<>();
        result.add(obj);
        if (obj.properties != null) {
            for (Snap.Property property : obj.properties) {
                addAllPropertyFunctions(seen, result, property);
            }
        }

        if (obj.prototype != null && obj.prototype.properties != null) {
            for (Snap.Property property : obj.prototype.properties) {
                addAllPropertyFunctions(seen, result, property);
            }
        }

        if (obj.env != null) {
            result.addAll(getAllObjects(obj.env, seen));
        }

        if (obj.function != null && obj.function.instance != null) {
            result.addAll(getAllObjects(obj.function.instance, seen));
        }

        return result;
    }

    private static void addAllPropertyFunctions(Set<Snap.Obj> seen, ArrayList<Snap.Obj> result, Snap.Property property) {
        if (property.value != null && property.value instanceof Snap.Obj) {
            result.addAll(getAllObjects((Snap.Obj) property.value, seen));
        }
        if (property.set != null && !(property.set instanceof Snap.UndefinedConstant)) {
            result.addAll(getAllObjects((Snap.Obj) property.set, seen));
        }
        if (property.get != null && !(property.get instanceof Snap.UndefinedConstant)) {
            result.addAll(getAllObjects((Snap.Obj) property.get, seen));
        }
    }
}