package dk.webbies.tscreate.analysis.unionFind;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.au.cs.casa.typescript.types.Signature;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.analysis.FunctionNodeFactory;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class HeapValueNode extends ObjectUnionNode {
    public final Snap.Value value;

    private HeapValueNode(Snap.Value value, Factory factory) {
        this.value = value;
        factory.cache.put(value, this);
        if (value instanceof Snap.Obj) {
            Snap.Obj obj = (Snap.Obj) value;
            if (obj.properties != null) {
                for (Snap.Property property : obj.properties) {
                    addField(property.name, factory.fromProperty(property));
                }
            }
        }
    }

    public static class Factory {
        private final Snap.Obj globalObject;
        private final Map<Type, String> typeNames;
        private final HasPrototypeUnionNode.Factory hasProtoFactory;
        private Map<Snap.Value, HeapValueNode> cache = new HashMap<>();
        private PrimitiveUnionNode.Factory primitivesFactory;
        private UnionFindSolver solver;
        private FunctionNodeFactory functionNodeFactory;
        private Multimap<Snap.Obj, UnionNode> functionCache = ArrayListMultimap.create();
        private Map<Snap.Obj, LibraryClass> libraryClasses;

        private final Map<Snap.Obj, FunctionNode> getterSetterCache = new HashMap<>();
        private final TypeAnalysis typeAnalysis;

        public Factory(Snap.Obj globalObject, UnionFindSolver solver, Map<Snap.Obj, LibraryClass> libraryClasses, Map<Type, String> typeNames, TypeAnalysis typeAnalysis) {
            this.libraryClasses = libraryClasses;
            this.globalObject = globalObject;
            this.typeAnalysis = typeAnalysis;
            this.primitivesFactory = new PrimitiveUnionNode.Factory(solver, this.globalObject, libraryClasses);
            this.solver = solver;
            this.typeNames = typeNames;
            this.functionNodeFactory = new FunctionNodeFactory(primitivesFactory, solver, this.typeNames);
            this.hasProtoFactory = new HasPrototypeUnionNode.Factory(libraryClasses);
        }

        public UnionNode fromProperty(Snap.Property property) {
            if (property.value == null) {
                if (property.get == null || property.set == null) {
                    throw new NullPointerException();
                }
                UnionNode getter = new EmptyUnionNode();
                if (!(property.get instanceof Snap.UndefinedConstant)) {
                    getter = getGetterSetterNode((Snap.Obj) property.get).returnNode;
                }
                UnionNode setter = new EmptyUnionNode();
                if (!(property.set instanceof Snap.UndefinedConstant)) {
                    FunctionNode setterFunctionNode = getGetterSetterNode((Snap.Obj) property.set);
                    if (!setterFunctionNode.arguments.isEmpty()) {
                        setter = setterFunctionNode.arguments.get(0);
                    }
                }
                GreatestCommonOfUnionNode fieldNode = new GreatestCommonOfUnionNode(getter, setter);
                solver.add(fieldNode);
                solver.add(getter);
                solver.add(setter);
                return fieldNode;
            } else {
                List<UnionNode> fieldNodes = fromValue(property.value);
                EmptyUnionNode fieldNode = new EmptyUnionNode();
                solver.union(fieldNode, fieldNodes);
                return fieldNode;
            }
        }

        public List<UnionNode> fromValue(Snap.Value value) {
            UnionNode primitive = getPrimitiveValue(value, primitivesFactory);
            if (primitive != null) {
                return Arrays.asList(primitive);
            }

            List<UnionNode> result = new ArrayList<>();
            ObjectUnionNode objectNode = new ObjectUnionNode();
            result.add(objectNode);

            Snap.Obj obj = (Snap.Obj) value;
            if (obj.prototype != null) {
                LibraryClass libraryClass = libraryClasses.get(obj.prototype);
                result.add(hasProtoFactory.create(obj.prototype));
                if (libraryClass != null && !libraryClass.isNativeClass()) {
                    solver.union(libraryClass.getNewThisNode(), objectNode);
                    Snap.Property constructorProp = obj.prototype.getProperty("constructor");
                    if (constructorProp != null) {
                        solver.union(libraryClass.getNewConstructorNode(), fromValue(constructorProp.value));
                    }
                }
            }
            if (obj.function != null) {
                result.addAll(getFunctionNode(obj));
            }

            if (cache.containsKey(value)) {
                result.add(cache.get(value));
            } else {
                result.add(new HeapValueNode(value, this));
            }
            return result;
        }

        private Collection<UnionNode> getFunctionNode(Snap.Obj obj) {
            if (functionCache.containsKey(obj)) {
                return functionCache.get(obj);
            }
            List<UnionNode> result = new ArrayList<>();
            if (obj.function.astNode != null) {
                FunctionNode functionNode = FunctionNode.create(obj);
                result.add(functionNode);
                if (obj.getProperty("prototype") != null) {
                    Snap.Obj prototype = (Snap.Obj) obj.getProperty("prototype").value;
                    if (libraryClasses.containsKey(prototype)) {
                         solver.union(functionNode, libraryClasses.get(prototype).getNewConstructorNode());
                    }
                }
            } else {
                ArrayList<Signature> signatures = new ArrayList<>(obj.function.callSignatures);
                signatures.addAll(obj.function.constructorSignatures); // TODO: Separate constructor calls?
                result.addAll(signatures.stream().map(signature -> functionNodeFactory.fromSignature(signature, obj, null)).collect(Collectors.toList()));
            }
            functionCache.putAll(obj, result);
            return result;
        }

        private UnionNode getPrimitiveValue(Snap.Value value, PrimitiveUnionNode.Factory primitivesBuilder) {
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
            throw new RuntimeException();
        }

        private FunctionNode getGetterSetterNode(Snap.Obj closure) {
            if (this.getterSetterCache.containsKey(closure)) {
                return this.getterSetterCache.get(closure);
            }

            FunctionNode functionNode = FunctionNode.create(closure);
            Map<Snap.Obj, FunctionNode> functionNodes = new HashMap<>();
            functionNodes.put(closure, functionNode);

            this.typeAnalysis.analyse(closure, new HashMap<>(), functionNodes, this.solver, functionNode, this);

            this.getterSetterCache.put(closure, functionNode);
            return functionNode;
        }
    }
}
