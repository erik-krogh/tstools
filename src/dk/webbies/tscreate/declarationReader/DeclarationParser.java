package dk.webbies.tscreate.declarationReader;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dk.au.cs.casa.typescript.SpecReader;
import dk.au.cs.casa.typescript.types.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static dk.webbies.tscreate.jsnap.Snap.Obj;
import static dk.webbies.tscreate.jsnap.Snap.Value;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class DeclarationParser {
    public static NativeClassesMap parseNatives(Obj global, Environment env, List<String> dependencyDeclarations, HashMap<Obj, LibraryClass> libraryClasses) {
        SpecReader spec = getTypeSpecification(env, dependencyDeclarations);

        return parseNatives(global, libraryClasses, spec);
    }

    public static NativeClassesMap parseNatives(Obj global, HashMap<Obj, LibraryClass> libraryClasses, SpecReader spec) {
        Map<Type, String> typeNames = new HashMap<>();

        markNamedTypes((SpecReader.Node) spec.getNamedTypes(), "", typeNames);

        Map<Obj, Type> prototypes = new HashMap<>();
        HashMap<Obj, String> namedObjects = new HashMap<>();
        Obj globalProto = global;
        while (globalProto != null) {
            spec.getGlobal().accept(new MarkNativesVisitor(prototypes, typeNames, namedObjects), globalProto);
            if (globalProto == globalProto.prototype) {
                break;
            }
            globalProto = globalProto.prototype;
        }

        for (Map.Entry<Type, String> entry : typeNames.entrySet()) {
            String name = entry.getValue();
            Type type = entry.getKey();

            if (global.getProperty(name) != null && global.getProperty(name).value instanceof Obj) {
                Obj obj = (Obj) global.getProperty(name).value;
                if (obj.getProperty("prototype") != null && obj.getProperty("prototype").value instanceof Obj) {
                    Obj prototype = (Obj) obj.getProperty("prototype").value;
                    if (!prototypes.containsKey(prototype)) {
                        prototypes.put(prototype, type);
                    }
                }
            }
        }

        return new NativeClassesMap(typeNames, prototypes, namedObjects, global, libraryClasses);
    }

    public static SpecReader getTypeSpecification(Environment env, String... declarationFilePaths) {
        return getTypeSpecification(env, Arrays.asList(declarationFilePaths));
    }

    public static SpecReader getTypeSpecification(Environment env, Collection<String> dependencies, String declarationFilePath) {
        ArrayList<String> specs = new ArrayList<>();
        specs.addAll(dependencies);
        specs.add(declarationFilePath);
        return getTypeSpecification(env, specs);
    }

    public static SpecReader getTypeSpecification(Environment env, Collection<String> declarationFilePaths) {
        String runString = "lib/ts-type-reader/src/CLI.js --env " + env.cliArgument;
        for (String declarationFile : declarationFilePaths) {
            runString += " \"" + declarationFile + "\"";
        }

        String cachePath = "declaration-" + env.cliArgument + "-" + runString.hashCode() + ".json";

        List<File> toCheckAgainst = new ArrayList<>(Arrays.asList(new File("lib/ts-type-reader")));
        declarationFilePaths.stream().map(File::new).forEach(toCheckAgainst::add);

        try {
            String specification = Util.getCachedOrRunNode(cachePath, toCheckAgainst, runString);
            return new SpecReader(specification.split("\\n")[1]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void markNamedTypes(SpecReader.Node namedTypes, String prefix, Map<Type, String> typeNames) {
        for (Map.Entry<String, SpecReader.TypeNameTree> entry : namedTypes.getChildren().entrySet()) {
            String name = prefix + entry.getKey();
            SpecReader.TypeNameTree type = entry.getValue();
            if (type instanceof SpecReader.Leaf) {
                SpecReader.Leaf leaf = (SpecReader.Leaf) type;
                if (leaf.getType() instanceof InterfaceType) {
                    typeNames.put(leaf.getType(), name);
                } else if (leaf.getType() instanceof GenericType) {
                    typeNames.put(leaf.getType(), name);
                } else {
                    throw new RuntimeException("I don't handle marking " + leaf.getType().getClass().getName() + " yet!");
                }
            } else {
                markNamedTypes((SpecReader.Node) type, name + ".", typeNames);
            }
        }
    }

    private static class MarkNativesVisitor implements TypeVisitorWithArgument<Void, Snap.Obj> {
        Set<Type> seen = new HashSet<>();
        private Map<Snap.Obj, Type> prototypes;
        private Map<Type, String> typeNames;
        private HashMap<Obj, String> namedObjects;

        public MarkNativesVisitor(Map<Obj, Type> prototypes, Map<Type, String> typeNames, HashMap<Obj, String> namedObjects) {
            this.prototypes = prototypes;
            this.typeNames = typeNames;
            this.namedObjects = namedObjects;
        }

        @Override
        public Void visit(AnonymousType t, Obj obj) {
            if (obj.function != null) {
                throw new UnsupportedOperationException("This is useless");
            } else {
                return null;
            }
        }

        private Value lookUp(Obj obj, String key) {
            if (obj == null) {
                return null;
            } else if (obj.getProperty(key) != null) {
                return obj.getProperty(key).value;
            } else if (obj.prototype == obj) { // Happens with Chrome
                return null;
            } else {
                return lookUp(obj.prototype, key);
            }
        }

        @SuppressWarnings("Duplicates")
        @Override
        public Void visit(GenericType t, Obj obj) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);
            if (typeNames.containsKey(t)) {
                this.namedObjects.put(obj, typeNames.get(t));
            }
            if (obj.function != null) {
                obj.function.type = "native";
                obj.function.callSignatures.addAll(t.getDeclaredCallSignatures());
                obj.function.constructorSignatures.addAll(t.getDeclaredConstructSignatures());
                if (!obj.function.constructorSignatures.isEmpty() && obj.getProperty("prototype").value != null) {
                    this.prototypes.put((Obj)obj.getProperty("prototype").value, t);
                }
            }
            for (Map.Entry<String, Type> entry : t.getDeclaredProperties().entrySet()) {
                String key = entry.getKey();
                Type value = entry.getValue();
                Value propValue = lookUp(obj, key);
                if (propValue != null && propValue instanceof Obj) {
                    value.accept(this, (Obj) propValue);
                }
            }
            return null;
        }

        @SuppressWarnings("Duplicates")
        @Override
        public Void visit(InterfaceType t, Obj obj) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);
            if (typeNames.containsKey(t)) {
                this.namedObjects.put(obj, typeNames.get(t));
            }
            if (obj.function != null) {
                obj.function.type = "native";
                obj.function.callSignatures.addAll(t.getDeclaredCallSignatures());
                obj.function.constructorSignatures.addAll(t.getDeclaredConstructSignatures());
                if (!obj.function.constructorSignatures.isEmpty() && obj.getProperty("prototype").value != null) {
                    this.prototypes.put((Obj)obj.getProperty("prototype").value, t);
                }
            }
            for (Map.Entry<String, Type> entry : t.getDeclaredProperties().entrySet()) {
                String key = entry.getKey();
                Type value = entry.getValue();
                Value propValue = lookUp(obj, key);
                if (propValue != null && propValue instanceof Obj) {
                    value.accept(this, (Obj) propValue);
                }
            }

            return null;
        }

        @Override
        public Void visit(ReferenceType t, Obj obj) {
            t.getTarget().accept(this, obj);
            return null;
        }

        @Override
        public Void visit(SimpleType t, Obj obj) {
            return null;
        }

        @Override
        public Void visit(TupleType t, Obj obj) {
            return null;
        }

        @Override
        public Void visit(UnionType t, Obj obj) {
            return null;
        }

        @Override
        public Void visit(UnresolvedType t, Obj obj) {
            return null;
        }

        @Override
        public Void visit(TypeParameterType t, Obj obj) {
            return null;
        }

        @Override
        public Void visit(SymbolType t, Obj obj) {
            return null;
        }

        @Override
        public Void visit(ClassType t, Obj obj) {
            return null;
        }
    }

    public static final class NativeClassesMap {
        private final BiMap<Type, String> typeNames;
        private final BiMap<Snap.Obj, String> classNames = HashBiMap.create();
        private BiMap<Obj, String> namedObjects;
        private final Obj global;
        private HashMap<Obj, LibraryClass> libraryClasses;

        public NativeClassesMap(Map<Type, String> typeNames, Map<Obj, Type> nativeClasses, HashMap<Obj, String> namedObjects, Obj global, HashMap<Obj, LibraryClass> libraryClasses) {
            this.libraryClasses = libraryClasses;
            this.namedObjects = HashBiMap.create(namedObjects);
            this.global = global;
            this.typeNames = HashBiMap.create(typeNames);
            nativeClasses.forEach((prototype, type) -> {
                if (typeNames.containsKey(type)) {
                    classNames.put(prototype, typeNames.get(type));
                }
            });
        }

        public String nameFromType(Type type) {
            return typeNames.get(type);
        }

        public String nameFromPrototype(Obj prototype) {
            String result = classNames.get(prototype);
            if (result != null) {
                return result;
            }
            Obj constructor = this.libraryClasses.get(prototype).getConstructor();
            if (constructor != null && constructor.function.constructorSignatures != null && !constructor.function.constructorSignatures.isEmpty()) {
                Type type = constructor.function.constructorSignatures.get(0).getResolvedReturnType();
                return nameFromType(type);
            }
            return null;
        }

        public Obj prototypeFromName(String name) {
            Obj result = classNames.inverse().get(name);
            if (result != null) {
                return result;
            } else {
                Snap.Property prop = global.getProperty(name);
                if (prop != null && prop.value instanceof Snap.Obj) {
                    Snap.Obj obj = (Snap.Obj) prop.value;
                    if (obj.getProperty("prototype") != null && obj.getProperty("prototype").value instanceof Snap.Obj) {
                        return (Snap.Obj) obj.getProperty("prototype").value;
                    }
                }
            }
            return null;
        }

        public Type typeFromName(String name) {
            return typeNames.inverse().get(name);
        }

        public String nameFromObject(Obj obj) {
            return this.namedObjects.get(obj);
        }

        public Obj objectFromName(String name) {
            return this.namedObjects.inverse().get(name);
        }

        public Set<String> getNames() {
            return this.typeNames.values();
        }
    }

    public enum Environment {
        ES5Core("es5", 5),
        ES5DOM("es5-dom", 5),
        ES6Core("es6", 6),
        ES6DOM("es6-dom", 6);

        private final String cliArgument;
        public final int ESversion;

        Environment(String cliArgument, int ESversion) {
            this.cliArgument = cliArgument;
            this.ESversion = ESversion;
        }


    }
}
