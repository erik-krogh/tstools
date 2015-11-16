package dk.webbies.tscreate.analysis;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.au.cs.casa.typescript.types.Signature;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;
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
    private final NativeClassesMap nativeClasses;
    private HashMap<Snap.Obj, LibraryClass> libraryClasses;
    private Options options;
    public final TypeReducer typeReducer;

    public TypeFactory(Snap.Obj globalObject, HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, NativeClassesMap nativeClasses) {
        this.globalObject = globalObject;
        this.libraryClasses = libraryClasses;
        this.options = options;
        this.nativeClasses = nativeClasses;
        this.typeReducer = new TypeReducer(globalObject, nativeClasses);
    }


    private final Map<Snap.Obj, UnresolvedDeclarationType> functionTypes = new HashMap<>();

    public DeclarationType getFunctionType(Snap.Obj closure) {
        return Util.getWithDefault(functionTypes, closure, new UnresolvedDeclarationType());
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
            return PrimitiveDeclarationType.Void();
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

    public DeclarationType getTypeNoCache(UnionFeature feature) {
        // First unpacking the IncludeNodes
        CombinationType result = new CombinationType(typeReducer);
        if (feature.unionClass.includes != null) {
            for (UnionClass include : feature.unionClass.includes) {
                result.addType(getType(include));
            }
        }

        // Adding primitives
        feature.getPrimitives().stream().forEach(result::addType);

        // Adding function
        if (feature.getFunctionFeature() != null) {
            createFunctionType(feature.getFunctionFeature()).forEach(result::addType);
        }

        // Adding names object types.
        feature.getTypeNames().stream().filter(Util::notNull).map(NamedObjectType::new).forEach(result::addType);

        // Adding object instance
        getObjectInstanceType(feature).forEach(result::addType);

        // Adding class declaration (if it is a constructor).
        if (feature.getFunctionFeature() != null) {
            feature.getFunctionFeature().getClosures().stream()
                .filter(obj -> obj.function != null && obj.getProperty("prototype") != null)
                .map(obj -> obj.getProperty("prototype").value)
                .distinct()
                .map(this.libraryClasses::get)
                .filter(Util::notNull)
                .filter(clazz -> !clazz.isNativeClass())
                .map(this::getClassType)
                .filter(Util::notNull)
                .forEach(result::addType);
        }

        if (!feature.getObjectFields().isEmpty()) {
            HashMap<String, DeclarationType> fields = new HashMap<>();
            feature.getObjectFields().forEach((name, node) -> {
                fields.put(name, getType(node));
            });
            result.addType(new UnnamedObjectType(fields));
        }

        if (feature.getDynamicAccessLookupExp() != null) {
            UnionNode lookupType = feature.getDynamicAccessLookupExp();
            UnionNode returnType = feature.getDynamicAccessReturnType();
            result.addType(new DynamicAccessType(getType(lookupType), getType(returnType)));
        }

        if (result.types.size() == 0) {
            return PrimitiveDeclarationType.Void();
        } else if (result.types.size() == 1 && !(result.types.get(0) instanceof UnresolvedDeclarationType)) {
            return result.types.get(0);
        }
        result.partiallyResolve();
        return result;
    }

    private List<DeclarationType> getObjectInstanceType(UnionFeature feature) {
        List<LibraryClass> classes = feature.getPrototypes().stream()
                .map(this.libraryClasses::get)
                .filter(clazz -> clazz != null).collect(Collectors.toList());

        List<DeclarationType> result = new ArrayList<>();

        // Adding all the user-defined classes classes.
        classes.stream()
                .filter(clazz -> !clazz.isNativeClass())
                .map(this::getClassType)
                .filter(classType -> classType != null)
                .map(ClassInstanceType::new)
                .forEach(result::add);

        // Adding all the native classes
        classes.stream()
                .filter(LibraryClass::isNativeClass)
                .map(LibraryClass::getPrototype)
                .map(nativeClasses::nameFromPrototype)
                .filter(Util::notNull)
                .map(name -> Util.removeSuffix(name, "Constructor"))
                .map(NamedObjectType::new)
                .map(TypeFactory::filterPrimitives)
                .filter(Util::notNull)
                .forEach(result::add);

        return result;
    }

    // Primitives are handled by the PrimitiveDeclarationType, and filtering them out here allows for "spurious" prototypes (see PrimitiveDeclarationType.STRING_OR_NUMBER
    private static DeclarationType filterPrimitives(NamedObjectType named) {
        switch (named.getName()) {
            case "Function": return new FunctionType(PrimitiveDeclarationType.Void(), Collections.EMPTY_LIST);
            case "Number":
            case "Boolean":
            case "String":
                return null;
            default:
                return named;
        }
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
                // This should never be hit.
                throw new RuntimeException();
            case "unknown": // Well we cant do anything with this one.
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
    private DeclarationType createClassType(LibraryClass libraryClass) {
        Snap.Property constructorProp = libraryClass.prototype.getProperty("constructor");
        Snap.Obj constructor = (Snap.Obj) constructorProp.value;
        switch (constructor.function.type) {
            case "user":
                CombinationType constructorType = createConstructorType(libraryClass, constructor);

                Map<String, DeclarationType> staticFields = new HashMap<>();
                for (Map.Entry<String, Snap.Property> entry : constructor.getPropertyMap().entrySet()) {
                    if (Arrays.asList("prototype", "caller", "length", "name", "arguments").stream().noneMatch(str -> str.equals(entry.getKey()))) {
                        staticFields.put(entry.getKey(), getHeapPropType(entry.getValue()));
                    }
                }

                Map<String, DeclarationType> prototypeProperties = createClassFields(libraryClass, constructor);

                ClassType classType = new ClassType(libraryClass.getName(), constructorType, prototypeProperties, staticFields);
                classType.setSuperClass(getClassType(libraryClass.superClass));
                return classType;
            default: // Case "native" should already be handled here.
                throw new UnsupportedOperationException("unhandled type for creating a class type: " + constructor.function.type);
        }
    }

    private CombinationType createConstructorType(LibraryClass libraryClass, Snap.Obj constructor) {
        CombinationType constructorType = new CombinationType(typeReducer);

        constructorType.addType(getPureFunction(constructor));

        Set<UnionFeature> constructorFeatures = new HashSet<>(libraryClass.constructorNodes.stream().map(UnionNode::getFeature).map(UnionFeature::getReachable).reduce(new ArrayList<>(), Util::reduceList));

        List<UnionFeature.FunctionFeature> constructorFunctionFeatures = constructorFeatures.stream().filter(feature -> feature.getFunctionFeature() != null).map(UnionFeature::getFunctionFeature).collect(Collectors.toList());
        constructorFunctionFeatures.forEach(constructorFeature -> {
            createFunctionType(constructorFeature).forEach(constructorType::addType);
        });
        return constructorType;
    }

    private Map<String, DeclarationType> createClassFields(LibraryClass libraryClass, Snap.Obj constructor) {
        Map<String, DeclarationType> fieldTypes = new HashMap<>();
        libraryClass.prototype.getPropertyMap().forEach((name, prop) -> {
            if (!name.equals("constructor")) {
                fieldTypes.put(name, getHeapPropType(prop));
            }
        });

        Multimap<String, UnionNode> thisNodePropertyMap = ArrayListMultimap.create();
        for (UnionNode thisNode : libraryClass.thisNodes) {
            for (Map.Entry<String, UnionNode> entry : thisNode.getFeature().getObjectFields().entrySet()) {
                thisNodePropertyMap.put(entry.getKey(), entry.getValue());
            }
        }

        if (options.classOptions.useClassInstancesFromHeap && (!libraryClass.instances.isEmpty() || constructor.function.instance != null)) {
            Multimap<String, Snap.Property> propertiesMultimap = ArrayListMultimap.create();

            List<Snap.Obj> instances = new ArrayList<>(libraryClass.instances);
            if (constructor.function.instance != null) {
                instances.add(constructor.function.instance);
            }

            for (Snap.Obj instance : instances) {
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
                DeclarationType fieldType = getHeapPropType(properties);
                if (fieldType == PrimitiveDeclarationType.Void()) {
                    fieldType = PrimitiveDeclarationType.NonVoid();
                }
                fieldTypes.put(name, fieldType);
                if (fieldType == PrimitiveDeclarationType.NonVoid()) {
                    if (thisNodePropertyMap.containsKey(name)) {
                        fieldTypes.put(name, getType(thisNodePropertyMap.get(name)));
                    }
                }
            }
        } else {
            for (Map.Entry<String, Collection<UnionNode>> entry : thisNodePropertyMap.asMap().entrySet()) {
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
            return PrimitiveDeclarationType.Void();
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
        TypeAnalysis typeAnalysis = new TypeAnalysis(libraryClasses, options, globalObject, this, nativeClasses);
        UnionFindSolver solver = new UnionFindSolver();
        HeapValueFactory heapFactory = new HeapValueFactory(globalObject, solver, libraryClasses, typeAnalysis, new HashSet<>());
        UnionNode unionNode = heapFactory.fromProperty(prop);
        solver.finish();
        return getType(unionNode);
    }

    public Snap.Obj currentClosure; // Set by TypeAnalysis, to set which closure we have just finished analyzing, and therefore should create the type of.
    public List<DeclarationType> createFunctionType(UnionFeature.FunctionFeature feature) {
        if (!feature.getClosures().isEmpty()) {
            return feature.getClosures().stream().map(closure -> {
                boolean isUserDefined = closure.function.type.equals("user") || closure.function.type.equals("bind");
                if (currentClosure != closure && isUserDefined) {
                    return getFunctionType(closure);
                } else {
                    if (isUserDefined) {
                        return getPureFunction(closure);
                    } else {
                        return getNativeFunctionType(closure);
                    }
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

    private DeclarationType getNativeFunctionType(Snap.Obj closure) {
        if (pureFunctionCache.containsKey(closure)) {
            return getPureFunction(closure);
        }
        if (closure.function.callSignatures.isEmpty()) {
            return new FunctionType(PrimitiveDeclarationType.Void(), Collections.EMPTY_LIST);
        }
        UnionFindSolver solver = new UnionFindSolver();
        NativeTypeFactory factory = new NativeTypeFactory(new PrimitiveNode.Factory(solver, globalObject), solver, nativeClasses);
        EmptyNode unionNode = new EmptyNode(solver);
        for (Signature signature : closure.function.callSignatures) {
            FunctionNode functionNode = factory.fromSignature(signature, closure, null);
            solver.union(unionNode, functionNode);
        }
        solver.finish();

        registerFunction(closure, unionNode.getFeature().getFunctionFeature());
        return getPureFunction(closure);
    }

    // This is only used, when we KNOW that we want a functionType. So only from createFunctionType and when we need a type for the constructor.
    private Map<Snap.Obj, UnresolvedDeclarationType> pureFunctionCache = new HashMap<>();

    // This is only used, when we KNOW that we want a functionType. So only from createFunctionType and when we need a type for the constructor.
    private UnresolvedDeclarationType getPureFunction(Snap.Obj closure) {
        return Util.getWithDefault(pureFunctionCache, closure, new UnresolvedDeclarationType());
    }

    public void registerFunction(Snap.Obj closure, UnionFeature.FunctionFeature feature) {
        UnresolvedDeclarationType unresolved = getPureFunction(closure);

        DeclarationType returnType = getType(feature.getReturnNode());

        List<FunctionType.Argument> arguments = feature.getArguments().stream().map((arg) -> {
            DeclarationType type = getType(arg.node);
            return new FunctionType.Argument(arg.name, type);
        }).collect(Collectors.toList());

        FunctionType result = new FunctionType(returnType, arguments);

        unresolved.setResolvedType(result);
    }
}
