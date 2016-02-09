package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class SubsetHeapValueFactory implements HeapValueFactory {
    private PrimitiveNode.Factory primitivesFactory;
    public final UnionFindSolver solver;
    public final Snap.Obj globalObject;
    private final TypeAnalysis typeAnalysis;

    private final Map<Snap.Obj, UnionNode> cache = new HashMap<>();
    private final Map<Snap.Property, UnionNode> propertyCache = new HashMap<>();

    public SubsetHeapValueFactory(Snap.Obj globalObject, UnionFindSolver solver, TypeAnalysis typeAnalysis) {
        this.globalObject = globalObject;
        this.typeAnalysis = typeAnalysis;
        this.primitivesFactory = new SubSetPrimitiveFactory(solver, globalObject);
        this.solver = solver;
        JSNAPUtil.getAllObjects(globalObject).forEach(obj -> {
            this.innerFromValue(obj);
            obj.properties.forEach(this::innerFromProperty);
        });
    }

    public PrimitiveNode.Factory getPrimitivesFactory() {
        return primitivesFactory;
    }

    public UnionNode fromProperty(Snap.Property property) {
        return new IncludeNode(solver, innerFromProperty(property));
    }

    private UnionNode innerFromProperty(Snap.Property property) {
        if (property.value == null) {
            if (propertyCache.containsKey(property)) {
                return propertyCache.get(property);
            }
            if (property.get == null || property.set == null) {
                // TODO: This sometimes happens with Symbols.
                return new EmptyNode(solver);
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
            IncludeNode result = new IncludeNode(solver, getter, setter);
            propertyCache.put(property, result);
            return result;
        } else {
            return innerFromValue(property.value);
        }
    }

    public UnionNode fromValue(Snap.Value value) {
        return new IncludeNode(solver, innerFromValue(value));
    }

    private UnionNode innerFromValue(Snap.Value value) {
        UnionNode primitive = getPrimitiveValue(value, primitivesFactory);
        if (primitive != null) {
            return primitive;
        }
        // From here we know that is in an Snap.Obj
        Snap.Obj obj = (Snap.Obj) value;

        if (cache.containsKey(obj)) {
            return new IncludeNode(solver, cache.get(obj));
        } else {
            List<UnionNode> result = new ArrayList<>();
            ObjectNode objectNode = new ObjectNode(solver);
            result.add(objectNode);
            if (this.typeAnalysis.getNativeClasses().nameFromObject(obj) != null) {
                objectNode.setTypeName(this.typeAnalysis.getNativeClasses().nameFromObject(obj));
            }

            if (obj.prototype != null) {
                result.add(new HasPrototypeNode(solver, obj.prototype));
            }

            if (obj.function != null) {
                result.addAll(getFunctionNode(obj));
            }

            cache.put(obj, objectNode);
            if (obj.properties != null) {
                for (Snap.Property property : obj.properties) {
                    objectNode.addField(property.name, this.innerFromProperty(property));
                }
            }
            return new IncludeNode(solver, solver.union(result));
        }
    }

    public Collection<UnionNode> getFunctionNode(Snap.Obj obj) {
        List<UnionNode> result = new ArrayList<>();
        if (obj.function != null) {
            FunctionNode functionNode = FunctionNode.create(obj, solver);
            result.add(functionNode);
        } else {
            // We could parse the signatures here, but we don't know if it is a constructorCall or not.
            // So signature parsing is done in UnionConstraintVisitor.CallGraphResolver.
        }
        return result;
    }

    public UnionNode getPrimitiveValue(Snap.Value value, PrimitiveNode.Factory primitivesBuilder) {
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
        return primitivesBuilder.undefined(); // TODO: Maybe fix whatever is causing this.
    }

    @SuppressWarnings("Duplicates")
    public FunctionNode getGetterSetterNode(Snap.Obj closure) {
        if (closure == null || closure.function == null || closure.function.type == null) {
            System.out.printf("");
        }
        switch (closure.function.type) {
            case "unknown": return FunctionNode.create(closure, solver);
            case "user":
            case "bind":
                assert this.typeAnalysis.getFunctionNodes().containsKey(closure);
                return this.typeAnalysis.getFunctionNodes().get(closure);
            case "native":
                throw new UnsupportedOperationException();
            default:
                throw new RuntimeException();
        }
    }

    public TypeAnalysis getTypeAnalysis() {
        return typeAnalysis;
    }

    private class SubSetPrimitiveFactory implements PrimitiveNode.Factory {
        private UnionFindSolver solver;
        private Snap.Obj globalObject;
        private UnionNode bool;
        private UnionNode number;
        private UnionNode undefined;
        private UnionNode string;
        private UnionNode  any;
        private UnionNode stringOrNumber;
        private UnionNode nonVoid;
        private UnionNode function;
        private UnionNode array;

        public SubSetPrimitiveFactory(UnionFindSolver solver, Snap.Obj globalObject) {
            this.solver = solver;
            this.globalObject = globalObject;
            this.bool = gen(PrimitiveDeclarationType.Boolean(), "Boolean");
            this.number = gen(PrimitiveDeclarationType.Number(), "Number");
            this.undefined = gen(PrimitiveDeclarationType.Void());
            this.string = gen(PrimitiveDeclarationType.String(), "String");
            this.any = gen(PrimitiveDeclarationType.Any());
            this.stringOrNumber = gen(PrimitiveDeclarationType.StringOrNumber(), "Number", "String");
            this.nonVoid = gen(PrimitiveDeclarationType.NonVoid());
            this.function = solver.union(getPrototype("Function"), FunctionNode.create(Collections.EMPTY_LIST, this.solver));
            this.array = solver.union(getPrototype("Array"), new DynamicAccessNode(this.solver, new EmptyNode(this.solver), number()));
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
            return new IncludeNode(solver, number);
        }

        public UnionNode undefined() {
            return new IncludeNode(solver, undefined);
        }

        public UnionNode bool() {
            return new IncludeNode(solver, bool);
        }

        public UnionNode string() {
            return new IncludeNode(solver, string);
        }

        public UnionNode any() {
            return new IncludeNode(solver, any);
        }

        public UnionNode stringOrNumber() {
            return new IncludeNode(solver, stringOrNumber);
        }

        public UnionNode nonVoid() {
            return new IncludeNode(solver, nonVoid);
        }

        public UnionNode function() {
            return new IncludeNode(solver, function);
        }

        public UnionNode array() {
            return new IncludeNode(solver, array);
        }
    }
}
