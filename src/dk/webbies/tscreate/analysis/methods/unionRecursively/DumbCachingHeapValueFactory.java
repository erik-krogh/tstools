package dk.webbies.tscreate.analysis.methods.unionRecursively;

import dk.webbies.tscreate.analysis.HeapValueFactory;
import dk.webbies.tscreate.analysis.SubsetHeapValueFactory;
import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.*;

/**
 * Created by erik1 on 08-02-2016.
 */
public class DumbCachingHeapValueFactory implements HeapValueFactory {
    private final UnionFindSolver solver;
    private SubsetHeapValueFactory subsetFactory;

    public DumbCachingHeapValueFactory(SubsetHeapValueFactory subsetFactory) {
        this.solver = subsetFactory.solver;
        this.subsetFactory = subsetFactory;
    }

    private Map<Snap.Obj, UnionNode> valueCache = new HashMap<>();

    @Override
    public UnionNode fromValue(Snap.Value value) {
        UnionNode primitive = subsetFactory.getPrimitiveValue(value, getPrimitivesFactory());
        if (primitive != null) {
            return primitive;
        }
        // From here we know that is in an Snap.Obj
        Snap.Obj obj = (Snap.Obj) value;

        if (valueCache.containsKey(obj)) {
            return valueCache.get(obj);
        } else {
            List<UnionNode> result = new ArrayList<>();
            ObjectNode objectNode = new ObjectNode(solver);
            result.add(objectNode);
            if (this.subsetFactory.getTypeAnalysis().getNativeClasses().nameFromObject(obj) != null) {
                objectNode.setTypeName(subsetFactory.getTypeAnalysis().getNativeClasses().nameFromObject(obj));
            }

            if (obj.prototype != null) {
                result.add(new HasPrototypeNode(solver, obj.prototype));
            }

            if (obj.function != null) {
                result.addAll(subsetFactory.getFunctionNode(obj));
            }

            valueCache.put(obj, objectNode);
            if (obj.properties != null) {
                for (Snap.Property property : obj.properties) {
                    objectNode.addField(property.name, this.fromProperty(property));
                }
            }
            return solver.union(result);
        }
    }

    @Override
    public PrimitiveNode.Factory getPrimitivesFactory() {
        return new DumbPrimitiveFactory(solver, subsetFactory.globalObject);
    }

    private Map<Snap.Property, UnionNode> propertyCache = new HashMap<>();

    @SuppressWarnings("Duplicates")
    @Override
    public UnionNode fromProperty(Snap.Property property) {
        if (property.value == null) {
            if (propertyCache.containsKey(property)) {
                return propertyCache.get(property);
            }
            if (property.get == null || property.set == null) {
                return new EmptyNode(solver);
            }
            UnionNode getter = new EmptyNode(solver);
            if (!(property.get instanceof Snap.UndefinedConstant)) {
                getter = subsetFactory.getGetterSetterNode((Snap.Obj) property.get).returnNode;
            }
            UnionNode setter = new EmptyNode(solver);
            if (!(property.set instanceof Snap.UndefinedConstant)) {
                FunctionNode setterFunctionNode = subsetFactory.getGetterSetterNode((Snap.Obj) property.set);
                if (!setterFunctionNode.arguments.isEmpty()) {
                    setter = setterFunctionNode.arguments.get(0);
                }
            }
            UnionNode result = solver.union(getter, setter);
            propertyCache.put(property, result);
            return result;
        } else {
            return fromValue(property.value);
        }
    }

    private class DumbPrimitiveFactory implements PrimitiveNode.Factory {
        private UnionFindSolver solver;
        private Snap.Obj globalObject;

        public DumbPrimitiveFactory(UnionFindSolver solver, Snap.Obj globalObject) {
            this.solver = solver;
            this.globalObject = globalObject;
        }

        private UnionNode gen(PrimitiveDeclarationType type, String... constructorNames) {
            PrimitiveNode result = new PrimitiveNode(type, solver);
            for (String constructorName : constructorNames) {
                solver.union(result, getPrototype(constructorName));
            }
            return result;
        }

        private HasPrototypeNode getPrototype(String constructorName) {
            Snap.Obj prototype = (Snap.Obj) ((Snap.Obj) this.globalObject.getProperty(constructorName).value).getProperty("prototype").value;
            return new HasPrototypeNode(solver, prototype);
        }

        public UnionNode number() {
            return gen(PrimitiveDeclarationType.Number(), "Number");
        }

        public UnionNode undefined() {
            return gen(PrimitiveDeclarationType.Void());
        }

        public UnionNode bool() {
            return gen(PrimitiveDeclarationType.Boolean(), "Boolean");
        }

        public UnionNode string() {
            return gen(PrimitiveDeclarationType.String(), "String");
        }

        public UnionNode any() {
            return gen(PrimitiveDeclarationType.Any());
        }

        public UnionNode stringOrNumber() {
            return gen(PrimitiveDeclarationType.StringOrNumber(), "Number", "String");
        }

        public UnionNode nonVoid() {
            return gen(PrimitiveDeclarationType.NonVoid());
        }

        public UnionNode function() {
            return solver.union(getPrototype("Function"), FunctionNode.create(Collections.EMPTY_LIST, this.solver));
        }

        public UnionNode array() {
            return solver.union(getPrototype("Array"), new DynamicAccessNode(this.solver, new EmptyNode(this.solver), number()));
        }
    }
}
