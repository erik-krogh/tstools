package dk.webbies.tscreate.cleanup;

import dk.au.cs.casa.typescript.types.*;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.analysis.declarations.types.ClassType;
import dk.webbies.tscreate.analysis.declarations.types.InterfaceDeclarationType;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.*;

/**
 * Created by erik1 on 11-03-2016.
 */
public class DeclarationTypeToTSTypes implements DeclarationTypeVisitor<Type> {
    private final DeclarationParser.NativeClassesMap nativeClasses;
    private final Map<DeclarationType, Type> cache = new HashMap<>();
    private final Map<ClassType, Type> classInstanceCache = new HashMap<>();
    private final List<Pair<Type, DeclarationType>> finalizationQueue = new ArrayList<>();

    public DeclarationTypeToTSTypes(DeclarationParser.NativeClassesMap nativeClasses) {
        this.nativeClasses = nativeClasses;
    }

    public Type getType(DeclarationType declaration) {
        if (declaration instanceof ClassInstanceType && classInstanceCache.containsKey(((ClassInstanceType) declaration).getClazz())) {
            return classInstanceCache.get(((ClassInstanceType) declaration).getClazz());
        } else if (this.cache.containsKey(declaration)) {
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
            } else if (type instanceof InterfaceDeclarationType) {
                InterfaceDeclarationType inter = (InterfaceDeclarationType) type;
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
                clazz.getConstructorType().setReturnType(new ClassInstanceType(clazz, EMPTY_SET));
                Signature constructor = toSignature(clazz.getConstructorType());
                t.setDeclaredConstructSignatures(singletonList(constructor));

                t.setDeclaredProperties(convertProperties(clazz.getPrototypeFields()));

                return null;
            } else if (type instanceof ClassInstanceType) {
                ClassInstanceType instance = (ClassInstanceType) type;
                ClassType clazz = instance.getClazz();
                dk.au.cs.casa.typescript.types.InterfaceType objectType = new dk.au.cs.casa.typescript.types.InterfaceType();
                objectType.setDeclaredProperties(convertProperties(clazz.getPrototypeFields()));

                DeclarationType superClass = clazz.getSuperClass();
                if (superClass != null) {
                    superClass = superClass.resolve();
                    if (superClass instanceof ClassType) {
                        objectType.setBaseTypes(singletonList(new ClassInstanceType(superClass, EMPTY_SET).accept(DeclarationTypeToTSTypes.this)));
                    } else if (superClass instanceof NamedObjectType) {
                        Type superType = nativeClasses.typeFromName(((NamedObjectType) superClass).getName());
                        objectType.setBaseTypes(singletonList(superType));
                    } else {
                        throw new RuntimeException(); //FIXME: This happens in underscore with mixed. And handleBars unify_cs.
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
            t.setDeclaredCallSignatures(singletonList(toSignature(type)));
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
            List<Type> mappedTypes = union.getTypes().stream().map(subType -> subType.accept(DeclarationTypeToTSTypes.this)).filter(Objects::nonNull).collect(Collectors.toList());
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
        if (e.second instanceof ClassInstanceType) {
            classInstanceCache.put(((ClassInstanceType) e.second).getClazz(), e.first);
        }
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
            case STRING_OR_NUMBER: return new UnionDeclarationType(PrimitiveDeclarationType.String(EMPTY_SET), PrimitiveDeclarationType.Number(EMPTY_SET)).accept(this);
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
    public Type visit(InterfaceDeclarationType interfaceType) {
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
        if (classInstanceCache.containsKey(instanceType.getClazz())) {
            return classInstanceCache.get(instanceType.getClazz());
        }
        InterfaceType result = new InterfaceType();
        register(new Pair<>(result, instanceType));
        return result;
    }
}
