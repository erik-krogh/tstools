package dk.webbies.tscreate.analysis.methods.old.analysis.unionFind;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.au.cs.casa.typescript.types.Signature;
import dk.webbies.tscreate.analysis.HeapValueFactory;
import dk.webbies.tscreate.analysis.NativeTypeFactory;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.analysis.methods.unionRecursively.DumbPrimitiveFactory;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class HeapValueNode extends ObjectNode {
    public final Snap.Value value;

    private HeapValueNode(Snap.Value value, Factory factory, UnionFindSolver solver) {
        super(solver);
        this.value = value;
        factory.cache.put(value, this);
        if (value instanceof Snap.Obj) {
            Snap.Obj obj = (Snap.Obj) value;
            if (obj.properties != null) {
                for (Snap.Property property : obj.properties) {
                    addField(property.name, solver.union(factory.fromProperty(property), factory.primitivesFactory.nonVoid()));
                }
            }
        }
    }

    public static class Factory implements HeapValueFactory {
        private final Snap.Obj globalObject;
        private final Map<Snap.Value, HeapValueNode> cache = new HashMap<>();
        private final PrimitiveNode.Factory primitivesFactory;
        private final UnionFindSolver solver;
        private final NativeTypeFactory functionNodeFactory;
        private final Multimap<Snap.Obj, UnionNode> functionCache = ArrayListMultimap.create();
        private final Map<Snap.Obj, LibraryClass> libraryClasses;

        private final TypeAnalysis typeAnalysis;

        public Factory(Snap.Obj globalObject, UnionFindSolver solver, Map<Snap.Obj, LibraryClass> libraryClasses, TypeAnalysis typeAnalysis) {
            this.libraryClasses = libraryClasses;
            this.globalObject = globalObject;
            this.typeAnalysis = typeAnalysis;
            this.primitivesFactory = new DumbPrimitiveFactory(solver, globalObject);
            this.solver = solver;
            this.functionNodeFactory = new NativeTypeFactory(primitivesFactory, solver, typeAnalysis.getNativeClasses());
        }

        public UnionNode fromProperty(Snap.Property property) {
            if (property.value == null) {
                if (property.get == null || property.set == null) {
                    return new EmptyNode(solver); // Happens once in a while.
                }
                UnionNode getter = new EmptyNode(solver);
                if (!(property.get instanceof Snap.UndefinedConstant)) {
                    getter = getGetterSetterNode((Snap.Obj) property.get).returnNode;
                }
                UnionNode setter = new EmptyNode(solver);
                if (!(property.set instanceof Snap.UndefinedConstant)) {
                    FunctionNode setterFunctionNode = getGetterSetterNode((Snap.Obj) property.set);
                    if (!setterFunctionNode.arguments.isEmpty()) {
                        setter = setterFunctionNode.arguments.get(0);
                    }
                }
                return solver.union(getter, setter);
            } else {
                return fromValue(property.value);
            }
        }

        public UnionNode fromValue(Snap.Value value) {
            if (value == null) {
                return new EmptyNode(solver);
            }
            UnionNode primitive = getPrimitiveValue(value, primitivesFactory);
            if (primitive != null) {
                return primitive;
            }

            List<UnionNode> result = new ArrayList<>();
            ObjectNode objectNode = new ObjectNode(solver);
            result.add(objectNode);

            Snap.Obj obj = (Snap.Obj) value;
            if (obj.prototype != null) {
                LibraryClass libraryClass = libraryClasses.get(obj.prototype);
                result.add(new HasPrototypeNode(solver, obj.prototype));
                if (libraryClass != null && !libraryClass.isNativeClass()) {
                    solver.union(libraryClass.getNewThisNode(solver), objectNode);
                }
            }

            if (this.typeAnalysis.getNativeClasses().nameFromObject(obj) != null) {
                objectNode.setTypeName(this.typeAnalysis.getNativeClasses().nameFromObject(obj));
            }

            if (obj.function != null) {
                result.addAll(getFunctionNode(obj));
            }

            if (cache.containsKey(value)) {
                result.add(cache.get(value));
            } else {
                result.add(new HeapValueNode(value, this, solver));
            }
            return solver.union(result);
        }

        @Override
        public PrimitiveNode.Factory getPrimitivesFactory() {
            return primitivesFactory;
        }

        private Collection<UnionNode> getFunctionNode(Snap.Obj obj) {
            if (functionCache.containsKey(obj)) {
                return functionCache.get(obj);
            }
            List<UnionNode> result = new ArrayList<>();
            if (obj.function.astNode != null) {
                FunctionNode functionNode = FunctionNode.create(obj, solver);
                result.add(functionNode);
            } else {
                ArrayList<Signature> signatures = new ArrayList<>(obj.function.callSignatures);
                signatures.addAll(obj.function.constructorSignatures);
                result.addAll(signatures.stream().map(functionNodeFactory::fromSignature).collect(Collectors.toList()));
            }
            functionCache.putAll(obj, result);
            return result;
        }

        private UnionNode getPrimitiveValue(Snap.Value value, PrimitiveNode.Factory primitivesBuilder) {
            if (value instanceof Snap.BooleanConstant) {
                return primitivesBuilder.bool();
            }
            if (value instanceof Snap.NumberConstant) {
                return primitivesBuilder.number();
            }
            if (value instanceof Snap.StringConstant) {
                return primitivesBuilder.string();
            }
            if (value instanceof Snap.UndefinedConstant) {
                return primitivesBuilder.undefined();
            }
            if (value instanceof Snap.NullConstant) {
                return primitivesBuilder.undefined();
            }
            if (value instanceof Snap.Obj) {
                return null;
            }
            return null;
        }

        @SuppressWarnings("Duplicates")
        private FunctionNode getGetterSetterNode(Snap.Obj closure) {
            switch (closure.function.type) {
                case "unknown": return FunctionNode.create(closure, solver);
                case "user":
                case "bind":
                    if (this.typeAnalysis.getFunctionNode(closure) != null) {
                        return this.typeAnalysis.getFunctionNode(closure);
                    } else {

                    }
                case "native":
                    return FunctionNode.create(closure, solver);
                default:
                    throw new RuntimeException();
            }
        }
    }
}
