package dk.webbies.tscreate.main.normalizeDec;

import dk.au.cs.casa.typescript.types.*;
import dk.au.cs.casa.typescript.types.ClassType;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.EMPTY_SET;

/**
 * Created by erik1 on 15-03-2016.
 */
public class ToDeclarationTypeVisitor implements TypeVisitor<DeclarationType> {
    private final TypeReducer reducer;
    private final Map<Type, String> libraryTypeNames;
    private Map<Type, String> typeNames;
    private Map<DeclarationType, List<DeclarationType>> interfaceExtensions = new HashMap<>();
    private List<Pair<InterfaceType, dk.webbies.tscreate.analysis.declarations.types.ClassType>> classesForLater = new ArrayList<>();
    public final Map<InterfaceDeclarationType, ClassInstanceType> interfaceToClassInstanceMap = new HashMap<>();

    public ToDeclarationTypeVisitor(Map<Type, String> libraryTypeNames, Map<Type, String> allTypeNames, TypeReducer reducer) {
        this.typeNames = allTypeNames;
        this.reducer = reducer;
        this.libraryTypeNames = libraryTypeNames;
        for (Map.Entry<Type, String> entry : this.libraryTypeNames.entrySet()) {
            cache.put(entry.getKey(), new NamedObjectType(entry.getValue(), false));
        }
    }

    public Map<DeclarationType, List<DeclarationType>> getInterfaceExtensions() {
        return interfaceExtensions;
    }

    private Set<String> name(Type type) {
        if (typeNames.containsKey(type)) {
            return new HashSet<>(Arrays.asList(typeNames.get(type)));
        } else {
            return EMPTY_SET;
        }
    }

    public void resolveClassHierarchy() {
        Map<Type, dk.webbies.tscreate.analysis.declarations.types.ClassType> classesMap = new HashMap<>();
        for (Pair<InterfaceType, dk.webbies.tscreate.analysis.declarations.types.ClassType> pair : this.classesForLater) {
            Type instanceType = pair.left.getDeclaredConstructSignatures().iterator().next().getResolvedReturnType();
            if (!(instanceType instanceof SimpleType)) {
                classesMap.put(instanceType, pair.right);
            }
        }

        for (Pair<InterfaceType, dk.webbies.tscreate.analysis.declarations.types.ClassType> pair : classesForLater) {
            Type uncastInstancetype = pair.left.getDeclaredConstructSignatures().iterator().next().getResolvedReturnType();
            if (uncastInstancetype instanceof SimpleType || uncastInstancetype == null) {
                continue;
            }
            if (uncastInstancetype instanceof ReferenceType) {
                uncastInstancetype = ((ReferenceType) uncastInstancetype).getTarget();
            }
            if (uncastInstancetype instanceof TypeParameterType) {
                uncastInstancetype = ((TypeParameterType) uncastInstancetype).getConstraint();
            }
            if (uncastInstancetype instanceof GenericType) {
                uncastInstancetype = ((GenericType) uncastInstancetype).toInterface();
            }
            InterfaceType instanceType = (InterfaceType) uncastInstancetype;
            if (instanceType.getBaseTypes() != null && !instanceType.getBaseTypes().isEmpty()) {
                if (instanceType.getBaseTypes().size() == 1) {
                    Type superType = instanceType.getBaseTypes().iterator().next();
                    if (libraryTypeNames.containsKey(superType)) {
                        String name = libraryTypeNames.get(superType);
                        pair.right.setSuperClass(new NamedObjectType(name, false));
                    } else {
                        dk.webbies.tscreate.analysis.declarations.types.ClassType superClass = classesMap.get(superType);
                        pair.right.setSuperClass(superClass);
                    }
                } else {
                    List<Type> filtered = instanceType.getBaseTypes().stream().filter(classesMap::containsKey).collect(Collectors.toList());
                    if (filtered.size() == 0) {
                        pair.right.setSuperClass(null);
                    } else  if (filtered.size() == 1) {
                        pair.right.setSuperClass(classesMap.get(filtered.iterator().next()));
                    } else {
                        throw new RuntimeException();
                    }
                }
            }
        }
    }

    private Map<Type, DeclarationType> cache = new HashMap<>();
    private List<Pair<Type, DeclarationType>> finalizationQueue = new ArrayList<>();
    private void register(Type type, DeclarationType dec) {
        assert !cache.containsKey(type);
        assert !cache.containsValue(dec);

        cache.put(type, dec);
        finalizationQueue.add(new Pair<>(type, dec));
    }


