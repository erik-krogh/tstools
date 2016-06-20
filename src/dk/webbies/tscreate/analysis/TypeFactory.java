package dk.webbies.tscreate.analysis;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dk.au.cs.casa.typescript.types.Signature;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.singleTypeReducers.FunctionReducer;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.analysis.jsdoc.JSDocParser;
import dk.webbies.tscreate.analysis.unionFind.UnionClass;
import dk.webbies.tscreate.analysis.unionFind.UnionFeature;
import dk.webbies.tscreate.analysis.unionFind.UnionFeature.FunctionFeature;
import dk.webbies.tscreate.analysis.unionFind.UnionNode;
import dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.paser.AST.Identifier;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.*;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class TypeFactory {
    private final Map<UnionClass, DeclarationType> cache = new WeakHashMap<>();
    private final NativeClassesMap nativeClasses;
    private final HashSet<String> takenClassNames = new HashSet<>();
    private TypeAnalysis typeAnalysis;
    private HashMap<Snap.Obj, LibraryClass> libraryClasses;
    private Options options;
    public final TypeReducer typeReducer;
    private final Snap.Obj globalObject;

    public TypeFactory(Snap.Obj globalObject, HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, NativeClassesMap nativeClasses, TypeAnalysis typeAnalysis) {
        this.libraryClasses = libraryClasses;
        this.options = options;
        this.nativeClasses = nativeClasses;
        this.globalObject = globalObject;
        this.typeAnalysis = typeAnalysis;
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
            return PrimitiveDeclarationType.Void(EMPTY_SET);
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

    public DeclarationType getType(UnionNode node) {
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
            for (UnionNode include : feature.unionClass.includes) {
                result.addType(getType(include));
            }
        }

        // Adding the names here, then I don't have to elsewhere in the factory.
        result.addType(PrimitiveDeclarationType.Void(feature.getNames()));

        // Adding primitives
        feature.getPrimitives().stream().map(type -> PrimitiveDeclarationType.fromType(type, EMPTY_SET)).forEach(type -> result.addType((DeclarationType)type));

        // Adding function
        if (feature.getFunctionFeature() != null) {
            createFunctionType(feature).forEach(result::addType);
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
            result.addType(new UnnamedObjectType(fields, feature.getNames()));
        }

        if (feature.getDynamicAccessLookupExp() != null) {
            UnionNode lookupType = feature.getDynamicAccessLookupExp();
            UnionNode returnType = feature.getDynamicAccessReturnType();
            DynamicAccessType dynamicAccessType = new DynamicAccessType(getType(lookupType), getType(returnType), feature.getNames());
            InterfaceDeclarationType dynamicInterface = new InterfaceDeclarationType(feature.getNames());
            dynamicInterface.dynamicAccess = dynamicAccessType;
            result.addType(dynamicInterface);
        }

        if (result.types.size() == 0) {
            return PrimitiveDeclarationType.Void(feature.getNames());
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
                .map(clazz -> new ClassInstanceType(clazz, feature.getNames()))
                .forEach(result::add);

        // Adding all the native classes
        classes.stream()
                .filter(LibraryClass::isNativeClass)
                .map(LibraryClass::getPrototype)
                .map(nativeClasses::nameFromPrototype)
                .filter(Objects::nonNull)
                .map(name -> name.endsWith("Constructor") ? Util.removeSuffix(name, "Constructor") : name)
                .map((name) -> new NamedObjectType(name, false))
                .map(TypeFactory::filterPrimitives)
                .filter(Objects::nonNull)
                .forEach(result::add);

        return result;
    }

    // Primitives are handled by the PrimitiveDeclarationType, and filtering them out here allows for "spurious" prototypes (see PrimitiveDeclarationType.STRING_OR_NUMBER
    private static DeclarationType filterPrimitives(NamedObjectType named) {
        switch (named.getName()) {
            case "Function": return new FunctionType(PrimitiveDeclarationType.Void(EMPTY_SET), EMPTY_LIST, named.getNames());
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
        boolean hardCodedClassName = false;
        if (options.isClassNames.stream().anyMatch(str -> str.equals(libraryClass.getName(nativeClasses, takenClassNames)))) {
            hardCodedClassName = true;
            libraryClass.setUsedAsClass(true);
        }
        String name = libraryClass.getName(nativeClasses, takenClassNames);
        if (!libraryClass.isUsedAsClass() && libraryClass.getUniqueConstructionSite().isEmpty()) {
            return null;
        }
        /*if (!hardCodedClassName) {
            String firstLetter = libraryClass.getName(nativeClasses, takenClassNames).substring(0, 1);
            if (!firstLetter.equals(firstLetter.toUpperCase()) && libraryClass.getUniqueConstructionSite().isEmpty()) {
                return null;
            }
        }*/
        Snap.Obj constructor = libraryClass.getConstructor();
        if (constructor == null) {
            return null;
        }

        if (name.toUpperCase().charAt(0) != name.charAt(0) && libraryClass.prototype.properties.stream().map(prop -> prop.name).filter(propName -> !propName.equals("constructor")).count() == 0) {
            if (libraryClass.prototype.prototype == ((Snap.Obj) globalObject.getProperty("Object").value).getProperty("prototype").value) {
                return null;
            }
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
        String name = libraryClass.getName(nativeClasses, takenClassNames);

        if (name.toUpperCase().charAt(0) != name.charAt(0)) {
            System.err.println("Couldn't infer a class-name that starts with an upper-case letter, class: \"" + name + "\"");
        }

        Snap.Obj constructor = libraryClass.getConstructor();
        switch (constructor.function.type) {
            case "user":
                DeclarationType constructorType = getPureFunction(constructor);

                Map<String, DeclarationType> staticFields = new HashMap<>();
                for (Map.Entry<String, Snap.Property> entry : constructor.getPropertyMap().entrySet()) {
                    if (Arrays.asList("prototype", "caller", "length", "name", "arguments").stream().noneMatch(str -> str.equals(entry.getKey()))) {
                        staticFields.put(entry.getKey(), getHeapPropType(entry.getValue()));
                    }
                }

                Map<String, DeclarationType> prototypeProperties = createClassFields(libraryClass);

                ClassType classType = new ClassType(name, constructorType, prototypeProperties, staticFields, libraryClass);
                if (libraryClass.superClass != null) {
                    if (libraryClass.superClass.isNativeClass()) {
                        String superName = nativeClasses.nameFromPrototype(libraryClass.superClass.prototype);
                        classType.setSuperClass(new NamedObjectType(superName, false));
                    } else {
                        classType.setSuperClass(getClassType(libraryClass.superClass));
                    }
                }
                return classType;
            default: // Case "native" should already be handled here.
                throw new UnsupportedOperationException("unhandled type for creating a class type: " + constructor.function.type);
        }
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
        result.addType(PrimitiveDeclarationType.NonVoid(Util.createSet(name)));

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
        return getType(typeAnalysis.getHeapFactory().fromProperty(prop));
    }

    public Snap.Obj currentClosure; // Set by TypeAnalysis, to set which closure we have just finished analyzing, and therefore should create the type of.
    public List<DeclarationType> createFunctionType(UnionFeature feature) {
        FunctionFeature functionFeature = feature.getFunctionFeature();
        if (!functionFeature.getClosures().isEmpty()) {
            return functionFeature.getClosures().stream().map(closure -> {
                boolean isUserDefined = closure.function.type.equals("user") || closure.function.type.equals("bind");
                if (currentClosure != closure && isUserDefined) {
                    return getFunctionType(closure);
                } else {
                    if (isUserDefined) {
                        return getPureFunction(closure);
                    } else {
                        return getNativeFunctionType(closure, feature);
                    }
                }
            }).collect(Collectors.toList());
        } else {
            DeclarationType returnType = getType(functionFeature.getReturnNode());

            List<FunctionType.Argument> arguments = functionFeature.getArguments().stream().map((arg) -> {
                DeclarationType type = getType(arg);
                return new FunctionType.Argument(FunctionReducer.getBestArgumentName(arg.getFeature().getNames()), type);
            }).collect(Collectors.toList());

            return singletonList(new FunctionType(returnType, arguments, feature.getNames()));
        }
    }

    private DeclarationType getNativeFunctionType(Snap.Obj closure, UnionFeature feature) {
        if (pureFunctionCache.containsKey(closure)) {
            return getPureFunction(closure);
        }
        if (closure.function.callSignatures.isEmpty()) {
            return new FunctionType(PrimitiveDeclarationType.Void(EMPTY_SET), new ArrayList<>(), feature.getNames());
        }
        throw new RuntimeException("Should have gotten the types of all functions by now. Callsigs: " + closure.function.callSignatures.size());
    }

    // This is only used, when we KNOW that we want a functionType. So only from createFunctionType and when we need a type for the constructor.
    private Map<Snap.Obj, UnresolvedDeclarationType> pureFunctionCache = new HashMap<>();

    // This is only used, when we KNOW that we want a functionType. So only from createFunctionType and when we need a type for the constructor.
    private UnresolvedDeclarationType getPureFunction(Snap.Obj closure) {
        return Util.getWithDefault(pureFunctionCache, closure, new UnresolvedDeclarationType());
    }

    public void registerFunction(Snap.Obj closure, List<UnionFeature> features) {
        UnresolvedDeclarationType unresolved = getPureFunction(closure);
        if (options.useJSDoc && closure.function.astNode != null && closure.function.astNode.jsDoc != null) {
            FunctionType result = new JSDocParser(globalObject, typeAnalysis, nativeClasses, typeReducer).parseFunctionDoc(closure, closure.function.astNode.jsDoc);

            FunctionType analyzedResult = functionFeatureToDec(closure, features);

            if (isVoidOrNull(result.getReturnType())) {
                result.setReturnType(analyzedResult.getReturnType());
            }

            // If JSDoc couldn't figure it out, fallback.
            for (int i = 0; i < Math.min(analyzedResult.getArguments().size(), result.getArguments().size()); i++) {
                if (isVoidOrNull(result.getArguments().get(i).getType())) {
                    result.getArguments().get(i).setType(analyzedResult.getArguments().get(i).getType());
                }
            }

            // Making sure that if the analysis sees more arguments than the JSDoc, then those are added.
            for (int i = result.getArguments().size(); i < analyzedResult.getArguments().size(); i++) {
                result.getArguments().add(analyzedResult.getArguments().get(i));
            }

            result.setArguments(result.getArguments().stream().filter(arg -> arg.getType() != null).collect(Collectors.toList()));

            unresolved.setResolvedType(result);

        } else {
            FunctionType result = functionFeatureToDec(closure, features);

            unresolved.setResolvedType(result);
        }
    }

    private boolean isVoidOrNull(DeclarationType type) {
        if (type instanceof UnionDeclarationType) {
            return ((UnionDeclarationType) type).getTypes().stream().allMatch(this::isVoidOrNull);
        }
        return type == null || type instanceof PrimitiveDeclarationType && ((PrimitiveDeclarationType) type).getType() == PrimitiveDeclarationType.Type.VOID;
    }

    private FunctionType functionFeatureToDec(Snap.Obj closure, List<UnionFeature> features) {
        List<FunctionFeature> functionFeatures = features.stream().map(UnionFeature::getFunctionFeature).filter(Objects::nonNull).collect(Collectors.toList());

        DeclarationType returnType = constructCombinationType(functionFeatures, (feature) -> getType(feature.getReturnNode()));

        List<String> argumentNames = new ArrayList<>();
        switch (closure.function.type) {
            case "bind":
                closure = closure.function.target;
                /* FALLTHROUGH */
            case "user":
                closure.function.astNode.getArguments().stream().map(Identifier::getName).forEach(argumentNames::add);
                break;
            case "native":
                if (!closure.function.callSignatures.isEmpty()) {
                    Signature signature = Collections.max(closure.function.callSignatures, (a, b) -> Integer.compare(a.getParameters().size(), b.getParameters().size()));
                    signature.getParameters().stream().map(Signature.Parameter::getName).forEach(argumentNames::add);
                }
                break;
            case "unknown":
                break;
            default:
                throw new RuntimeException();
        }

        final int[] argCount = {0};
        List<FunctionType.Argument> argumentsTypes = getArguments(functionFeatures).stream().map((arguments) -> {
            String name;
            if (argumentNames.size() > argCount[0]) {
                name = argumentNames.get(argCount[0]);
            } else {
                name = FunctionReducer.getBestArgumentName(arguments.stream().map(argument -> argument.getFeature().getNames()).reduce(new HashSet<>(), Util::reduceSet));
            }

            argCount[0]++;
            return new FunctionType.Argument(name, constructCombinationType(arguments, this::getType));
        }).collect(Collectors.toList());

        FunctionType result = new FunctionType(returnType, argumentsTypes, features.stream().map(UnionFeature::getNames).reduce(new HashSet<>(), Util::reduceSet));

        if (closure.recordedCalls != null) {
            result.minArgs = Integer.MAX_VALUE;
            for (JSNAPUtil.RecordedCall call : JSNAPUtil.getCalls(closure.recordedCalls)) {
                result.minArgs = Math.min(result.minArgs, call.arguments.size());
            }
        }
        return result;
    }

    private<T> CombinationType constructCombinationType(Collection<T> collection, Function<T, DeclarationType> mapper) {
        return new CombinationType(typeReducer, collection.stream().map(mapper).collect(Collectors.toList()));
    }

    private static List<List<UnionNode>> getArguments(List<FunctionFeature> features) {
        ArrayList<List<UnionNode>> result = new ArrayList<>();
        for (FunctionFeature feature : features) {
            for (int i = 0; i < feature.getArguments().size(); i++) {
                UnionNode argument = feature.getArguments().get(i);
                if (result.size() > i) {
                    result.get(i).add(argument);
                } else {
                    result.add(new ArrayList<>(singletonList(argument)));
                }
            }
        }
        return result;
    }
}
