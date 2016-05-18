package dk.webbies.tscreate.evaluation;

import dk.au.cs.casa.typescript.types.*;
import dk.webbies.tscreate.evaluation.DeclarationEvaluator.FunctionToEvaluate;
import dk.webbies.tscreate.util.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by erik1 on 12-05-2016.
 */
public class SignatureCollector implements TypeVisitorWithArgument<Void, SignatureCollector.Arg> {
    public static final class Arg {
        final Type type;
        final String path;

        public Arg(Type type, String path) {
            this.type = type;
            this.path = path;
        }
    }


    private Set<FunctionToEvaluate> functions = new HashSet<>();
    private Set<FunctionToEvaluate> constructors = new HashSet<>();

    private Set<Type> seen = new HashSet<>();

    public Set<FunctionToEvaluate> getFunctions() {
        return functions;
    }

    public Set<FunctionToEvaluate> getConstructors() {
        return constructors;
    }

    @Override
    public Void visit(AnonymousType t, Arg arg) {
        return null;
    }

    @Override
    public Void visit(ClassType t, Arg arg) {
        throw new RuntimeException();
    }

    @Override
    public Void visit(GenericType t, Arg arg) {
        if (seen.contains(t)) {
            return null;
        }
        seen.add(t);
        return t.toInterface().accept(this, arg);
    }

    @Override
    public Void visit(InterfaceType t, Arg arg) {
        if (seen.contains(t)) {
            return null;
        }
        seen.add(t);

        Type type = arg.type;

        if (type instanceof ReferenceType) {
            type = ((ReferenceType) type).getTarget();
        }
        if (type instanceof GenericType) {
            type = ((GenericType) type).toInterface();
        }

        if (type instanceof InterfaceType) {
            InterfaceType interfaceType = (InterfaceType) type;
            if (!t.getDeclaredCallSignatures().isEmpty() && !interfaceType.getDeclaredCallSignatures().isEmpty()) {
                functions.add(new FunctionToEvaluate(t.getDeclaredCallSignatures(), interfaceType.getDeclaredCallSignatures(), arg.path));
            }

            if (!t.getDeclaredConstructSignatures().isEmpty() && !interfaceType.getDeclaredConstructSignatures().isEmpty()) {
                constructors.add(new FunctionToEvaluate(t.getDeclaredConstructSignatures(), interfaceType.getDeclaredConstructSignatures(), arg.path));

                Type realInstance = t.getDeclaredConstructSignatures().iterator().next().getResolvedReturnType();
                Type myInstance = interfaceType.getDeclaredConstructSignatures().iterator().next().getResolvedReturnType();
                realInstance.accept(this, new Arg(myInstance, arg.path + ".[instance]"));
            }


            for (Map.Entry<String, Type> entry : t.getDeclaredProperties().entrySet()) {
                String key = entry.getKey();
                Type realType = entry.getValue();
                Type myType = interfaceType.getDeclaredProperties().get(key);
                if (myType != null) {
                    realType.accept(this, new Arg(myType, arg.path + "." + key));
                }
            }


            return null;
        } else if (type instanceof SimpleType) {
            return null;
        } else if (type instanceof UnionType) {
            for (Type subType : ((UnionType) type).getElements()) {
                seen.remove(t);
                t.accept(this, new Arg(subType, arg.path));
            }
            return null;
        }
        throw new NotImplementedException();
    }

    @Override
    public Void visit(ReferenceType t, Arg arg) {
        return t.getTarget().accept(this, arg);
    }

    @Override
    public Void visit(SimpleType t, Arg arg) {
        return null; // Do nothing.
    }

    @Override
    public Void visit(TupleType t, Arg arg) {
        throw new NotImplementedException();
    }

    @Override
    public Void visit(UnionType t, Arg arg) {
        t.getElements().forEach(elem -> {
            elem.accept(this, arg);
        });
        return null;
    }

    @Override
    public Void visit(UnresolvedType t, Arg arg) {
        throw new NotImplementedException();
    }

    @Override
    public Void visit(TypeParameterType t, Arg arg) {
        return t.getConstraint().accept(this, arg);
    }

    @Override
    public Void visit(SymbolType t, Arg arg) {
        throw new NotImplementedException();
    }
}
