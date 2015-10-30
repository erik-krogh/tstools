package dk.webbies.tscreate.analysis;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.analysis.declarations.types.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class TypeFactory {
    private final Map<UnionClass, DeclarationType> cache = new HashMap<>();
    private final Snap.Obj globalObject;
    private HashMap<Snap.Obj, LibraryClass> libraryClasses;
    private Options options;
    public final Set<Snap.Obj> finishedFunctionClosures = new HashSet<>();
    public final TypeReducer typeReducer;
    private Map<Type, String> typeNames;

    public TypeFactory(Snap.Obj globalObject, HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, Map<Type, String> typeNames) {
        this.globalObject = globalObject;
        this.libraryClasses = libraryClasses;
        this.options = options;
        this.typeNames = typeNames;
        this.typeReducer = new TypeReducer(globalObject);
    }


    private final Map<Snap.Obj, UnresolvedDeclarationType> functionTypes = new HashMap<>();

    public DeclarationType getFunctionType(Snap.Obj closure) {
        if (functionTypes.containsKey(closure)) {
            return functionTypes.get(closure);
        } else {
            UnresolvedDeclarationType result = new UnresolvedDeclarationType();
            functionTypes.put(closure, result);
            return result;
        }
    }

    public void putResolvedFunctionType(Snap.Obj closure, DeclarationType type) {
        if (functionTypes.containsKey(closure)) {
            UnresolvedDeclarationType unresolved = functionTypes.get(closure);
            unresolved.setResolvedType(type);
        } else {
            functionTypes.put(closure, new UnresolvedDeclarationType(type));
        }
    }

    private DeclarationType getType(Collection<UnionNode> nodes) {
        if (nodes.size() == 0) {
            return PrimitiveDeclarationType.VOID;
        } else if (nodes.size() == 1) {
            return getType(nodes.iterator().next());
        } else {
            CombinationType result = new CombinationType(typeReducer);
            for (UnionNode node : nodes) {
                result.addType(getType(node));
            }

            return result;
        }
    }

    DeclarationType getType(UnionNode node) {
        return getType(node.getUnionClass());
    }

    private DeclarationType getType(UnionClass unionClass) {
        if (unionClass == null) {
            throw new NullPointerException();
        }
        DeclarationType result = cache.get(unionClass);
        if (result != null) {
            return result;
        }
        UnresolvedDeclarationType unresolvedType = new UnresolvedDeclarationType();
        cache.put(unionClass, unresolvedType);
        result = getTypeNoCache(unionClass.getFeature());
        unresolvedType.setResolvedType(result);
        return result;
    }

    public CombinationType getTypeNoCache(UnionFeature feature) {
        // First unpacking the IncludeNode
        CombinationType result = new CombinationType(typeReducer);
        if (feature.unionClass.includes != null) {
            for (UnionClass include : feature.unionClass.includes) {
                result.addType(getType(include));
            }
        }

        // Adding primitives
        result.addType(getPrimitiveType(feature.getPrimitives()));

        // Adding function
        if (feature.getFunctionFeature() != null) {
            result.addType(createFunctionType(feature.getFunctionFeature()));
        }

        // Adding names object types.
        feature.getTypeNames().stream().filter(name -> name != null).map(NamedObjectType::new).forEach(result::addType);

        // Adding object instance
        result.addType(getObjectInstanceType(feature));

        // Adding class declaration (if it is a constructor).
        feature.getHeapValues().stream()
                .filter(obj -> obj instanceof Snap.Obj)
                .map(obj -> (Snap.Obj) obj)
                .filter(obj -> obj.function != null && obj.getProperty("prototype") != null)
                .map(obj -> obj.getProperty("prototype").value)
                .distinct()
                .map(this.libraryClasses::get)
                .map(this::getClassType)
                .forEach(result::addType);

        if (!feature.getObjectFields().isEmpty()) {
            HashMap<String, DeclarationType> fields = new HashMap<>();
            feature.getObjectFields().forEach((name, node) -> {
                fields.put(name, getType(node));
            });
            result.addType(new UnnamedObjectType(fields));
        }
        return result;
    }

    private List<DeclarationType> getObjectInstanceType(UnionFeature feature) {
        return feature.getPrototypes().stream()
                .map(this.libraryClasses::get)
                .filter(clazz -> clazz != null)
                .filter(clazz -> !clazz.isNativeClass())
                .map(this::getClassType)
                .filter(classType -> classType != null)
                .map(ClassInstanceType::new)
                .collect(Collectors.toList());
    }

    private List<DeclarationType> getPrimitiveType(Set<PrimitiveDeclarationType> primitives) {
        return new ArrayList<>(primitives);
    }

    private Map<LibraryClass, UnresolvedDeclarationType> libraryClassCache = new HashMap<>();

    private DeclarationType getClassType(LibraryClass libraryClass) {
        if (libraryClass == null) {
            return null;
        }
        if (options.isClassNames.stream().anyMatch(str -> str.equals(libraryClass.getName()))) {
            libraryClass.isUsedAsClass = true;
        }
        if (!libraryClass.isUsedAsClass) {
            return null;
        }
        Snap.Property constructorProp = libraryClass.prototype.getProperty("constructor");
        if (constructorProp == null) {
            return null;
        }

        // Handling the native case here, because there we never end up in a recursive type.
        Snap.Obj constructor = (Snap.Obj) libraryClass.prototype.getProperty("constructor").value;
        switch (constructor.function.type) {
            case "native":
                // We cant produce the "Constructor" of a native class, because we don't know what to call it.
                // I could produce something like "interface NumberConstructor { new () : Number;}", but that just seems stupid.
                return null;
        }

        if (libraryClassCache.containsKey(libraryClass)) {
            return libraryClassCache.get(libraryClass);
        }
        UnresolvedDeclarationType unresolvedType = new UnresolvedDeclarationType();
        libraryClassCache.put(libraryClass, unresolvedType);

        return unresolvedType;
    }

    public void resolveClassTypes() {
        boolean resolvedAClass;
        do {
            resolvedAClass = false;
            for (Map.Entry<LibraryClass, UnresolvedDeclarationType> entry : new HashMap<>(libraryClassCache).entrySet()) {
                if (!entry.getValue().isResolved()) {
                    resolvedAClass = true;
                    entry.getValue().setResolvedType(createClassType(entry.getKey()));
                }
            }
        } while (resolvedAClass);
    }

    // This is only to be used from within resolveClassTypes.
    // TODO: Use the options defined in Options
    // TODO: This nodes from constructor, and from prototype-functions.
    private DeclarationType createClassType(LibraryClass libraryClass) {
        Snap.Obj constructor = (Snap.Obj) libraryClass.prototype.getProperty("constructor").value;
        switch (constructor.function.type) {
            case "user":
                Set<UnionFeature> constructorFeatures = new HashSet<>(libraryClass.constructorNodes.stream().map(UnionNode::getFeature).map(UnionFeature::getReachable).reduce(new ArrayList<>(), Util::reduceList));

                CombinationType constructorType = new CombinationType(typeReducer);
                List<UnionFeature.FunctionFeature> constructorFunctionFeatures = constructorFeatures.stream().filter(feature -> feature.getFunctionFeature() != null).map(UnionFeature::getFunctionFeature).collect(Collectors.toList());
                constructorFunctionFeatures.forEach(constructorFeature -> {
                    constructorType.addType(createFunctionType(constructorFeature));
                });

                Map<String, DeclarationType> staticFields = new HashMap<>();
                for (Map.Entry<String, Snap.Property> entry : constructor.getPropertyMap().entrySet()) {
                    if (Arrays.asList("prototype", "caller", "length", "name", "arguments").stream().noneMatch(str -> str.equals(entry.getKey()))) {
                        staticFields.put(entry.getKey(), getHeapPropType(entry.getValue()));
                    }
                }


                Map<String, DeclarationType> prototypeProperties = createClassFields(libraryClass);


                ClassType classType = new ClassType(constructorType, prototypeProperties, libraryClass.getName(), staticFields);
                classType.setSuperClass(getClassType(libraryClass.superClass));
                return classType;
            default: // Case "native" should already be handled here.
                throw new UnsupportedOperationException();
        }
    }

    private Map<String, DeclarationType> createClassFields(LibraryClass libraryClass) {
        Map<String, DeclarationType> fieldTypes = new HashMap<>();
        libraryClass.prototype.getPropertyMap().forEach((name, prop) -> {
            if (!name.equals("constructor")) {
                fieldTypes.put(name, getHeapPropType(prop));
            }
        });

        if (options.classOptions.useClassInstancesFromHeap) {
            Multimap<String, Snap.Property> propertiesMultimap = ArrayListMultimap.create();
            for (Snap.Obj instance : libraryClass.instances) {
                for (Snap.Property property : instance.properties) {
                    propertiesMultimap.put(property.name, property);
                }
            }
            for (Map.Entry<String, Collection<Snap.Property>> entry : propertiesMultimap.asMap().entrySet()) {
                String name = entry.getKey();
                Collection<Snap.Property> properties = entry.getValue();
                if (name.equals("constructor") || fieldTypes.containsKey(name)) {
                    continue;
                }
                fieldTypes.put(name, getHeapPropType(properties));
            }
        }

        if (options.classOptions.useThisObjectUsages) {
            Multimap<String, UnionNode> propertiesMultiMap = ArrayListMultimap.create();
            for (UnionNode thisNode : libraryClass.thisNodes) {
                for (Map.Entry<String, UnionNode> entry : thisNode.getFeature().getObjectFields().entrySet()) {
                    propertiesMultiMap.put(entry.getKey(), entry.getValue());
                }
            }
            for (Map.Entry<String, Collection<UnionNode>> entry : propertiesMultiMap.asMap().entrySet()) {
                String name = entry.getKey();
                Collection<UnionNode> nodes = entry.getValue();
                if (name.equals("constructor") || fieldTypes.containsKey(name)) {
                    continue;
                }
                fieldTypes.put(name, getType(nodes));
            }
        }
        return fieldTypes;
    }

    private DeclarationType getHeapPropType(Collection<Snap.Property> properties) {
        if (properties.size() == 0) {
            return PrimitiveDeclarationType.VOID;
        } else if (properties.size() == 1) {
            return getHeapPropType(properties.iterator().next());
        } else {
            CombinationType result = new CombinationType(typeReducer);
            for (Snap.Property property : properties) {
                result.addType(getHeapPropType(property));
            }

            return result;
        }
    }

    public DeclarationType getHeapPropType(Snap.Property prop) {
        if (prop.value != null) {
            return getHeapValueType(prop.value);
        } else {
            TypeAnalysis typeAnalysis = new TypeAnalysis(libraryClasses, options, globalObject, this, typeNames);
            UnionFindSolver solver = new UnionFindSolver();
            HeapValueNode.Factory heapFactory = new HeapValueNode.Factory(globalObject, solver, libraryClasses, typeAnalysis, new HashSet<>());
            UnionNode unionNode = heapFactory.fromProperty(prop);
            solver.finish();
            return getType(unionNode);
        }
    }

    // TODO: Can this be deleted, and just use HeapValueNode and get a type from that?
    public DeclarationType getHeapValueType(Snap.Value value) {
        if (value instanceof Snap.BooleanConstant) {
            return PrimitiveDeclarationType.BOOLEAN;
        } else if (value instanceof Snap.NumberConstant) {
            return PrimitiveDeclarationType.NUMBER;
        } else if (value instanceof Snap.StringConstant) {
            return PrimitiveDeclarationType.STRING;
        } else if (value instanceof Snap.UndefinedConstant) {
            return PrimitiveDeclarationType.VOID;
        } else if (value instanceof Snap.NullConstant) {
            return PrimitiveDeclarationType.ANY;
        } else if ((value instanceof Snap.Obj)) {
            if (((Snap.Obj) value).function != null) {
                return getFunctionType((Snap.Obj) value);
            } else {
                return getObjectType((Snap.Obj) value);
            }
        } else {
            throw new RuntimeException("I don't know what to do with a " + value);
        }

    }


    private Map<Snap.Obj, UnresolvedDeclarationType> objectTypeCache = new HashMap<>();
    private UnresolvedDeclarationType getObjectType(Snap.Obj value) {
        if (objectTypeCache.containsKey(value)) {
            return objectTypeCache.get(value);
        }
        UnresolvedDeclarationType unresolvedType = new UnresolvedDeclarationType();
        objectTypeCache.put(value, unresolvedType);

        if (libraryClasses.get(value.prototype) != null && getClassType(libraryClasses.get(value.prototype)) != null) {
            ClassInstanceType result = new ClassInstanceType(getClassType(libraryClasses.get(value.prototype)));
            unresolvedType.setResolvedType(result);
            return unresolvedType;
        } else {
            HashMap<String, DeclarationType> declarations = new HashMap<>();
            for (Map.Entry<String, Snap.Property> entry : value.getPropertyMap().entrySet()) {
                declarations.put(entry.getKey(), getHeapPropType(entry.getValue()));
            }

            UnnamedObjectType result = new UnnamedObjectType(declarations);
            unresolvedType.setResolvedType(result);

            return unresolvedType;
        }
    }



    public List<DeclarationType> createFunctionType(UnionFeature.FunctionFeature feature) {
        if (!feature.getClosures().isEmpty()) {
            return feature.getClosures().stream().map(closure -> {
                boolean isUserDefined = closure.function.type.equals("user") || closure.function.type.equals("bind");
                if (!finishedFunctionClosures.contains(closure) && isUserDefined) {
                    return getFunctionType(closure);
                } else {
                    DeclarationType returnType = getType(feature.getReturnNode());

                    List<FunctionType.Argument> arguments = feature.getArguments().stream().map((arg) -> {
                        DeclarationType type = getType(arg.node);
                        return new FunctionType.Argument(arg.name, type);
                    }).collect(Collectors.toList());

                    return new FunctionType(returnType, arguments);
                }
            }).collect(Collectors.toList());
        } else {
            DeclarationType returnType = getType(feature.getReturnNode());

            List<FunctionType.Argument> arguments = feature.getArguments().stream().map((arg) -> {
                DeclarationType type = getType(arg.node);
                return new FunctionType.Argument(arg.name, type);
            }).collect(Collectors.toList());

            return Arrays.asList(new FunctionType(returnType, arguments));
        }
    }

}
