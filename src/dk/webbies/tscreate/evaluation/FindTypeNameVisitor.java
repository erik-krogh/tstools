package dk.webbies.tscreate.evaluation;

import dk.au.cs.casa.typescript.types.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by erik1 on 26-01-2016.
 */
public class FindTypeNameVisitor implements TypeVisitorWithArgument<Void, String> {
    private Map<Type, String> names = new HashMap<>();
    private Map<Signature, String> signatureNames = new HashMap<>();

    private <T> void addName(T element, String name, Map<T, String> map) {
        if (!hasBetter(element, name, map)) {
            map.put(element, name);
        }
    }

    private <T> boolean hasBetter(T element, String name, Map<T, String> map) {
        if (map.containsKey(element)) {
            String oldName = map.get(element);
            if (dots(name) > dots(oldName)) {
                return true;
            }
        }
        return false;
    }

    private void addName(Type type, String name) {
        addName(type, name, names);
    }

    private boolean hasBetter(Type type, String name) {
        return hasBetter(type, name, names);
    }

    private void addName(Signature sig, String name) {
        addName(sig, name, signatureNames);
    }

    private boolean hasBetter(Signature sig, String name) {
        return hasBetter(sig, name, signatureNames);
    }

    private static final Pattern dotPattern = Pattern.compile("\\.");
    private int dots(String string) {
        Matcher matcher = dotPattern.matcher(string);

        int count = 0;
        while (matcher.find())
            count++;

        return count;
    }

    @Override
    public Void visit(AnonymousType t, String prefix) {
        throw new RuntimeException("AnonymousType");
    }

    @Override
    public Void visit(ClassType t, String prefix) {
        throw new RuntimeException("ClassType");
    }

    @Override
    public Void visit(GenericType t, String prefix) {
        if (hasBetter(t, prefix)) {
            return null;
        }
        addName(t, prefix);
        t.toInterface().accept(this, prefix);
        return null;
    }

    @Override
    public Void visit(InterfaceType t, String prefix) {
        if (hasBetter(t, prefix)) {
            return null;
        }

        addName(t, prefix);

        for (int i = 0; i < t.getTypeParameters().size(); i++) {
            Type type = t.getTypeParameters().get(i);
            type.accept(this, prefix + ".[typeParam" + i + "]");
        }

        for (int i = 0; i < t.getBaseTypes().size(); i++) {
            Type type = t.getBaseTypes().get(i);
            type.accept(this, prefix + ".[extends" + i + "]");
        }

        for (Map.Entry<String, Type> entry : t.getDeclaredProperties().entrySet()) {
            entry.getValue().accept(this, prefix + "." + entry.getKey());
        }

        if (t.getDeclaredNumberIndexType() != null) {
            t.getDeclaredNumberIndexType().accept(this, prefix + ".[numberIndexer]");
        }

        if (t.getDeclaredStringIndexType() != null) {
            t.getDeclaredStringIndexType().accept(this, prefix + ".[stringIndexer]");
        }

        for (int i = 0; i < t.getDeclaredCallSignatures().size(); i++) {
            Signature signature = t.getDeclaredCallSignatures().get(i);
            visitSignature(signature, prefix + ".[callSig" + i + "]");
        }

        for (int i = 0; i < t.getDeclaredConstructSignatures().size(); i++) {
            Signature signature = t.getDeclaredConstructSignatures().get(i);
            visitSignature(signature, prefix + ".[newSig" + i + "]");
        }

        return null;
    }

    private void visitSignature(Signature signature, String prefix) {
        addName(signature, prefix);

        for (int i = 0; i < signature.getParameters().size(); i++) {
            Signature.Parameter parameter = signature.getParameters().get(i);
            parameter.getType().accept(this, prefix + ".[arg:" + parameter.getName() + "]");
        }
        signature.getResolvedReturnType().accept(this, prefix + ".[return]");
    }

    @Override
    public Void visit(ReferenceType t, String prefix) {
        if (hasBetter(t, prefix)) {
            return null;
        }

        addName(t, prefix);
        t.getTarget().accept(this, prefix);
        for (int i = 0; i < t.getTypeArguments().size(); i++) {
            Type type = t.getTypeArguments().get(i);
            type.accept(this, prefix + ".[typeParam" + i + "]");
        }
        return null;
    }

    @Override
    public Void visit(SimpleType t, String prefix) {
        addName(t, prefix);
        return null;
    }

    @Override
    public Void visit(TupleType t, String prefix) {
        addName(t, prefix);

        if (t.getBaseArrayType() != null) {
            t.getBaseArrayType().accept(this, prefix + ".[baseArrayType]");
        }
        for (int i = 0; i < t.getElementTypes().size(); i++) {
            Type type = t.getElementTypes().get(i);
            type.accept(this, prefix + ".[tuple" + i + "]");
        }
        return null;
    }

    @Override
    public Void visit(UnionType t, String prefix) {
        addName(t, prefix);

        for (int i = 0; i < t.getElements().size(); i++) {
            Type type = t.getElements().get(i);
            type.accept(this, prefix + ".[union:" + i + "]");
        }

        return null;
    }

    @Override
    public Void visit(UnresolvedType t, String prefix) {
        throw new RuntimeException("unhandled type: " + t.getClass().getSimpleName());
    }

    @Override
    public Void visit(TypeParameterType t, String prefix) {
        addName(t, prefix);

        if (t.getConstraint() != null) {
            t.getConstraint().accept(this, prefix + ".[typeParameterConstraint]");
        }
        if (t.getTarget() != null) {
            t.getTarget().accept(this, prefix + ".[typeParameterTarget]");
        }
        return null;
    }

    @Override
    public Void visit(SymbolType t, String prefix) {
        addName(t, prefix);
        return null;
    }

    public String getTypeName(Type type) {
//        assert type == null || names.containsKey(type);
        if (type == null || !names.containsKey(type)) { // TODO: Why?
            return "null";
        }
        return names.get(type);
    }

    public void addTypeName(Type type, String name) {
        addName(type, name);
    }
}
