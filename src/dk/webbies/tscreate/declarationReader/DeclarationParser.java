package dk.webbies.tscreate.declarationReader;

import dk.brics.tajs.envspec.typescript.SpecReader;
import dk.brics.tajs.envspec.typescript.types.*;
import dk.webbies.tscreate.Util;
import dk.webbies.tscreate.jsnapconvert.Snap;

import java.io.IOException;
import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class DeclarationParser {
    public static void markNatives(Snap.Obj global, Environment env) {
        try {
            // TODO: Cache the JSON.
            SpecReader spec = new SpecReader(Util.runNodeScript("lib/ts-type-reader/src/CLI.js --env " + env.cliArgument));

            markNamedTypes((SpecReader.Node) spec.getNamedTypes(), "");


            while (global != null) {
                spec.getGlobal().accept(new MarkNativesVisitor(), global);
                global = global.prototype;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void markNamedTypes(SpecReader.Node namedTypes, String prefix) {
        for (Map.Entry<String, SpecReader.TypeNameTree> entry : namedTypes.getChildren().entrySet()) {
            String name = prefix + entry.getKey();
            SpecReader.TypeNameTree type = entry.getValue();
            if (type instanceof SpecReader.Leaf) {
                SpecReader.Leaf leaf = (SpecReader.Leaf) type;
                if (leaf.getType() instanceof InterfaceType) {
                    ((InterfaceType) leaf.getType()).setName(name);
                } else if (leaf.getType() instanceof GenericType) {
                    ((GenericType) leaf.getType()).setName(name);
                } else {
                    throw new RuntimeException("Dont handle marking " + leaf.getType().getClass().getName() + " yet!");
                }
            } else {
                markNamedTypes((SpecReader.Node) type, name + ".");
            }
        }
    }

    private static class MarkNativesVisitor implements TypeVisitorWithArgument<Void, Snap.Obj> {
        Set<Type> seen = new HashSet<>();
        @Override
        public Void visit(AnonymousType t, Snap.Obj obj) {
            if (obj.function != null) {
                throw new UnsupportedOperationException("This is useless");
            } else {
                return null;
            }
        }

        @Override
        public Void visit(GenericType t, Snap.Obj obj) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);
            if (obj.function != null) {
                obj.function.callSignatures.addAll(t.getDeclaredCallSignatures());
                obj.function.constructorSignatures.addAll(t.getDeclaredConstructSignatures());
            }
            for (Map.Entry<String, Type> entry : t.getDeclaredProperties().entrySet()) {
                String key = entry.getKey();
                Type value = entry.getValue();
                if (obj.getProperty(key) != null && obj.getProperty(key).value instanceof Snap.Obj) {
                    value.accept(this, (Snap.Obj) obj.getProperty(key).value);
                }
            }
            return null;
        }

        @Override
        public Void visit(InterfaceType t, Snap.Obj obj) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);
            if (obj.function != null) {
                obj.function.callSignatures.addAll(t.getDeclaredCallSignatures());
                obj.function.constructorSignatures.addAll(t.getDeclaredConstructSignatures());
            }
            for (Map.Entry<String, Type> entry : t.getDeclaredProperties().entrySet()) {
                String key = entry.getKey();
                Type value = entry.getValue();
                if (obj.getProperty(key) != null && obj.getProperty(key).value instanceof Snap.Obj) {
                    value.accept(this, (Snap.Obj) obj.getProperty(key).value);
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
            throw new UnsupportedOperationException("none");
        }

        @Override
        public Void visit(TupleType t, Snap.Obj obj) {
            throw new UnsupportedOperationException("of");
        }

        @Override
        public Void visit(UnionType t, Snap.Obj obj) {
            throw new UnsupportedOperationException("these");
        }

        @Override
        public Void visit(UnresolvedType t, Snap.Obj obj) {
            throw new UnsupportedOperationException("happens");
        }

        @Override
        public Void visit(TypeParameterType t, Snap.Obj obj) {
            throw new UnsupportedOperationException("in");
        }

        @Override
        public Void visit(SymbolType t, Snap.Obj obj) {
            throw new UnsupportedOperationException("this");
        }

        @Override
        public Void visit(ClassType t, Snap.Obj obj) {
            throw new UnsupportedOperationException("visitor.");
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