    public void finish() {
        while (!finalizationQueue.isEmpty()) {
            ArrayList<Pair<Type, DeclarationType>> copy = new ArrayList<>(finalizationQueue);
            finalizationQueue.clear();
            for (Pair<Type, DeclarationType> pair : copy) {
                pair.left.accept(new FinalizationVisitor(), pair.right);
            }

        }
    }

    private class FinalizationVisitor implements TypeVisitorWithArgument<Void,DeclarationType> {
        @Override
        public Void visit(AnonymousType t, DeclarationType type) {
            throw new RuntimeException();
        }

        @Override
        public Void visit(ClassType t, DeclarationType type) {
            throw new RuntimeException();
        }

        @Override
        public Void visit(GenericType t, DeclarationType type) {
            throw new RuntimeException();
        }

        @Override
        public Void visit(InterfaceType t, DeclarationType type) {
            Map<String, DeclarationType> properties = t.getDeclaredProperties().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().accept(ToDeclarationTypeVisitor.this)));
            if (t.getDeclaredConstructSignatures().isEmpty()) {
                InterfaceDeclarationType inter = (InterfaceDeclarationType) type;
                if (!properties.isEmpty()) {
                    inter.setObject(new UnnamedObjectType(properties, name(t)));
                }
                if (!t.getDeclaredCallSignatures().isEmpty()) {
                    CombinationType functions = new CombinationType(reducer, t.getDeclaredCallSignatures().stream().map(ToDeclarationTypeVisitor.this::toFunction).collect(Collectors.toList()));
                    ((InterfaceDeclarationType) type).setFunction((FunctionType) functions.getCombined());
                }
                if (t.getDeclaredNumberIndexType() != null) {
                    inter.setDynamicAccess(new DynamicAccessType(PrimitiveDeclarationType.Number(EMPTY_SET), t.getDeclaredNumberIndexType().accept(ToDeclarationTypeVisitor.this), name(t)));
                }
                if (t.getDeclaredStringIndexType() != null) {
                    inter.setDynamicAccess(new DynamicAccessType(PrimitiveDeclarationType.String(EMPTY_SET), t.getDeclaredStringIndexType().accept(ToDeclarationTypeVisitor.this), name(t)));
                }
                if (t.getBaseTypes() != null && !t.getBaseTypes().isEmpty()) {
                    ToDeclarationTypeVisitor.this.interfaceExtensions.put(type, t.getBaseTypes().stream().map(tstype -> tstype.accept(ToDeclarationTypeVisitor.this)).collect(Collectors.toList()));
                }
                return null;
            } else {
                throw new RuntimeException();
            }
        }

        @Override
        public Void visit(ReferenceType t, DeclarationType type) {
            throw new RuntimeException();
        }

        @Override
        public Void visit(SimpleType t, DeclarationType type) {
            throw new RuntimeException();
        }

        @Override
        public Void visit(TupleType t, DeclarationType type) {
            throw new RuntimeException();
        }

        @Override
        public Void visit(UnionType t, DeclarationType type) {
            throw new RuntimeException();
        }

        @Override
        public Void visit(UnresolvedType t, DeclarationType type) {
            throw new RuntimeException();
        }

        @Override
        public Void visit(TypeParameterType t, DeclarationType type) {
            throw new RuntimeException();
        }

        @Override
        public Void visit(SymbolType t, DeclarationType type) {
            throw new RuntimeException();
        }
    }

    private FunctionType toFunction(Signature signature) {
        List<FunctionType.Argument> arguments = new ArrayList<>();
        for (Signature.Parameter parameter : signature.getParameters()) {
            arguments.add(new FunctionType.Argument(parameter.getName(), parameter.getType().accept(ToDeclarationTypeVisitor.this)));
        }

        FunctionType result = new FunctionType(null, signature.getResolvedReturnType().accept(ToDeclarationTypeVisitor.this), arguments, EMPTY_SET);
        result.minArgs = signature.getMinArgumentCount();
        return result;
    }

    @Override
    public DeclarationType visit(AnonymousType t) {
        throw new RuntimeException();
    }

    @Override
    public DeclarationType visit(ClassType t) {
        throw new RuntimeException();
    }

    @Override
    public DeclarationType visit(GenericType t) {
        if (cache.containsKey(t)) {
            return cache.get(t);
        }
        DeclarationType result = t.toInterface().accept(this);
        cache.put(t, result);
        return result;
    }

    @Override
    public DeclarationType visit(InterfaceType t) {
        if (t.getDeclaredProperties().isEmpty() && t.getDeclaredCallSignatures().isEmpty() && t.getBaseTypes().isEmpty() && t.getDeclaredStringIndexType() == null && t.getDeclaredNumberIndexType() == null && t.getDeclaredConstructSignatures().isEmpty()) {
            return new SimpleType(SimpleTypeKind.Any).accept(this);
        }
        if (cache.containsKey(t)) {
            return cache.get(t);
        }
        if (t.getDeclaredConstructSignatures().isEmpty()) {
            InterfaceDeclarationType result = new InterfaceDeclarationType(name(t));
            register(t, result);
            return result;
        } else {
            String name = new InterfaceDeclarationType(null, Collections.EMPTY_SET).name;
            dk.webbies.tscreate.analysis.declarations.types.ClassType clazz = new dk.webbies.tscreate.analysis.declarations.types.ClassType(name, new FunctionType(null, PrimitiveDeclarationType.Void(EMPTY_SET), new ArrayList<>(), EMPTY_SET), new HashMap<>(), new HashMap<>(), null);

            cache.put(t, clazz);

            FunctionType constructorFunction = toFunction(t.getDeclaredConstructSignatures().iterator().next());


            Map<String, DeclarationType> staticFields = t.getDeclaredProperties().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().accept(ToDeclarationTypeVisitor.this)));

            finish();

            DeclarationType uncastReturnType = constructorFunction.getReturnType().resolve();
            InterfaceDeclarationType returnType;
            if (uncastReturnType instanceof PrimitiveDeclarationType /* Happens if literally empty */|| uncastReturnType instanceof dk.webbies.tscreate.analysis.declarations.types.ClassType /*Not sure when this happens */) {
                returnType = new InterfaceDeclarationType(null, EMPTY_SET);
            } else {
                if (uncastReturnType instanceof NamedObjectType) {
                    returnType = new InterfaceDeclarationType(Collections.EMPTY_SET); // Enough of an edge-case, only happens in Jasmine.
                } else {
                    returnType = (InterfaceDeclarationType) uncastReturnType;
                }
            }

            interfaceToClassInstanceMap.put(returnType, clazz.getEmptyNameInstance());

            Map<String, DeclarationType> declarations = new HashMap<>();
            if (returnType.object != null) {
                declarations = returnType.getObject().getDeclarations();
            }
            clazz.setConstructorType(constructorFunction);
            clazz.setPrototypeFields(declarations);
            clazz.setStaticFields(staticFields);

            this.classesForLater.add(new Pair<>(t, clazz));

            return clazz;
        }
    }

    @Override
    public DeclarationType visit(ReferenceType t) {
        if (cache.containsKey(t)) {
            return cache.get(t);
        }
        DeclarationType result = t.getTarget().accept(this);
        if ("Array".equals(typeNames.get(t.getTarget())) && t.getTypeArguments().size() == 1) {
            DeclarationType arrayType = t.getTypeArguments().iterator().next().accept(this);
            result = new CombinationType(reducer, result, new DynamicAccessType(PrimitiveDeclarationType.Number(EMPTY_SET), arrayType, EMPTY_SET));
        }
        cache.put(t, result);
        return result;
    }

    @Override
    public DeclarationType visit(SimpleType t) {
        switch (t.getKind()) {
            case Any: return PrimitiveDeclarationType.Any(EMPTY_SET);
            case Boolean: return PrimitiveDeclarationType.Boolean(EMPTY_SET);
            case Enum: return PrimitiveDeclarationType.Number(EMPTY_SET);
            case Null: return PrimitiveDeclarationType.NonVoid(EMPTY_SET);
            case Number: return PrimitiveDeclarationType.Number(EMPTY_SET);
            case String: return PrimitiveDeclarationType.String(EMPTY_SET);
            case Undefined: return PrimitiveDeclarationType.NonVoid(EMPTY_SET);
            case Void: return PrimitiveDeclarationType.Void(EMPTY_SET);
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public DeclarationType visit(TupleType t) {
        return new NamedObjectType("Array", false);
    }

    @Override
    public DeclarationType visit(UnionType t) {
        List<DeclarationType> types = t.getElements().stream().map(type -> type.accept(this)).collect(Collectors.toList());
        return new UnionDeclarationType(types);
    }

    @Override
    public DeclarationType visit(UnresolvedType t) {
        throw new RuntimeException();
    }

    @Override
    public DeclarationType visit(TypeParameterType t) {
        return new SimpleType(SimpleTypeKind.Any).accept(this);
    }

    @Override
    public DeclarationType visit(SymbolType t) {
        throw new RuntimeException();
    }
}
