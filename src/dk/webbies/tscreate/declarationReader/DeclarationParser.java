package dk.webbies.tscreate.declarationReader;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dk.au.cs.casa.typescript.SpecReader;
import dk.au.cs.casa.typescript.types.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class DeclarationParser {
    public static NativeClassesMap markNatives(Snap.Obj global, Environment env) {
        SpecReader spec = getTypeSpecification(env);

        Map<Type, String> typeNames = new HashMap<>();

        markNamedTypes((SpecReader.Node) spec.getNamedTypes(), "", typeNames);

        Map<Snap.Obj, Type> prototypes = new HashMap<>();
        while (global != null) {
            spec.getGlobal().accept(new MarkNativesVisitor(prototypes), global);
            global = global.prototype;
        }

        return new NativeClassesMap(typeNames, prototypes);
    }

    public static SpecReader getTypeSpecification(Environment env, String... declarationFilePaths) {
        String runString = "lib/ts-type-reader/src/CLI.js --env " + env.cliArgument;
        for (String declarationFile : declarationFilePaths) {
            runString += " \"" + declarationFile + "\"";
        }

        String cachePath = "declaration-" + env.cliArgument + "-" + runString.hashCode() + ".json";

        try {
            String specification = Util.getCachedOrRun(cachePath, new File("lib/ts-type-reader"), runString);
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
        private Map<Snap.Obj, Type> nativeClasses;

        public MarkNativesVisitor(Map<Snap.Obj, Type> nativeClasses) {
            this.nativeClasses = nativeClasses;
        }

        @Override
        public Void visit(AnonymousType t, Snap.Obj obj) {
            if (obj.function != null) {
                throw new UnsupportedOperationException("This is useless");
            } else {
                return null;
            }
        }

        private Snap.Value lookUp(Snap.Obj obj, String key) {
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
        public Void visit(GenericType t, Snap.Obj obj) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);
            if (obj.function != null) {
                obj.function.type = "native";
                obj.function.callSignatures.addAll(t.getDeclaredCallSignatures());
                obj.function.constructorSignatures.addAll(t.getDeclaredConstructSignatures());
                if (!obj.function.constructorSignatures.isEmpty() && obj.getProperty("prototype").value != null) {
                    this.nativeClasses.put((Snap.Obj)obj.getProperty("prototype").value, t);
                }
            }
            for (Map.Entry<String, Type> entry : t.getDeclaredProperties().entrySet()) {
                String key = entry.getKey();
                Type value = entry.getValue();
                Snap.Value propValue = lookUp(obj, key);
                if (propValue != null && propValue instanceof Snap.Obj) {
                    value.accept(this, (Snap.Obj) propValue);
                }
            }
            return null;
        }

        @SuppressWarnings("Duplicates")
        @Override
        public Void visit(InterfaceType t, Snap.Obj obj) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);
            if (obj.function != null) {
                obj.function.type = "native";
                obj.function.callSignatures.addAll(t.getDeclaredCallSignatures());
                obj.function.constructorSignatures.addAll(t.getDeclaredConstructSignatures());
                if (!obj.function.constructorSignatures.isEmpty() && obj.getProperty("prototype").value != null) {
                    this.nativeClasses.put((Snap.Obj)obj.getProperty("prototype").value, t);
                }
            }
            for (Map.Entry<String, Type> entry : t.getDeclaredProperties().entrySet()) {
                String key = entry.getKey();
                Type value = entry.getValue();
                Snap.Value propValue = lookUp(obj, key);
                if (propValue != null && propValue instanceof Snap.Obj) {
                    value.accept(this, (Snap.Obj) propValue);
                }
            }

            return null;
        }

        @Override
        public Void visit(ReferenceType t, Snap.Obj obj) {
            t.getTarget().accept(this, obj);
            return null;
        }

        @Override
        public Void visit(SimpleType t, Snap.Obj obj) {
            return null;
        }

        @Override
        public Void visit(TupleType t, Snap.Obj obj) {
            return null;
        }

        @Override
        public Void visit(UnionType t, Snap.Obj obj) {
            return null;
        }

        @Override
        public Void visit(UnresolvedType t, Snap.Obj obj) {
            return null;
        }

        @Override
        public Void visit(TypeParameterType t, Snap.Obj obj) {
            return null;
        }

        @Override
        public Void visit(SymbolType t, Snap.Obj obj) {
            return null;
        }

        @Override
        public Void visit(ClassType t, Snap.Obj obj) {
            return null;
        }
    }

    public static final class NativeClassesMap {
        private final BiMap<Type, String> typeNames;
        private final Map<Snap.Obj, String> classNames = new HashMap<>();

        public NativeClassesMap(Map<Type, String> typeNames, Map<Snap.Obj, Type> nativeClasses) {
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

        public String nameFromPrototype(Snap.Obj prototype) {
            return classNames.get(prototype);
        }

        public Type typeFromName(String name) {
            return typeNames.inverse().get(name);
        }
    }

    public enum Environment {
        ES5Core("es5"),
        ES5DOM("es5-dom"),
        ES6Core("es6"),
        ES6DOM("es6-dom");

        private final String cliArgument;
        Environment(String cliArgument) {
            this.cliArgument = cliArgument;
        }
    }
}
