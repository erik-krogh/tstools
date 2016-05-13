package dk.webbies.tscreate.evaluation;

import dk.au.cs.casa.typescript.types.*;
import dk.webbies.tscreate.util.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by erik1 on 12-05-2016.
 */
public class SignatureCollector implements TypeVisitorWithArgument<Void, Type> {
    private Set<Pair<Set<Signature>, Set<Signature>>> functions = new HashSet<>();
    private Set<Pair<Set<Signature>, Set<Signature>>> constructors = new HashSet<>();

    private Set<Type> seen = new HashSet<>();

    public Set<Pair<Set<Signature>, Set<Signature>>> getFunctions() {
        return functions;
    }

    public Set<Pair<Set<Signature>, Set<Signature>>> getConstructors() {
        return constructors;
    }

    @Override
    public Void visit(AnonymousType t, Type type) {
        return null;
    }

    @Override
    public Void visit(ClassType t, Type type) {
        throw new RuntimeException();
    }

    @Override
    public Void visit(GenericType t, Type type) {
        if (seen.contains(t)) {
            return null;
        }
        seen.add(t);
        return t.toInterface().accept(this, type);
    }

    @Override
    public Void visit(InterfaceType t, Type type) {
        if (seen.contains(t)) {
            return null;
        }
        seen.add(t);

        if (type instanceof ReferenceType) {
            type = ((ReferenceType) type).getTarget();
        }
        if (type instanceof GenericType) {
            type = ((GenericType) type).toInterface();
        }

        if (type instanceof InterfaceType) {
            InterfaceType interfaceType = (InterfaceType) type;
            if (!t.getDeclaredCallSignatures().isEmpty() && !interfaceType.getDeclaredCallSignatures().isEmpty()) {
                functions.add(new Pair<>(new HashSet<>(t.getDeclaredCallSignatures()), new HashSet<>(interfaceType.getDeclaredCallSignatures())));
            }

            if (!t.getDeclaredConstructSignatures().isEmpty() && !interfaceType.getDeclaredConstructSignatures().isEmpty()) {
                constructors.add(new Pair<>(new HashSet<>(t.getDeclaredConstructSignatures()), new HashSet<>(interfaceType.getDeclaredConstructSignatures())));

                Type realInstance = t.getDeclaredConstructSignatures().iterator().next().getResolvedReturnType();
                Type myInstance = interfaceType.getDeclaredConstructSignatures().iterator().next().getResolvedReturnType();
                realInstance.accept(this, myInstance);
            }


            for (Map.Entry<String, Type> entry : t.getDeclaredProperties().entrySet()) {
                String key = entry.getKey();
                Type realType = entry.getValue();
                Type myType = interfaceType.getDeclaredProperties().get(key);
                if (myType != null) {
                    realType.accept(this, myType);
                }
            }


            return null;
        } else if (type instanceof SimpleType) {
            return null;
        } else if (type instanceof UnionType) {
            for (Type subType : ((UnionType) type).getElements()) {
                seen.remove(t);
                t.accept(this, subType);
            }
            return null;
        }
        throw new NotImplementedException();
    }

    @Override
    public Void visit(ReferenceType t, Type type) {
        return t.getTarget().accept(this, type);
    }

    @Override
    public Void visit(SimpleType t, Type type) {
        return null; // Do nothing.
    }

    @Override
    public Void visit(TupleType t, Type type) {
        throw new NotImplementedException();
    }

    @Override
    public Void visit(UnionType t, Type type) {
        t.getElements().forEach(elem -> {
            elem.accept(this, type);
        });
        return null;
    }

    @Override
    public Void visit(UnresolvedType t, Type type) {
        throw new NotImplementedException();
    }

    @Override
    public Void visit(TypeParameterType t, Type type) {
        return t.getConstraint().accept(this, type);
    }

    @Override
    public Void visit(SymbolType t, Type type) {
        throw new NotImplementedException();
    }
}
