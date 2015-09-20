package dk.webbies.tscreate.analysis.unionFind.nodes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.brics.tajs.envspec.typescript.types.Signature;
import dk.webbies.tscreate.analysis.FunctionNodeFactory;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
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
                    List<UnionNode> fieldNodes = factory.fromValue(property.value);
                    EmptyUnionNode fieldNode = new EmptyUnionNode();
                    for (UnionNode node : fieldNodes) {
                        factory.solver.union(node, fieldNode);
                    }
                    addField(property.name, fieldNode);
                }
            }
        }
    }

    public static class Factory {
        private Map<Snap.Value, HeapValueNode> cache = new HashMap<>();
        private PrimitiveUnionNode.Factory primitivesFactory;
        private UnionFindSolver solver;
        private FunctionNodeFactory functionNodeFactory;
        private Multimap<Snap.Obj, UnionNode> functionCache = ArrayListMultimap.create();
        private HashMap<Snap.Obj, LibraryClass> libraryClasses;

        public Factory(Snap.Obj globalObject, UnionFindSolver solver, HashMap<Snap.Obj, LibraryClass> libraryClasses) {
            this.libraryClasses = libraryClasses;
            this.primitivesFactory = new PrimitiveUnionNode.Factory(solver, globalObject);
            this.solver = solver;
            this.functionNodeFactory = new FunctionNodeFactory(primitivesFactory, solver);
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
                if ((libraryClasses.get(obj.prototype) != null && libraryClasses.get(obj.prototype).isPrimitiveClass())) {
                    // Skipping, because it is a primitive class. (Here just functions are left).
                } else {
                    result.add(new HasPrototypeUnionNode(obj.prototype));
                    if (libraryClasses.get(obj.prototype) != null) {
                        solver.union(libraryClasses.get(obj.prototype).thisNode, objectNode);
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
                FunctionNode functionNode = new FunctionNode(obj);
                result.add(functionNode);
                if (obj.getProperty("prototype") != null) {
                    Snap.Obj prototype = (Snap.Obj) obj.getProperty("prototype").value;
                    if (libraryClasses.containsKey(prototype)) {
                        solver.union(functionNode, libraryClasses.get(prototype).constructorNode);
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
            if (value == null) {
                return new NonVoidNode();
            }
            return null;
        }
    }
}
