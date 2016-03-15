package dk.webbies.tscreate.cleanup;

import dk.au.cs.casa.typescript.types.*;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.analysis.declarations.types.ClassType;
import dk.webbies.tscreate.analysis.declarations.types.InterfaceType;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by erik1 on 11-03-2016.
 */
public class DeclarationTypeToTSTypes implements DeclarationTypeVisitor<Type> {
    private final DeclarationParser.NativeClassesMap nativeClasses;
    private final Map<DeclarationType, Type> cache = new HashMap<>();
    private final List<Pair<Type, DeclarationType>> finalizationQueue = new ArrayList<>();

    public DeclarationTypeToTSTypes(DeclarationParser.NativeClassesMap nativeClasses) {
        this.nativeClasses = nativeClasses;
    }

    public Type getType(DeclarationType declaration) {
        if (this.cache.containsKey(declaration)) {
            return this.cache.get(declaration);
        } else {
            Type result = declaration.accept(this);
            finishQueue();
            return result;
        }
    }

    private void finishQueue() {
        FinalizationVisitor finalizationVisitor = new FinalizationVisitor();
        while (!finalizationQueue.isEmpty()) {
            ArrayList<Pair<Type, DeclarationType>> copy = new ArrayList<>(finalizationQueue);
            finalizationQueue.clear();
            copy.forEach(pair -> {
                pair.first.accept(finalizationVisitor, pair.second);
            });
        }
    }

    private final class FinalizationVisitor implements TypeVisitorWithArgument<Void, DeclarationType> {

        @Override
        public Void visit(AnonymousType t, DeclarationType type) {
            throw new RuntimeException();
        }

        @Override
        public Void visit(dk.au.cs.casa.typescript.types.ClassType t, DeclarationType type) {
            throw new RuntimeException();
        }

        @Override
        public Void visit(GenericType t, DeclarationType type) {
            throw new RuntimeException();
        }

        @Override
        public Void visit(dk.au.cs.casa.typescript.types.InterfaceType t, DeclarationType type) {
            t.setDeclaredProperties(new HashMap<>());
            if (type instanceof FunctionType) {
                addFunctionToInterface(t, (FunctionType) type);
                return null;
            } else if (type instanceof UnnamedObjectType) {
                t.setDeclaredProperties(convertProperties(((UnnamedObjectType) type).getDeclarations()));
                return null;
            } else if (type instanceof InterfaceType) {
                InterfaceType inter = (InterfaceType) type;
                if (inter.getObject() != null) {
                    t.setDeclaredProperties(convertProperties(inter.getObject().getDeclarations()));
                }
                if (inter.getFunction() != null) {
                    addFunctionToInterface(t, inter.getFunction());
                }
                if (inter.getDynamicAccess() != null) {
                    if (inter.getDynamicAccess().isNumberIndexer()) {
                        t.setDeclaredNumberIndexType(inter.getDynamicAccess().getReturnType().accept(DeclarationTypeToTSTypes.this));
                    } else {
                        t.setDeclaredStringIndexType(inter.getDynamicAccess().getReturnType().accept(DeclarationTypeToTSTypes.this));
                    }
                }
                return null;
            } else if (type instanceof ClassType) {
                ClassType clazz = (ClassType) type;
                t.setDeclaredProperties(convertProperties(clazz.getStaticFields()));

                Signature constructor = toSignature(clazz.getConstructorType());
                t.setDeclaredConstructSignatures(Collections.singletonList(constructor));

                dk.au.cs.casa.typescript.types.InterfaceType objectType = new dk.au.cs.casa.typescript.types.InterfaceType();
                objectType.setDeclaredProperties(convertProperties(clazz.getPrototypeFields()));
                constructor.setResolvedReturnType(objectType);

                DeclarationType superClass = clazz.getSuperClass();
                if (superClass != null) {
                    superClass = superClass.resolve();
                    if (superClass instanceof ClassType) {
                        objectType.setBaseTypes(Collections.singletonList(new ClassInstanceType(superClass).accept(DeclarationTypeToTSTypes.this)));
                    } else {
                        throw new RuntimeException();
                    }
                }

                return null;
            } else {
                throw new RuntimeException(type.getClass().getSimpleName());
            }
        }

