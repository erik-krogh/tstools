package dk.webbies.tscreate.util;

import dk.au.cs.casa.typescript.types.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by erik1 on 13-06-2016.
 */
public class LookupType implements TypeVisitor<Type> {
    private String path;

    public LookupType(String path) {
        this.path = path;
    }

    public String firstPart() {
        return path.split("\\.")[0];
    }

    public String rest() {
        if (!path.contains(".")) {
            return "";
        }
        return path.substring(firstPart().length() + 1, path.length());
    }

    private Type next(Type Type) {
        if (rest().isEmpty()) {
            return Type;
        }
        return Type.accept(new LookupType(rest()));
    }

    @Override
    public Type visit(AnonymousType t) {
        throw new RuntimeException();
    }

    @Override
    public Type visit(ClassType t) {
        throw new RuntimeException();
    }

    @Override
    public Type visit(GenericType t) {
        return t.toInterface().accept(this);
    }

    @Override
    public Type visit(InterfaceType t) {
        String first = firstPart();
        if (t.getBaseTypes() != null) {
            List<Type> foundInBase = t.getBaseTypes().stream().map(base -> base.accept(this)).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            if (foundInBase.size() == 1) {
                return foundInBase.iterator().next();
            }
        }
        if (first.contains("[")) {
            List<Signature> signatures;
            if (first.equals("[constructor]")) {
                signatures = t.getDeclaredConstructSignatures();
            } else {
                signatures = t.getDeclaredCallSignatures();
            }
            if (signatures == null) {
                return null;
            } else {
                LookupType restVisitor = new LookupType(rest());
                List<Type> result = signatures.stream().map(restVisitor::visitSignature).filter(Objects::nonNull).distinct().collect(Collectors.toList());
                if (result.size() == 1) {
                    return result.iterator().next();
                }
            }
        } else {
            if (t.getDeclaredProperties() != null && t.getDeclaredProperties().get(first) != null) {
                return next(t.getDeclaredProperties().get(first));
            }
        }
        return null;
    }

    private Type visitSignature(Signature sig) {
        String first = firstPart();
        if (first.startsWith("[arg")) {
            int argNumber = Integer.parseInt(first.substring("[arg".length(), first.length() - 1));
            if (sig.getParameters() == null) {
                return null;
            }
            if (sig.getParameters().size() <= argNumber) {
                return null;
            } else {
                return next(sig.getParameters().get(argNumber).getType());
            }
        } else {
            if (sig.getResolvedReturnType() == null) {
                return null;
            }
            return next(sig.getResolvedReturnType());
        }
    }


    @Override
    public Type visit(ReferenceType t) {
        return t.getTarget().accept(this);
    }

    @Override
    public Type visit(SimpleType t) {
        return null;
    }

    @Override
    public Type visit(TupleType t) {
        throw new RuntimeException();
    }

    @Override
    public Type visit(UnionType t) {
        List<Type> result = t.getElements().stream().map(this::next).distinct().collect(Collectors.toList());
        if (result.size() == 1) {
            return result.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public Type visit(UnresolvedType t) {
        throw new RuntimeException();
    }

    @Override
    public Type visit(TypeParameterType t) {
        if (t.getTarget() == null) {
            return null;
        } else {
            return t.getTarget().accept(this);
        }
    }

    @Override
    public Type visit(SymbolType t) {
        throw new RuntimeException();
    }
}
