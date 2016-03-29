package dk.webbies.tscreate.analysis.methods.unionRecursively;

import dk.webbies.tscreate.analysis.HeapValueFactory;
import dk.webbies.tscreate.analysis.SubsetHeapValueFactory;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    objectNode.addField(property.name, solver.union(this.fromProperty(property), getPrimitivesFactory().nonVoid()));
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


}
