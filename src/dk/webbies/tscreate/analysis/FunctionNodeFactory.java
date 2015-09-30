package dk.webbies.tscreate.analysis;

import com.google.common.collect.Iterables;
import dk.au.cs.casa.typescript.types.*;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.analysis.unionFind.nodes.*;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 18-09-2015.
 */
public class FunctionNodeFactory {
    private Map<Signature, FunctionNode> signatureCache = new HashMap<>(); // Used to get around recursive types.
    private PrimitiveUnionNode.Factory primitiveFactory;
    private UnionFindSolver solver;
    private Map<Type, String> typeNames;

    public FunctionNodeFactory(PrimitiveUnionNode.Factory primitiveFactory, UnionFindSolver solver, Map<Type, String> typeNames) {
        this.primitiveFactory = primitiveFactory;
        this.solver = solver;
        this.typeNames = typeNames;
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
        FunctionNode functionNode = new FunctionNode(closure, argumentNames);
        signatureCache.put(signature, functionNode);

        int normalParameterCount = signature.getParameters().size();
        if (signature.isHasRestParameter()) {
            normalParameterCount--;
            Type restType = Iterables.getLast(signature.getParameters()).getType();
            List<Type> typeArguments;
            if (restType instanceof ReferenceType) {
                ReferenceType ref = (ReferenceType) restType;
                typeArguments = ref.getTypeArguments();
            } else if (restType instanceof GenericType) { // TODO: problem with DeclarationParser; RegExpExecArray.splice(start: number, deleteCount: number, ...items: string[]): string[]; Items is just an Array[AnonymousType].
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
                solver.union(args.get(i), typeArgument.accept(new UnionNativeTypeVisitor()));
                solver.union(functionNode.arguments.get(i), args.get(i));
            }
        }
        for (int i = 0; i < normalParameterCount; i++) {
            Type parameter = signature.getParameters().get(i).getType();
            UnionNode argument = functionNode.arguments.get(i);
            solver.union(argument, parameter.accept(new UnionNativeTypeVisitor()));
        }
        solver.union(functionNode.returnNode, signature.getResolvedReturnType().accept(new UnionNativeTypeVisitor()));

        functionNode.arguments.forEach(arg -> solver.union(arg, new NonVoidNode()));

        return functionNode;
    }

    private class UnionNativeTypeVisitor implements TypeVisitor<List<UnionNode>> {

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
            InterfaceType interfaceType = t.toInterface();
            if (typeNames.containsKey(t)) {
                typeNames.put(interfaceType, typeNames.get(t));
            }
            return interfaceType.accept(this);
        }

        Map<InterfaceType, List<UnionNode>> cache = new HashMap<>();
        @Override
        public List<UnionNode> visit(InterfaceType t) {
            if (cache.containsKey(t)) {
                return cache.get(t);
            }
            ArrayList<UnionNode> result = new ArrayList<>();
            cache.put(t, result);

            ObjectUnionNode obj = new ObjectUnionNode();
            if (typeNames.containsKey(t)) {
                obj.setTypeName(typeNames.get(t));
            }
            result.add(obj);

            List<Map.Entry<UnionNode, List<UnionNode>>> delayedUnions = new ArrayList<>();
            for (Map.Entry<String, Type> entry : t.getDeclaredProperties().entrySet()) {
                String key = entry.getKey();
                Type type = entry.getValue();

                EmptyUnionNode fieldNode = new EmptyUnionNode();
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
            return t.getElements().stream().map(type -> type.accept(this)).reduce(new ArrayList<>(), (acc, elem) -> {acc.addAll(elem); return acc;});
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
