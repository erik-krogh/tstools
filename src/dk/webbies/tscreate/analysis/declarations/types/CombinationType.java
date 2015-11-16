package dk.webbies.tscreate.analysis.declarations.types;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 16-10-2015.
 */
public class CombinationType implements DeclarationType {
    public final List<DeclarationType> types = new ArrayList<>();
    private final TypeReducer combiner;

    private boolean hasBeenUnfolded = false;

    public CombinationType(TypeReducer combiner, DeclarationType... types) {
        this(combiner, Arrays.asList(types));
    }

    public CombinationType(TypeReducer combiner, Collection<DeclarationType> types) {
        this.combiner = combiner;
        this.combiner.registerUnresolved(this);
        addType(types);
    }

    public void addType(DeclarationType type) {
        if (hasBeenUnfolded) {
            throw new RuntimeException("Cannot add a type after unfolding");
        }
        if (type != null) {
            this.types.add(type);
        }
    }

    public void addType(Collection<DeclarationType> types) {
        for (DeclarationType type : types) {
            if (type != null) {
                this.addType(type);
            }
        }
    }

    private DeclarationType combined = null;
    public DeclarationType createCombined() {
        this.hasBeenUnfolded = true;

        if (this.types.size() == 0) {
            combined = PrimitiveDeclarationType.Void();
            return PrimitiveDeclarationType.Void();
        } else {
            Set<DeclarationType> unfolded = unfold(this, new HashSet<>());

            if (combiner.combinationTypeCache.containsKey(unfolded)) {
                DeclarationType result = combiner.combinationTypeCache.get(unfolded);
                this.combined = result;
                return result;
            }

            DeclarationType result;
            if (unfolded.isEmpty()) {
                result = PrimitiveDeclarationType.Void();
            } else {
                result = combiner.combineTypes(unfolded);
            }
            this.types.clear();
            this.types.add(result);

            combined = result;

            combiner.combinationTypeCache.put(unfolded, result);

            return result;
        }
    }

    private static Set<DeclarationType> unfold(DeclarationType type, Set<CombinationType> seen) {
        if (type instanceof CombinationType) {
            CombinationType combination = (CombinationType) type;
            if (seen.contains(combination)) {
                return Collections.EMPTY_SET;
            }
            seen.add(combination);

            combination.hasBeenUnfolded = true;

            Set<DeclarationType> result = new HashSet<>();
            for (DeclarationType subType : combination.types) {
                result.addAll(unfold(subType, seen));
            }

            return result;
        } else if (type instanceof UnresolvedDeclarationType) {
            return unfold(((UnresolvedDeclarationType) type).getResolvedType(), seen);
        } else if (type instanceof UnionDeclarationType) {
            Set<DeclarationType> result = new HashSet<>();
            for (DeclarationType subType : ((UnionDeclarationType) type).getTypes()) {
                result.addAll(unfold(subType, seen));
            }

            return result;
        } else if (type instanceof InterfaceType) {
            HashSet<DeclarationType> result = new HashSet<>();
            InterfaceType interfaceType = (InterfaceType) type;
            if (interfaceType.function != null) {
                result.addAll(unfold(interfaceType.function, seen));
            }
            if (interfaceType.object != null) {
                result.addAll(unfold(interfaceType.object, seen));
            }
            if (interfaceType.getDynamicAccess() != null) {
                result.addAll(unfold(interfaceType.getDynamicAccess(), seen));
            }

            return result;
        } else {
            return new HashSet<>(Arrays.asList(type));
        }
    }

    public DeclarationType getCombined() {
        if (this.combined == null) {
            throw new NullPointerException();
        }
        return this.combined;
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        return getCombined().accept(visitor);
    }

    @Override
    public <T, A> T accept(DeclarationTypeVisitorWithArgument<T, A> visitor, A argument) {
        return getCombined().accept(visitor, argument);
    }
}
