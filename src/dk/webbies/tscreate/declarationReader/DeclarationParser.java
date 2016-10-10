package dk.webbies.tscreate.declarationReader;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dk.au.cs.casa.typescript.SpecReader;
import dk.au.cs.casa.typescript.types.*;
import dk.webbies.tscreate.analysis.declarations.types.NamedObjectType;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.util.Util;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.jsnap.JSNAPUtil.lookupRecursive;
import static dk.webbies.tscreate.jsnap.Snap.Obj;
import static dk.webbies.tscreate.jsnap.Snap.Value;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class DeclarationParser {
    public static NativeClassesMap parseNatives(Obj global, Environment env, List<String> dependencyDeclarations, Map<Obj, LibraryClass> libraryClasses, Obj emptySnap) {
        SpecReader spec = getTypeSpecification(env, dependencyDeclarations);

        return parseNatives(global, libraryClasses, spec, emptySnap);
    }

    public static NativeClassesMap parseNatives(Obj global, Map<Obj, LibraryClass> libraryClasses, SpecReader spec, Obj emptySnap) {

        fixBind(global);

        Map<Type, String> typeNames = getTypeNamesMap(spec);

        Map<Obj, Type> prototypes = new HashMap<>();
        Map<Obj, String> namedObjects = new HashMap<>();
        {
            Obj globalProto = global;
            while (globalProto != null) {
                spec.getGlobal().accept(new MarkNativesVisitor(prototypes, typeNames, namedObjects), globalProto);
                if (globalProto == globalProto.prototype) {
                    break;
                }
                globalProto = globalProto.prototype;
            }
        }

        /*{
            Map<Obj, Type> prototypesInEmpty = new HashMap<>();
            Map<Obj, String> namedObjectsInEmpty = new HashMap<>();
            Obj globalProto = emptySnap;
            while (globalProto != null) {
                spec.getGlobal().accept(new MarkNativesVisitor(prototypesInEmpty, typeNames, namedObjectsInEmpty), globalProto);
                if (globalProto == globalProto.prototype) {
                    break;
                }
                globalProto = globalProto.prototype;
            }
            namedObjects = namedObjects.entrySet().stream().filter(entry -> {
                return namedObjectsInEmpty.values().contains(entry.getValue());
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }*/

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

    public static Map<Type, String> getTypeNamesMap(SpecReader spec) {
        Map<Type, String> typeNames = new HashMap<>();
        markNamedTypes((SpecReader.Node) spec.getNamedTypes(), "", typeNames);
        return typeNames;
    }

    private static void fixBind(Obj global) {
        String id = "Function.prototype.bind";
        Obj closure = (Obj) lookupRecursive(global, id);
        closure.function.id = id;
        closure.function.type = "native";
    }

    public static SpecReader getTypeSpecification(Environment env, Collection<String> dependencies, String declarationFilePath) {
        ArrayList<String> specs = new ArrayList<>();
        specs.addAll(dependencies);
        specs.add(declarationFilePath);
        return getTypeSpecification(env, specs);
    }

    public static SpecReader getTypeSpecification(Environment env, Collection<String> declarationFilePaths) {
        String runString = "node_modules/ts-type-reader/src/CLI.js --env " + env.cliArgument;
        for (String declarationFile : declarationFilePaths) {
            runString += " \"" + declarationFile + "\"";
        }

        String cachePath = "declaration-" + env.cliArgument + "-" + runString.hashCode() + ".json";

        List<File> toCheckAgainst = new ArrayList<>(Arrays.asList(new File("node_modules/ts-type-reader")));
        declarationFilePaths.stream().map(File::new).forEach(toCheckAgainst::add);

        String specification;
        try {
            specification = Util.getCachedOrRunNode(cachePath, toCheckAgainst, runString);
            return new SpecReader(specification.split("\\n")[specification.split("\\n").length - 1]);
        } catch (Exception e) {
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
        private Map<Obj, String> namedObjects;

        public MarkNativesVisitor(Map<Obj, Type> prototypes, Map<Type, String> typeNames, Map<Obj, String> namedObjects) {
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

        @Override
        public Void visit(GenericType t, Obj obj) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);
            visitInterface(t, obj);
            return null;
        }

        @Override
        public Void visit(InterfaceType t, Obj obj) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);
            visitInterface(t, obj);
            return null;
        }

        private void visitInterface(Type t, Obj obj) {
            if (typeNames.containsKey(t)) {
                String name = typeNames.get(t);
                this.namedObjects.put(obj, name);
            }

            SyntheticInterface type = new SyntheticInterface(getWithBaseTypes(t));

            if (obj.function != null) {
                obj.function.type = "native";
                if (obj.function.id == null) {
                    obj.function.id = "";
                }
                obj.function.callSignatures.addAll(type.getDeclaredCallSignatures());
                obj.function.constructorSignatures.addAll(type.getDeclaredConstructSignatures());
                if (!obj.function.constructorSignatures.isEmpty() && obj.getProperty("prototype") != null && obj.getProperty("prototype").value != null) {
                    this.prototypes.put((Obj)obj.getProperty("prototype").value, t);
                }
            }
            for (Map.Entry<String, Type> entry : type.getDeclaredProperties().entries()) {
                String key = entry.getKey();
                Type value = entry.getValue();
                Value propValue = lookUp(obj, key);
                if (propValue != null && propValue instanceof Obj) {
                    value.accept(this, (Obj) propValue);
                }
            }
            if (obj.getProperty("prototype") != null) {
                Value prototype = obj.getProperty("prototype").value;
                for (Signature constructor : type.getDeclaredConstructSignatures()) {
                    constructor.getResolvedReturnType().accept(this, (Obj)prototype);
                }
            }

        }

        private static final class SyntheticInterface {
            private Set<InterfaceType> types;

            public SyntheticInterface(Set<InterfaceType> types) {
                this.types = types;
            }

            public List<Signature> getDeclaredCallSignatures() {
                ArrayList<Signature> result = new ArrayList<>();
                for (InterfaceType type : types) {
                    result.addAll(type.getDeclaredCallSignatures());
                }

                return result;
            }

            public List<Signature> getDeclaredConstructSignatures() {
                ArrayList<Signature> result = new ArrayList<>();
                for (InterfaceType type : types) {
                    result.addAll(type.getDeclaredConstructSignatures());
                }

                return result;
            }

            public Multimap<String, Type> getDeclaredProperties() {
                HashMultimap<String, Type> result = HashMultimap.create();
                for (InterfaceType type : types) {
                    for (Map.Entry<String, Type> entry : type.getDeclaredProperties().entrySet()) {
                        result.put(entry.getKey(), entry.getValue());
                    }
                }

                return result;
            }
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
        private final Set<NamedObjectType> nativeDeclarationTypes;
        private BiMap<Obj, String> namedObjects;
        private final Obj global;
        private Map<Obj, LibraryClass> libraryClasses;

        public NativeClassesMap(Map<Type, String> typeNames, Map<Obj, Type> nativeClasses, Map<Obj, String> namedObjects, Obj global, Map<Obj, LibraryClass> libraryClasses) {
            this.libraryClasses = libraryClasses;
            this.namedObjects = HashBiMap.create(namedObjects);
            this.global = global;
            this.typeNames = HashBiMap.create(typeNames);
            nativeClasses.forEach((prototype, type) -> {
                if (typeNames.containsKey(type)) {
                    classNames.put(prototype, typeNames.get(type));
                }
            });
            this.nativeDeclarationTypes = this.typeNames.values().stream().filter(name -> !name.startsWith("MS")).map(name -> new NamedObjectType(name, false)).collect(Collectors.toSet());
        }

        public String nameFromType(Type type) {
            if (!typeNames.containsKey(type) && type instanceof ReferenceType) {
                type = ((ReferenceType) type).getTarget();
            }
            return typeNames.get(type);
        }

        public Set<NamedObjectType> getNativeDeclarationTypes() {
            return nativeDeclarationTypes;
        }

        public Set<Type> nativeTypes() {
            return typeNames.keySet();
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

        private Map<String, Set<String>> baseNameCache = new HashMap<>();
        public Set<String> getBaseNames(String name) {
            assert name != null;
            if (baseNameCache.containsKey(name)) {
                return baseNameCache.get(name);
            }
            Type type = typeFromName(name);
            if (type == null) {
                return Collections.EMPTY_SET;
            }
            HashSet<String> result = new HashSet<>();
            if (type instanceof GenericType) {
                type = ((GenericType) type).toInterface();
            }
            if (!(type instanceof InterfaceType)) {
                throw new RuntimeException();
            }
            for (Type baseType : ((InterfaceType) type).getBaseTypes()) {
                String baseName = nameFromType(baseType);
                result.add(baseName);
                result.addAll(getBaseNames(baseName));
            }
            result.remove(null);
            baseNameCache.put(name, result);
            return result;
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

        public Set<String> getNativeTypeNames() {
            return this.typeNames.values().stream().filter(name -> !name.startsWith("MS")).collect(Collectors.toSet());
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

    public static Set<InterfaceType> getWithBaseTypes(Type t) {
        return getWithBaseTypes(t, new HashSet<>());
    }

    private static Set<InterfaceType> getWithBaseTypes(Type t, Set<InterfaceType> acc) {
        List<Type> baseTypes;
        if (t instanceof ReferenceType) {
            t = ((ReferenceType) t).getTarget();
        }

        if (t instanceof InterfaceType) {
            acc.add((InterfaceType) t);
            baseTypes = ((InterfaceType) t).getBaseTypes();
        } else if (t instanceof GenericType) {
            acc.add(((GenericType) t).toInterface());
            baseTypes = ((GenericType) t).getBaseTypes();
        } else {
            return acc;
        }
        if (baseTypes == null) {
            return acc;
        }
        for (Type type : baseTypes) {
            if (!acc.contains(type)) {
                getWithBaseTypes(type, acc);
            }
        }
        return acc;
    }
}
