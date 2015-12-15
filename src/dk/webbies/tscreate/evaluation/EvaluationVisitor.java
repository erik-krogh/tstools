package dk.webbies.tscreate.evaluation;

import dk.au.cs.casa.typescript.types.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Erik Krogh Kristensen on 14-12-2015.
 */
public class EvaluationVisitor implements TypeVisitorWithArgument<Void, Type> {
    private final int depth;
    private final Evaluation evaluation;
    private List<Runnable> queue;
    private Set<Type> nativeTypesInReal;

    public EvaluationVisitor(int depth, Evaluation evaluation, List<Runnable> queue, Set<Type> nativeTypesInReal) {
        this.depth = depth;
        this.evaluation = evaluation;
        this.queue = queue;
        this.nativeTypesInReal = nativeTypesInReal; // FIXME: Use this.
    }

    private EvaluationVisitor nextDepth() {
        return nextDepth(1);
    }

    private EvaluationVisitor nextDepth(int levels) {
        return new EvaluationVisitor(depth + levels, evaluation, queue, nativeTypesInReal);
    }

    @Override
    public Void visit(AnonymousType real, Type type) {
        throw new RuntimeException();
    }

    @Override
    public Void visit(ClassType real, Type type) {
        throw new RuntimeException();
    }

    @Override
    public Void visit(GenericType real, Type type) {
        return real.toInterface().accept(this, type);
    }

    @Override
    public Void visit(InterfaceType real, Type type) {
        if (type instanceof ReferenceType) {
            type = ((ReferenceType) type).getTarget();
        }
        if (type instanceof GenericType) {
            type = ((GenericType) type).toInterface();
        }

        if (!(type instanceof InterfaceType)) {
            throw new RuntimeException();
        }
        InterfaceType my = (InterfaceType) type;

        // Properties
        Map<String, Type> realProperties = real.getDeclaredProperties();
        Map<String, Type> myProperties = my.getDeclaredProperties();

        Set<String> properties = new HashSet<>();
        properties.addAll(realProperties.keySet());
        properties.addAll(myProperties.keySet());

        for (String property : properties) {
            if (!myProperties.containsKey(property)) {
                evaluation.addFalseNegative(depth + 1);
            } else if (!realProperties.containsKey(property)) {
                evaluation.addFalsePositive(depth + 1);
            } else {
                evaluation.addTruePositive(depth + 1);
                Type myType = myProperties.get(property);
                Type realType = realProperties.get(property);
                queue.add(() -> realType.accept(nextDepth(), myType));
            }
        }

        // Function
        // FIXME: Functions

        // Indexers:
        Type realNumber = real.getDeclaredNumberIndexType();
        Type myNumber = my.getDeclaredNumberIndexType();
        testIndexers(realNumber, myNumber);

        Type realString = real.getDeclaredNumberIndexType();
        Type myString = my.getDeclaredNumberIndexType();
        testIndexers(realString, myString);

        return null;
    }

    private void testIndexers(Type realString, Type myString) {
        if (realString != null && myString == null) {
            evaluation.addFalseNegative(depth + 1);
        } else if (myString != null && realString == null) {
            evaluation.addFalsePositive(depth + 1);
        } else if (myString != null /* && realString != null */) {
            queue.add(() -> realString.accept(nextDepth(), myString));
        }
    }

    @Override
    public Void visit(ReferenceType real, Type type) {
        return real.getTarget().accept(this, type); // Ignoring generic types for now.
    }

    @Override
    public Void visit(SimpleType real, Type type) {
        if (!real.equals(type)) {
            evaluation.addFalseNegative(depth);
        } else {
            evaluation.addTruePositive(depth);
        }
        return null;
    }

    @Override
    public Void visit(TupleType real, Type type) {
        throw new RuntimeException();
    }

    @Override
    public Void visit(UnionType real, Type type) {
        throw new RuntimeException();
    }

    @Override
    public Void visit(UnresolvedType real, Type type) {
        throw new RuntimeException();
    }

    @Override
    public Void visit(TypeParameterType real, Type type) {
        throw new RuntimeException();
    }

    @Override
    public Void visit(SymbolType real, Type type) {
        throw new RuntimeException();
    }
}
