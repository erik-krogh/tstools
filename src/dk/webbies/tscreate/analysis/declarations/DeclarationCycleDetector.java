package dk.webbies.tscreate.analysis.declarations;

import dk.webbies.tscreate.analysis.declarations.types.*;
import fj.F;
import fj.pre.Ord;
import fj.pre.Ordering;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Erik Krogh Kristensen on 17-11-2015.
 */
public class DeclarationCycleDetector implements DeclarationTypeVisitorWithArgument<Void, fj.data.Set<DeclarationType>>{
    private Set<DeclarationType> cyclicTypes = new HashSet<>();


    private static <T> boolean contains(T element, fj.data.Set<T> set) {
        return set.member(element);
    }

    private static final Ord<DeclarationType> ordering = Ord.ord(new F<DeclarationType, F<DeclarationType, Ordering>>() {
        public F<DeclarationType, Ordering> f(DeclarationType one) {
            return two -> {
                int x = Integer.compare(one.counter, two.counter);
                return x < 0 ? Ordering.LT : (x == 0 ? Ordering.EQ : Ordering.GT);
            };
        }
    });

    public void addType(DeclarationType type) {
        type.accept(this, fj.data.Set.empty(ordering));
    }

    public Set<DeclarationType> getCyclicTypes() {
        return cyclicTypes;
    }

    @Override
    public Void visit(FunctionType type, fj.data.Set<DeclarationType> seen) {
        if (contains(type, seen)) {
            cyclicTypes.add(type);
        } else {
            fj.data.Set<DeclarationType> cons = seen.insert(type);
            type.getArguments().forEach(arg -> arg.getType().accept(this, cons));
            type.getReturnType().accept(this, cons);
        }
        return null;
    }

    @Override
    public Void visit(PrimitiveDeclarationType type, fj.data.Set<DeclarationType> seen) {
        // Nothing, cannot be recursive
        return null;
    }

    @Override
    public Void visit(UnnamedObjectType type, fj.data.Set<DeclarationType> seen) {
        if (contains(type, seen)) {
            cyclicTypes.add(type);
        } else {
            fj.data.Set<DeclarationType> cons = seen.insert(type);
            type.getDeclarations().forEach((name, decType) -> {
                decType.accept(this, cons);
            });
        }
        return null;
    }

    @Override
    public Void visit(InterfaceType type, fj.data.Set<DeclarationType> seen) {
        // Interfaces are already being printed top-level, so i just recurse.
        fj.data.Set<DeclarationType> cons = seen.insert(type);
        if (contains(type, seen)) {
            cyclicTypes.add(type);
        } else {
            if (type.function != null) {
                type.getFunction().accept(this, cons);
            }
            if (type.object != null) {
                type.getObject().accept(this, cons);
            }
            if (type.dynamicAccess != null) {
                type.getDynamicAccess().getLookupType().accept(this, cons);
                type.getDynamicAccess().getReturnType().accept(this, cons);
            }
        }
        return null;
    }

    @Override
    public Void visit(UnionDeclarationType type, fj.data.Set<DeclarationType> seen) {
        if (contains(type, seen)) {
            cyclicTypes.add(type);
        } else {
            fj.data.Set<DeclarationType> cons = seen.insert(type);
            type.getTypes().forEach(subType -> subType.accept(this, cons));
        }
        return null;
    }

    @Override
    public Void visit(NamedObjectType type, fj.data.Set<DeclarationType> seen) {
        // Cannot be recursive.
        return null;
    }

    @Override
    public Void visit(ClassType type, fj.data.Set<DeclarationType> seen) {
        if (contains(type, seen)) {
            cyclicTypes.add(type);
        } else {
            fj.data.Set<DeclarationType> cons = seen.insert(type);
            // It would always be recursive if i looked at the return-type of the constructor, so i dont.
            type.getConstructorType().getArguments().forEach(arg -> arg.getType().accept(this, cons));
            type.getPrototypeFields().entrySet().forEach(entry -> entry.getValue().accept(this, cons));
            type.getStaticFields().entrySet().forEach(entry -> entry.getValue().accept(this, cons));
        }
        return null;
    }

    @Override
    public Void visit(ClassInstanceType type, fj.data.Set<DeclarationType> seen) {
        // Cannot be recursive...
        return null;
    }
}
