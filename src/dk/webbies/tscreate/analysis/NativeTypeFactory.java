package dk.webbies.tscreate.analysis;

import com.google.common.collect.Iterables;
import dk.au.cs.casa.typescript.types.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;
import dk.webbies.tscreate.util.Util;
import dk.webbies.tscreate.analysis.unionFind.*;
import dk.webbies.tscreate.jsnap.Snap;

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

    public FunctionNode fromSignature(Signature signature, Snap.Obj closure, List<UnionNode> args) {
        if (args == null) {
            args = Collections.EMPTY_LIST;
        }
        if (signatureCache.containsKey(signature)) {
            return signatureCache.get(signature);
        }
        List<String> argumentNames = signature.getParameters().stream().map(Signature.Parameter::getName).collect(Collectors.toList());
        for (int i = argumentNames.size(); i < args.size(); i++) {
            argumentNames.add("arg" + i);
        }
        FunctionNode functionNode = FunctionNode.create(closure, argumentNames, solver);
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

            Type typeArgument = Iterables.getLast(typeArguments);
            for (int i = normalParameterCount; i < args.size(); i++) {
                solver.union(args.get(i), fromType(typeArgument, true));
                solver.union(functionNode.arguments.get(i), args.get(i));
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

    private List<UnionNode> fromType(Type parameter, boolean isArgument) {
        return parameter.accept(new UnionNativeTypeVisitor(isArgument));
    }

    public List<UnionNode> fromType(Type parameter) {
        return fromType(parameter, false);
    }

    private Map<GenericType, List<UnionNode>> genericTypeCache = new HashMap<>();
    private Map<InterfaceType, List<UnionNode>> interfaceCache = new HashMap<>();

    private class UnionNativeTypeVisitor implements TypeVisitor<List<UnionNode>> {
        private final boolean isArgument;
        private Map<InterfaceType, GenericType> convertedTypeMap = new HashMap<>();

        private UnionNativeTypeVisitor(boolean isArgument) {
            this.isArgument = isArgument;
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
            result.addAll(interfaceType.accept(this));
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

            List<Map.Entry<UnionNode, List<UnionNode>>> delayedUnions = new ArrayList<>();
            for (Map.Entry<String, Type> entry : t.getDeclaredProperties().entrySet()) {
                String key = entry.getKey();
                Type type = entry.getValue();

                EmptyNode fieldNode = new EmptyNode(solver);
                obj.addField(key, fieldNode);
                delayedUnions.add(new AbstractMap.SimpleEntry<>(fieldNode, type.accept(this)));
            }

            if (!t.getDeclaredCallSignatures().isEmpty()) {
                List<FunctionNode> functionNodes = t.getDeclaredCallSignatures().stream().map(sig -> fromSignature(sig, null, null)).collect(Collectors.toList());
                result.addAll(functionNodes);
            }

            if (!t.getDeclaredConstructSignatures().isEmpty()) {
                List<FunctionNode> functionNodes = t.getDeclaredConstructSignatures().stream().map(sig -> fromSignature(sig, null, null)).collect(Collectors.toList());
                result.addAll(functionNodes);
            }

            if (t.getDeclaredStringIndexType() != null) {
                result.add(new DynamicAccessNode(solver.union(t.getDeclaredStringIndexType().accept(this)), primitiveFactory.string(), solver));
            }
            if (t.getDeclaredNumberIndexType() != null) {
                result.add(new DynamicAccessNode(solver.union(t.getDeclaredNumberIndexType().accept(this)), primitiveFactory.number(), solver));
            }

            for (Map.Entry<UnionNode, List<UnionNode>> entry : delayedUnions) {
                solver.union(entry.getKey(), entry.getValue());
            }

            return result;
        }

        @Override
        public List<UnionNode> visit(ReferenceType t) {
            return t.getTarget().accept(this);
        }

        @Override
        public List<UnionNode> visit(SimpleType t) {
            switch (t.getKind()) {
                case Any:
                    if (this.isArgument) {
                        return Arrays.asList(primitiveFactory.nonVoid());
                    } else {
                        return Arrays.asList(primitiveFactory.any());
                    }
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
            return t.getElements().stream().map(type -> type.accept(this)).reduce(new ArrayList<>(), Util::reduceList);
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
