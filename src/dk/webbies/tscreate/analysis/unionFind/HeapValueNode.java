package dk.webbies.tscreate.analysis.unionFind;

import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class HeapValueNode extends ObjectUnionNode {
    public final Snap.Value value;

    private HeapValueNode(Snap.Value value, Factory factory, Map<Snap.Value, HeapValueNode> cache, UnionFindSolver solver) {
        super(solver);
        this.value = value;
        cache.put(value, this);
        if (value instanceof Snap.Obj) {
            Snap.Obj obj = (Snap.Obj) value;
            if (obj.properties != null) {
                for (Snap.Property property : obj.properties) {
                    addField(property.name, factory.fromProperty(property, cache));
                }
            }
        }
    }

    @Override
    public void addTo(UnionClass unionClass) {
        super.addTo(unionClass);
        UnionFeature feature = unionClass.getFeature();
        if (feature.heapValues == null) {
            feature.heapValues = new HashSet<>();
        }
        feature.heapValues.add(this.value);
    }

    public static class Factory {
        private final Snap.Obj globalObject;
        private PrimitiveUnionNode.Factory primitivesFactory;
        private UnionFindSolver solver;
        private Map<Snap.Obj, LibraryClass> libraryClasses;

        private final Map<Snap.Obj, FunctionNode> getterSetterCache = new HashMap<>();
        private final TypeAnalysis typeAnalysis;
        private Set<Snap.Obj> analyzedFunctions;

        public Factory(Snap.Obj globalObject, UnionFindSolver solver, Map<Snap.Obj, LibraryClass> libraryClasses, TypeAnalysis typeAnalysis, Set<Snap.Obj> analyzedFunctions) {
            this.libraryClasses = libraryClasses;
            this.globalObject = globalObject;
            this.typeAnalysis = typeAnalysis;
            this.analyzedFunctions = analyzedFunctions;
            this.primitivesFactory = new PrimitiveUnionNode.Factory(solver, this.globalObject);
            this.solver = solver;
        }

        public UnionNode fromProperty(Snap.Property property) {
            return fromProperty(property, new HashMap<>());
        }

        private UnionNode fromProperty(Snap.Property property, Map<Snap.Value, HeapValueNode> cache) {
            if (property.value == null) {
                if (property.get == null || property.set == null) {
                    throw new NullPointerException();
                }
                UnionNode getter = new EmptyUnionNode(solver);
                if (!(property.get instanceof Snap.UndefinedConstant)) {
                    getter = getGetterSetterNode((Snap.Obj) property.get).returnNode;
                }
                UnionNode setter = new EmptyUnionNode(solver);
                if (!(property.set instanceof Snap.UndefinedConstant)) {
                    FunctionNode setterFunctionNode = getGetterSetterNode((Snap.Obj) property.set);
                    if (!setterFunctionNode.arguments.isEmpty()) {
                        setter = setterFunctionNode.arguments.get(0);
                    }
                }
                return new IncludeNode(solver, getter, setter);
            } else {
                List<UnionNode> fieldNodes = fromValue(property.value, cache);
                EmptyUnionNode fieldNode = new EmptyUnionNode(solver);
                solver.union(fieldNode, fieldNodes);
                return fieldNode;
            }
        }

        public List<UnionNode> fromValue(Snap.Value value) {
            return fromValue(value, new HashMap<>());
        }

        private List<UnionNode> fromValue(Snap.Value value, Map<Snap.Value, HeapValueNode> cache) {
            UnionNode primitive = getPrimitiveValue(value, primitivesFactory);
            if (primitive != null) {
                return Arrays.asList(primitive);
            }

            List<UnionNode> result = new ArrayList<>();
            ObjectUnionNode objectNode = new ObjectUnionNode(solver);
            result.add(objectNode);

            Snap.Obj obj = (Snap.Obj) value;
            if (obj.prototype != null) {
                LibraryClass libraryClass = libraryClasses.get(obj.prototype);
                result.add(HasPrototypeUnionNode.create(obj.prototype, solver));
                if (libraryClass != null && !libraryClass.isNativeClass()) {
                    solver.union(libraryClass.getNewThisNode(solver), objectNode);
                    Snap.Property constructorProp = obj.prototype.getProperty("constructor");
                    if (constructorProp != null) {
                        solver.union(libraryClass.getNewConstructorNode(solver), fromProperty(constructorProp, cache));
                    }
                }
            }
            if (obj.function != null) {
                result.addAll(getFunctionNode(obj));
            }

            if (cache.containsKey(value)) {
                result.add(cache.get(value));
            } else {
                result.add(new HeapValueNode(value, this, cache, solver));
            }
            return result;
        }

        private Collection<UnionNode> getFunctionNode(Snap.Obj obj) {
            List<UnionNode> result = new ArrayList<>();
            if (obj.function != null) {
                FunctionNode functionNode = FunctionNode.create(obj, solver);
                result.add(functionNode);
                if (obj.getProperty("prototype") != null) {
                    Snap.Obj prototype = (Snap.Obj) obj.getProperty("prototype").value;
                    if (libraryClasses.containsKey(prototype)) {
                         solver.union(functionNode, libraryClasses.get(prototype).getNewConstructorNode(solver));
                    }
                }
            } else {
                // We could parse the signatures here, but we don't know if it is a constructorCall or not.
                // So signature parsing is done in UnionConstraintVisitor.CallGraphResolver. 
            }
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

            FunctionNode functionNode = FunctionNode.create(closure, solver);
            Map<Snap.Obj, FunctionNode> functionNodes = new HashMap<>();
            functionNodes.put(closure, functionNode);

            this.getterSetterCache.put(closure, functionNode);

            this.typeAnalysis.analyse(closure, functionNodes, this.solver, functionNode, this, analyzedFunctions);

            return functionNode;
        }
    }
}