        private Map<String, Type> convertProperties(Map<String, DeclarationType> declarations) {
            Map<String, Type> properties = new HashMap<>();
            declarations.forEach((name, fieldType) -> {
                properties.put(name, fieldType.accept(DeclarationTypeToTSTypes.this));
            });
            return properties;
        }

        private void addFunctionToInterface(dk.au.cs.casa.typescript.types.InterfaceType t, FunctionType type) {
            t.setDeclaredCallSignatures(Collections.singletonList(toSignature(type)));
        }

        private Map<FunctionType, Signature> signatureCache = new HashMap<>();
        private Signature toSignature(FunctionType type) {
            Signature result = new Signature();
            result.setResolvedReturnType(type.getReturnType().accept(DeclarationTypeToTSTypes.this));
            result.setParameters(new ArrayList<>());
            for (FunctionType.Argument argument : type.getArguments()) {
                Signature.Parameter parameter = new Signature.Parameter();
                parameter.setName(argument.getName());
                parameter.setType(argument.getType().accept(DeclarationTypeToTSTypes.this));
                result.getParameters().add(parameter);
            }
            result.setMinArgumentCount(type.minArgs);
            return result;
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
            UnionDeclarationType union = (UnionDeclarationType) type;
            List<Type> mappedTypes = union.getTypes().stream().map(subType -> subType.accept(DeclarationTypeToTSTypes.this)).collect(Collectors.toList());
            t.setElements(mappedTypes);
            return null;
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

    private void register(Pair<Type, DeclarationType> e) {
        cache.put(e.second, e.first);
        finalizationQueue.add(e);
    }

    @Override
    public Type visit(FunctionType functionType) {
        if (cache.containsKey(functionType)) {
            return cache.get(functionType);
        }
        dk.au.cs.casa.typescript.types.InterfaceType result = new dk.au.cs.casa.typescript.types.InterfaceType();
        register(new Pair<>(result, functionType));
        return result;
    }

    @Override
    public Type visit(PrimitiveDeclarationType primitive) {
        switch (primitive.getType()) {
            case ANY: return new SimpleType(SimpleTypeKind.Any);
            case BOOLEAN: return new SimpleType(SimpleTypeKind.Boolean);
            case NON_VOID: return new SimpleType(SimpleTypeKind.Any);
            case NUMBER: return new SimpleType(SimpleTypeKind.Number);
            case STRING: return new SimpleType(SimpleTypeKind.String);
            case STRING_OR_NUMBER: return new UnionDeclarationType(PrimitiveDeclarationType.String(), PrimitiveDeclarationType.Number()).accept(this);
            case VOID: return new SimpleType(SimpleTypeKind.Void);
        }
        throw new RuntimeException();
    }

    @Override
    public Type visit(UnnamedObjectType objectType) {
        if (cache.containsKey(objectType)) {
            return cache.get(objectType);
        }
        dk.au.cs.casa.typescript.types.InterfaceType result = new dk.au.cs.casa.typescript.types.InterfaceType();
        register(new Pair<>(result, objectType));
        return result;
    }

    @Override
    public Type visit(InterfaceType interfaceType) {
        if (cache.containsKey(interfaceType)) {
            return cache.get(interfaceType);
        }
        dk.au.cs.casa.typescript.types.InterfaceType result = new dk.au.cs.casa.typescript.types.InterfaceType();
        register(new Pair<>(result, interfaceType));
        return result;
    }

    @Override
    public Type visit(UnionDeclarationType union) {
        if (cache.containsKey(union)) {
            return cache.get(union);
        }
        UnionType result = new UnionType();
        register(new Pair<>(result, union));
        return result;
    }

    @Override
    public Type visit(NamedObjectType named) {
        Type result = nativeClasses.typeFromName(named.getName());
        assert result != null;
        return result;
    }

    @Override
    public Type visit(ClassType classType) {
        if (cache.containsKey(classType)) {
            return cache.get(classType);
        }
        dk.au.cs.casa.typescript.types.InterfaceType result = new dk.au.cs.casa.typescript.types.InterfaceType();
        register(new Pair<>(result, classType));
        return result;
    }

    @Override
    public Type visit(ClassInstanceType instanceType) {
        dk.au.cs.casa.typescript.types.InterfaceType clazzType = (dk.au.cs.casa.typescript.types.InterfaceType)instanceType.getClazz().accept(this);
        finishQueue();
        return clazzType.getDeclaredConstructSignatures().iterator().next().getResolvedReturnType();
    }
}