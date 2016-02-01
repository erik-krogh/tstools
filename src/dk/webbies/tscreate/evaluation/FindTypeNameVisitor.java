package dk.webbies.tscreate.evaluation;

import dk.au.cs.casa.typescript.types.*;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by erik1 on 26-01-2016.
 */
public class FindTypeNameVisitor implements TypeVisitorWithArgument<Void, FindTypeNameVisitor.QueueElement> {
    private Map<Type, String> names = new HashMap<>();
    private Map<Signature, String> signatureNames = new HashMap<>();

    private <T> void addName(T element, String name, Map<T, String> map) {
        if (!hasBetter(element, name, map)) {
            map.put(element, name);
        }
    }

    private <T> boolean hasBetter(T element, String name, Map<T, String> map) {
        if (map.containsKey(element)) {
            return true;
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
    public Void visit(AnonymousType t, QueueElement elm) {
        throw new RuntimeException("AnonymousType");
    }

    @Override
    public Void visit(ClassType t, QueueElement elm) {
        throw new RuntimeException("ClassType");
    }

    @Override
    public Void visit(GenericType t, QueueElement elm) {
        if (hasBetter(t, elm.prefix)) {
            return null;
        }
        addName(t, elm.prefix);
        t.toInterface().accept(this, elm);
        return null;
    }

    @Override
    public Void visit(InterfaceType t, QueueElement elm) {
        String prefix = elm.prefix;
        if (hasBetter(t, prefix)) {
            return null;
        }

        addName(t, prefix);

        for (int i = 0; i < t.getTypeParameters().size(); i++) {
            Type type = t.getTypeParameters().get(i);
            enqueue(prefix + ".[typeParam" + i + "]", elm.depth + 1, type);
        }

        for (int i = 0; i < t.getBaseTypes().size(); i++) {
            Type type = t.getBaseTypes().get(i);
            enqueue(prefix + ".[extends" + i + "]", elm.depth + 1, type);
        }

        for (Map.Entry<String, Type> entry : t.getDeclaredProperties().entrySet()) {
            Type type = entry.getValue();
            enqueue(prefix + "." + entry.getKey(), elm.depth + 1, type);
        }

        if (t.getDeclaredNumberIndexType() != null) {
            enqueue(prefix + ".[numberIndexer]", elm.depth + 1, t.getDeclaredNumberIndexType());
        }

        if (t.getDeclaredStringIndexType() != null) {
            enqueue(prefix + ".[stringIndexer]", elm.depth + 1, t.getDeclaredStringIndexType());
        }

        for (int i = 0; i < t.getDeclaredCallSignatures().size(); i++) {
            Signature signature = t.getDeclaredCallSignatures().get(i);
            visitSignature(signature, prefix + ".[callSig" + i + "]", elm.depth);
        }

        for (int i = 0; i < t.getDeclaredConstructSignatures().size(); i++) {
            Signature signature = t.getDeclaredConstructSignatures().get(i);
            visitSignature(signature, prefix + ".[newSig" + i + "]", elm.depth);
        }

        return null;
    }

    private void enqueue(String prefix, int depth, Type type) {
        queue.add(new QueueElement(depth, prefix, type));
    }

    private void visitSignature(Signature signature, String prefix, int depth) {
        addName(signature, prefix);

        for (int i = 0; i < signature.getParameters().size(); i++) {
            Signature.Parameter parameter = signature.getParameters().get(i);
            enqueue(prefix + ".[arg:" + parameter.getName() + "]", depth + 1, parameter.getType());
        }
        enqueue(prefix + ".[return]", depth + 1, signature.getResolvedReturnType());
    }

    @Override
    public Void visit(ReferenceType t, QueueElement elm) {
        String prefix = elm.prefix;
        if (hasBetter(t, prefix)) {
            return null;
        }

        addName(t, prefix);
        t.getTarget().accept(this, elm);
        for (int i = 0; i < t.getTypeArguments().size(); i++) {
            Type type = t.getTypeArguments().get(i);
            enqueue(prefix + ".[typeParam" + i + "]", elm.depth + 1, type);
        }
        return null;
    }

    @Override
    public Void visit(SimpleType t, QueueElement elm) {
        addName(t, elm.prefix);
        return null;
    }

    @Override
    public Void visit(TupleType t, QueueElement elm) {
        String prefix = elm.prefix;
        addName(t, prefix);

        if (t.getBaseArrayType() != null) {
            enqueue(prefix + ".[baseArrayType]", elm.depth + 1, t.getBaseArrayType());
        }
        for (int i = 0; i < t.getElementTypes().size(); i++) {
            Type type = t.getElementTypes().get(i);
            enqueue(prefix + ".[tuple" + i + "]", elm.depth + 1, type);
        }
        return null;
    }

    @Override
    public Void visit(UnionType t, QueueElement elm) {
        String prefix = elm.prefix;
        addName(t, prefix);

        for (int i = 0; i < t.getElements().size(); i++) {
            Type type = t.getElements().get(i);
            enqueue(prefix + ".[union:" + i + "]", elm.depth + 1, type);
        }

        return null;
    }

    @Override
    public Void visit(UnresolvedType t, QueueElement elm) {
        throw new RuntimeException("unhandled type: " + t.getClass().getSimpleName());
    }

    @Override
    public Void visit(TypeParameterType t, QueueElement elm) {
        String prefix = elm.prefix;
        addName(t, prefix);

        if (t.getConstraint() != null) {
            enqueue(prefix + ".[typeParameterConstraint]", elm.depth + 1, t.getConstraint());
        }
        if (t.getTarget() != null) {
            enqueue(prefix + ".[typeParameterTarget]", elm.depth + 1, t.getTarget());
        }
        return null;
    }

    @Override
    public Void visit(SymbolType t, QueueElement elm) {
        addName(t, elm.prefix);
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

    private PriorityQueue<QueueElement> queue = new PriorityQueue<>();

    public void findNames(InterfaceType declaration) {
        queue.add(new QueueElement(0, "window", declaration));

        while (!queue.isEmpty()) {
            QueueElement element = queue.poll();
            element.type.accept(this, element);
        }
    }

    protected static final class QueueElement implements Comparable<QueueElement> {
        final int depth;
        final String prefix;
        final Type type;

        private QueueElement(int depth, String prefix, Type type) {
            this.depth = depth;
            this.prefix = prefix;
            this.type = type;
        }

        @Override
        public int compareTo(QueueElement o) {
            return Integer.compare(this.depth, o.depth);
        }
    }
}
