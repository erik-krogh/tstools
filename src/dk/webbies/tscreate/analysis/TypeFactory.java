package dk.webbies.tscreate.analysis;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers.FunctionReducer;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.analysis.unionFind.UnionClass;
import dk.webbies.tscreate.analysis.unionFind.UnionFeature;
import dk.webbies.tscreate.analysis.unionFind.UnionFeature.FunctionFeature;
import dk.webbies.tscreate.analysis.unionFind.UnionNode;
import dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class TypeFactory {
    private final Map<UnionClass, DeclarationType> cache = new HashMap<>();
    private final NativeClassesMap nativeClasses;
    private final HashSet<String> takenClassNames = new HashSet<>();
    private TypeAnalysis typeAnalysis;
    private NativeTypeFactory nativeTypeFactory;
    private HashMap<Snap.Obj, LibraryClass> libraryClasses;
    private Options options;
    public final TypeReducer typeReducer;

    public TypeFactory(Snap.Obj globalObject, HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, NativeClassesMap nativeClasses, TypeAnalysis typeAnalysis, NativeTypeFactory nativeTypeFactory) {
        this.libraryClasses = libraryClasses;
        this.options = options;
        this.nativeClasses = nativeClasses;
        this.typeAnalysis = typeAnalysis;
        this.nativeTypeFactory = nativeTypeFactory;
        this.typeReducer = new TypeReducer(globalObject, nativeClasses, options);
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
//        unionClass.getStronglyConnectedComponents(); // FIXME: Look at this.
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
            for (UnionNode include : feature.unionClass.includes) {
                result.addType(getType(include));
            }
        }

        // Adding primitives
        feature.getPrimitives().stream().map(PrimitiveDeclarationType::fromType).forEach(result::addType);

        // Adding function
        if (feature.getFunctionFeature() != null) {
            createFunctionType(feature.getFunctionFeature()).forEach(result::addType);
        }

        // Adding names object types.
        feature.getTypeNames().stream().filter(Objects::nonNull).map(NamedObjectType::new).forEach(result::addType);

        // Adding object instance
        getObjectInstanceType(feature).forEach(result::addType);

        // Adding class declaration (if it is a constructor).
        if (feature.getFunctionFeature() != null) {
            feature.getFunctionFeature().getClosures().stream()
                .filter(obj -> obj.function != null && obj.getProperty("prototype") != null)
                .map(obj -> obj.getProperty("prototype").value)
                .distinct()
                .map(this.libraryClasses::get)
                .filter(Objects::nonNull)
                .filter(clazz -> !clazz.isNativeClass())
                .map(this::getClassType)
                .filter(Objects::nonNull)
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
            DynamicAccessType dynamicAccessType = new DynamicAccessType(getType(lookupType), getType(returnType));
            InterfaceType dynamicInterface = new InterfaceType();
            dynamicInterface.dynamicAccess = dynamicAccessType;
            result.addType(dynamicInterface);
        }

        if (result.types.size() == 0) {
            return PrimitiveDeclarationType.Void();
        } else if (result.types.size() == 1 && !(result.types.get(0) instanceof UnresolvedDeclarationType)) {
            return result.types.get(0);
        }
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
                .filter(Objects::nonNull)
                .map(name -> name.endsWith("Constructor") ? Util.removeSuffix(name, "Constructor") : name)
                .map(NamedObjectType::new)
                .map(TypeFactory::filterPrimitives)
                .filter(Objects::nonNull)
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

    private Map<LibraryClass, UnresolvedDeclarationType> libraryClassCache = new HashMap<>();

    private DeclarationType getClassType(LibraryClass libraryClass) {
        if (libraryClass == null) {
            return null;
        }
        // FIXME: The heuristic needs to be revised after the ts-spec-reader bug has been fixed. Consider what the upper-case name policy should be.
        boolean hardCodedClassName = false;
        if (options.isClassNames.stream().anyMatch(str -> str.equals(libraryClass.getName(nativeClasses, takenClassNames)))) {
            hardCodedClassName = true;
            libraryClass.isUsedAsClass = true;
        }
        if (!libraryClass.isUsedAsClass && libraryClass.getUniqueConstructionSite().isEmpty()) {
            return null;
        }
        if (!hardCodedClassName) {
            String firstLetter = libraryClass.getName(nativeClasses, takenClassNames).substring(0, 1);
            if (!firstLetter.equals(firstLetter.toUpperCase()) && libraryClass.getUniqueConstructionSite().isEmpty()) {
                return null;
            }
        }
        Snap.Obj constructor = libraryClass.getConstructor();
        if (constructor == null) {
            return null;
        }

        switch (constructor.function.type) {
            case "native":
                // Native classes should be handled before entering this method.
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
        Snap.Obj constructor = libraryClass.getConstructor();
        switch (constructor.function.type) {
            case "user":
                CombinationType constructorType = createConstructorType(libraryClass, constructor);

                Map<String, DeclarationType> staticFields = new HashMap<>();
                for (Map.Entry<String, Snap.Property> entry : constructor.getPropertyMap().entrySet()) {
                    if (Arrays.asList("prototype", "caller", "length", "name", "arguments").stream().noneMatch(str -> str.equals(entry.getKey()))) {
                        staticFields.put(entry.getKey(), getHeapPropType(entry.getValue()));
                    }
                }

                Map<String, DeclarationType> prototypeProperties = createClassFields(libraryClass);

                ClassType classType = new ClassType(libraryClass.getName(nativeClasses, takenClassNames), constructorType, prototypeProperties, staticFields);
                if (libraryClass.superClass != null) {
                    if (libraryClass.superClass.isNativeClass()) {
                        classType.setSuperClass(new NamedObjectType(nativeClasses.nameFromPrototype(libraryClass.superClass.prototype)));
                    } else {
                        classType.setSuperClass(getClassType(libraryClass.superClass));
                    }
                }
                return classType;
            default: // Case "native" should already be handled here.
                throw new UnsupportedOperationException("unhandled type for creating a class type: " + constructor.function.type);
        }
    }

    private CombinationType createConstructorType(LibraryClass libraryClass, Snap.Obj constructor) {
        CombinationType constructorType = new CombinationType(typeReducer);

        constructorType.addType(getPureFunction(constructor));

        Set<UnionFeature> constructorFeatures = new HashSet<>(libraryClass.constructorNodes.stream().map(UnionNode::getFeature).map(UnionFeature::getReachable).reduce(new ArrayList<>(), Util::reduceList));

        List<FunctionFeature> constructorFunctionFeatures = constructorFeatures.stream().filter(feature -> feature.getFunctionFeature() != null).map(UnionFeature::getFunctionFeature).collect(Collectors.toList());
        constructorFunctionFeatures.forEach(constructorFeature -> {
            createFunctionType(constructorFeature).forEach(constructorType::addType);
        });
        return constructorType;
    }

    private Map<String, DeclarationType> createClassFields(LibraryClass libraryClass) {
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

        List<Snap.Obj> instancesToFindFieldNames = new ArrayList<>(libraryClass.getInstances());
        if (libraryClass.getConstructor().function.instance != null) {
            instancesToFindFieldNames.add(libraryClass.getConstructor().function.instance);
        }
        instancesToFindFieldNames.removeAll(libraryClasses.keySet()); // Sometimes the prototype of one Class, is an instance of another Class. Those are not "valid" instances, so we filter them out.
        instancesToFindFieldNames = instancesToFindFieldNames.stream().filter(instance -> instance.prototype != null && instance.prototype == libraryClass.prototype).collect(Collectors.toList()); // Sub-classes are also instances, but these contain to many fields.

        if (options.classOptions.useClassInstancesFromHeap && !instancesToFindFieldNames.isEmpty()) {
            Multimap<String, Snap.Property> propertiesMultimap = ArrayListMultimap.create();

            Set<String> fieldNames = new HashSet<>();

            for (Snap.Obj instance : instancesToFindFieldNames) {
                for (Snap.Property property : instance.properties) {
                    fieldNames.add(property.name);
                }
            }
            // Now we have which names are right. From here we take all instances, to maximize the sources we have to get the correct type.
            for (Snap.Obj obj : libraryClass.getInstances()) {
                for (String fieldName : fieldNames) {
                    Snap.Property prop = obj.getProperty(fieldName);
                    if (prop != null) {
                        propertiesMultimap.put(fieldName, prop);
                    }
                }
            }

            for (Map.Entry<String, Collection<Snap.Property>> entry : propertiesMultimap.asMap().entrySet()) {
                String name = entry.getKey();
                Collection<Snap.Property> properties = entry.getValue();
                if (name.equals("constructor") || fieldTypes.containsKey(name)) {
                    continue;
                }
                fieldTypes.put(name, getClassFieldType(name, properties, libraryClass.thisNodes));
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

    private DeclarationType getClassFieldType(String name, Collection<Snap.Property> properties, List<UnionNode> thisNodes) {
        assert !properties.isEmpty();
        CombinationType result = new CombinationType(typeReducer);
        boolean onlyVoid = true;
        for (Snap.Property property : properties) {
            DeclarationType type = getHeapPropType(property);
            if (!(property.value instanceof Snap.UndefinedConstant)) {
                onlyVoid = false;
            }
            result.addType(type);
        }
        result.addType(PrimitiveDeclarationType.NonVoid());

        if (onlyVoid) {
            for (UnionNode thisNode : thisNodes) {
                Map<String, UnionNode> objectFields = thisNode.getFeature().getObjectFields();
                if (objectFields.containsKey(name)) {
                    result.addType(getType(objectFields.get(name)));
                }
            }
        }

        return result;
    }

    public DeclarationType getHeapPropType(Snap.Property prop) {
        return getType(typeAnalysis.heapFactory.fromProperty(prop));
    }

    public Snap.Obj currentClosure; // Set by TypeAnalysis, to set which closure we have just finished analyzing, and therefore should create the type of.
    public List<DeclarationType> createFunctionType(FunctionFeature feature) {
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
            return new FunctionType(PrimitiveDeclarationType.Void(), new ArrayList<>());
        }
        throw new RuntimeException("Should have gotten the types of all functions by now. Callsigs: " + closure.function.callSignatures.size());
    }

    // This is only used, when we KNOW that we want a functionType. So only from createFunctionType and when we need a type for the constructor.
    private Map<Snap.Obj, UnresolvedDeclarationType> pureFunctionCache = new HashMap<>();

    // This is only used, when we KNOW that we want a functionType. So only from createFunctionType and when we need a type for the constructor.
    private UnresolvedDeclarationType getPureFunction(Snap.Obj closure) {
        return Util.getWithDefault(pureFunctionCache, closure, new UnresolvedDeclarationType());
    }

    public void registerFunction(Snap.Obj closure, List<FunctionFeature> features) {
        UnresolvedDeclarationType unresolved = getPureFunction(closure);

        DeclarationType returnType = constructCombinationType(features, (feature) -> getType(feature.getReturnNode()));

        List<FunctionType.Argument> argumentsTypes = getArguments(features).stream().map((arguments) -> {
            String name = FunctionReducer.getBestArgumentName(arguments.stream().map(argument -> argument.name).collect(Collectors.toList()));
            return new FunctionType.Argument(name, constructCombinationType(arguments, arg -> getType(arg.node)));
        }).collect(Collectors.toList());

        FunctionType result = new FunctionType(returnType, argumentsTypes);

        unresolved.setResolvedType(result);
    }

    private<T> CombinationType constructCombinationType(Collection<T> collection, Function<T, DeclarationType> mapper) {
        return new CombinationType(typeReducer, collection.stream().map(mapper).collect(Collectors.toList()));
    }

    private static List<List<FunctionFeature.Argument>> getArguments(List<FunctionFeature> features) {
        ArrayList<List<FunctionFeature.Argument>> result = new ArrayList<>();
        for (FunctionFeature feature : features) {
            for (int i = 0; i < feature.getArguments().size(); i++) {
                FunctionFeature.Argument argument = feature.getArguments().get(i);
                if (result.size() > i) {
                    result.get(i).add(argument);
                } else {
                    result.add(new ArrayList<>(Arrays.asList(argument)));
                }
            }
        }
        return result;
    }
}
