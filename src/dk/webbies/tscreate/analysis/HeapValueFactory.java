package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 05-09-2015.
 */
public class HeapValueFactory {
    private PrimitiveNode.Factory primitivesFactory;
    private UnionFindSolver solver;
    private Map<Snap.Obj, LibraryClass> libraryClasses;

    private final Map<Snap.Obj, FunctionNode> getterSetterCache = new HashMap<>();
    private final TypeAnalysis typeAnalysis;
    private Set<Snap.Obj> analyzedFunctions;

    public HeapValueFactory(Snap.Obj globalObject, UnionFindSolver solver, Map<Snap.Obj, LibraryClass> libraryClasses, TypeAnalysis typeAnalysis, Set<Snap.Obj> analyzedFunctions) {
        this.libraryClasses = libraryClasses;
        this.typeAnalysis = typeAnalysis;
        this.analyzedFunctions = analyzedFunctions;
        this.primitivesFactory = new PrimitiveNode.Factory(solver, globalObject);
        this.solver = solver;
    }

    public UnionNode fromProperty(Snap.Property property) {
        return fromProperty(property, new HashMap<>());
    }

    private UnionNode fromProperty(Snap.Property property, Map<Snap.Value, ObjectNode> cache) {
        if (property.value == null) {
            if (property.get == null || property.set == null) {
                throw new NullPointerException();
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
            return new IncludeNode(solver, getter, setter);
        } else {
            List<UnionNode> fieldNodes = fromValue(property.value, cache);
            EmptyNode fieldNode = new EmptyNode(solver);
            solver.union(fieldNode, fieldNodes);
            return fieldNode;
        }
    }

    public List<UnionNode> fromValue(Snap.Value value) {
        return fromValue(value, new HashMap<>());
    }

    private List<UnionNode> fromValue(Snap.Value value, Map<Snap.Value, ObjectNode> cache) {
        UnionNode primitive = getPrimitiveValue(value, primitivesFactory);
        if (primitive != null) {
            return Arrays.asList(primitive);
        }

        List<UnionNode> result = new ArrayList<>();
        ObjectNode objectNode = new ObjectNode(solver);
        result.add(objectNode);

        Snap.Obj obj = (Snap.Obj) value;
        if (obj.prototype != null) {
            LibraryClass libraryClass = libraryClasses.get(obj.prototype);
            result.add(new HasPrototypeNode(solver, obj.prototype));
            if (libraryClass != null && !libraryClass.isNativeClass()) {
                if (typeAnalysis.options.classOptions.useThisObjectUsages) {
                    solver.union(libraryClass.getNewThisNode(solver), objectNode);
                }
                Snap.Property constructorProp = obj.prototype.getProperty("constructor");
                if (constructorProp != null && typeAnalysis.options.classOptions.useConstructorUsages) {
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
            cache.put(value, objectNode);
            if (obj.properties != null) {
                for (Snap.Property property : obj.properties) {
                    objectNode.addField(property.name, this.fromProperty(property, cache));
                }
            }
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
                if (libraryClasses.containsKey(prototype) && !libraryClasses.get(prototype).isNativeClass()) {
                    if (typeAnalysis.options.classOptions.useConstructorUsages) {
                        solver.union(functionNode, libraryClasses.get(prototype).getNewConstructorNode(solver));
                    }
                }
            }
        } else {
            // We could parse the signatures here, but we don't know if it is a constructorCall or not.
            // So signature parsing is done in UnionConstraintVisitor.CallGraphResolver.
        }
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
