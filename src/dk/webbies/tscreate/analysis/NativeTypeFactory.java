package dk.webbies.tscreate.analysis;

import com.google.common.collect.Iterables;
import dk.au.cs.casa.typescript.types.*;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 18-09-2015.
 */
public class NativeTypeFactory {
    private final NativeClassesMap nativeClasses;
    private Map<Signature, FunctionNode> signatureCache = new HashMap<>(); // Used to get around recursive types.
    private PrimitiveNode.Factory primitiveFactory;
    private UnionFindSolver solver;

    public NativeTypeFactory(PrimitiveNode.Factory primitiveFactory, UnionFindSolver solver, NativeClassesMap nativeClasses) {
        this.primitiveFactory = primitiveFactory;
        this.solver = solver;
        this.nativeClasses = nativeClasses;
    }

    public FunctionNode fromSignature(Signature signature) {
        if (signatureCache.containsKey(signature)) {
            return signatureCache.get(signature);
        }
        List<String> argumentNames = signature.getParameters().stream().map(Signature.Parameter::getName).collect(Collectors.toList());
        FunctionNode functionNode = FunctionNode.create(argumentNames, solver);
        signatureCache.put(signature, functionNode);

        int normalParameterCount = signature.getParameters().size();
        if (signature.isHasRestParameter()) {
            normalParameterCount--;
            Type restType = Iterables.getLast(signature.getParameters()).getType();
            List<Type> typeArguments;
            if (restType instanceof ReferenceType) {
                ReferenceType ref = (ReferenceType) restType;
                typeArguments = ref.getTypeArguments();
            } else if (restType instanceof GenericType) {
                GenericType generic = (GenericType) restType;
                typeArguments = generic.getTypeArguments();
            } else {
                throw new RuntimeException();
            }

            if (typeArguments.size() != 1) {
                throw new RuntimeException();
            }
        }
        for (int i = 0; i < normalParameterCount; i++) {
            Type parameter = signature.getParameters().get(i).getType();
            UnionNode argument = functionNode.arguments.get(i);
            solver.union(argument, fromType(parameter, true));
        }
        solver.union(functionNode.returnNode, fromType(signature.getResolvedReturnType(), false));

        functionNode.arguments.forEach(arg -> solver.union(arg, primitiveFactory.nonVoid()));

        return functionNode;
    }

    private Map<Type, UnionNode> typeCache = new HashMap<>();
    private UnionNode fromType(Type type, boolean isArgument) {
        if (isArgument && type instanceof SimpleType && ((SimpleType) type).getKind() == SimpleTypeKind.Any) {
            return primitiveFactory.nonVoid();
        }
        if (typeCache.containsKey(type)) {
            return new IncludeNode(solver, typeCache.get(type));
        } else {
            EmptyNode node = new EmptyNode(solver);
            typeCache.put(type, node);
            List<UnionNode> result = type.accept(new UnionNativeTypeVisitor());
            solver.union(node, result);
            return new IncludeNode(solver, node);
        }
    }

    private Map<GenericType, List<UnionNode>> genericTypeCache = new HashMap<>();
    private Map<InterfaceType, List<UnionNode>> interfaceCache = new HashMap<>();

    private class UnionNativeTypeVisitor implements TypeVisitor<List<UnionNode>> {
        private Map<InterfaceType, GenericType> convertedTypeMap = new HashMap<>();

        private UnionNode recurse(Type t) {
            List<UnionNode> nodes = t.accept(this);
            if (nodes.isEmpty()) {
                return new EmptyNode(solver);
            } else {
                return new IncludeNode(solver, nodes);
            }
        }

        @Override
        public List<UnionNode> visit(AnonymousType t) {
            return Collections.EMPTY_LIST;
        }

        @Override
        public List<UnionNode> visit(ClassType t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<UnionNode> visit(GenericType t) {
            if (genericTypeCache.containsKey(t)) {
                return genericTypeCache.get(t);
            }
            ArrayList<UnionNode> result = new ArrayList<>();
            genericTypeCache.put(t, result);

            InterfaceType interfaceType = t.toInterface();
            convertedTypeMap.put(interfaceType, t);
            result.add(recurse(interfaceType));
            return result;
        }

        @Override
        public List<UnionNode> visit(InterfaceType t) {
            if (interfaceCache.containsKey(t)) {
                return interfaceCache.get(t);
            }
            ArrayList<UnionNode> result = new ArrayList<>();
            interfaceCache.put(t, result);

            ObjectNode obj = new ObjectNode(solver);
            if (convertedTypeMap.containsKey(t)) {
                GenericType type = convertedTypeMap.get(t);
                if (nativeClasses.nameFromType(type) != null) {
                    obj.setTypeName(nativeClasses.nameFromType(type));
                }
            } else {
                if (nativeClasses.nameFromType(t) != null) {
                    obj.setTypeName(nativeClasses.nameFromType(t));
                }
            }

            result.add(obj);

            for (Map.Entry<String, Type> entry : t.getDeclaredProperties().entrySet()) {
                String key = entry.getKey();
                Type type = entry.getValue();

                EmptyNode fieldNode = new EmptyNode(solver);
                obj.addField(key, fieldNode);
                solver.union(fieldNode, recurse(type));
            }

            if (!t.getDeclaredCallSignatures().isEmpty()) {
                List<FunctionNode> functionNodes = t.getDeclaredCallSignatures().stream().map(sig -> fromSignature(sig)).collect(Collectors.toList());
                result.add(new IncludeNode(solver, functionNodes));
            }

            if (!t.getDeclaredConstructSignatures().isEmpty()) {
                List<FunctionNode> functionNodes = t.getDeclaredConstructSignatures().stream().map(sig -> fromSignature(sig)).collect(Collectors.toList());
                result.add(new IncludeNode(solver, functionNodes));
            }

            if (t.getDeclaredStringIndexType() != null) {
                result.add(new DynamicAccessNode(solver.union(recurse(t.getDeclaredStringIndexType())), primitiveFactory.string(), solver));
            }
            if (t.getDeclaredNumberIndexType() != null) {
                result.add(new DynamicAccessNode(solver.union(recurse(t.getDeclaredNumberIndexType())), primitiveFactory.number(), solver));
            }

            return result;
        }

        @Override
        public List<UnionNode> visit(ReferenceType t) {
            return Arrays.asList(recurse(t.getTarget()));
        }

        @Override
        public List<UnionNode> visit(SimpleType t) {
            switch (t.getKind()) {
                case Any: return Arrays.asList(primitiveFactory.any());
                case Boolean: return Arrays.asList(primitiveFactory.bool());
                case Enum: throw new UnsupportedOperationException();
                case Number: return Arrays.asList(primitiveFactory.number());
                case String: return Arrays.asList(primitiveFactory.string());
                case Undefined: return Arrays.asList(primitiveFactory.undefined());
                case Void: return Collections.EMPTY_LIST;
                default:
                    throw new UnsupportedOperationException("Unhandled type: " + t.getKind());
            }
        }

        @Override
        public List<UnionNode> visit(TupleType t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<UnionNode> visit(UnionType t) {
            return t.getElements().stream().map(this::recurse).collect(Collectors.toList());
        }

        @Override
        public List<UnionNode> visit(UnresolvedType t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<UnionNode> visit(TypeParameterType t) {
            // Generics, doesn't do this yet.
            return Collections.EMPTY_LIST;
        }

        @Override
        public List<UnionNode> visit(SymbolType t) {
            throw new UnsupportedOperationException();
        }
    }
}
