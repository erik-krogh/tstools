package dk.webbies.tscreate.analysis.declarations.types;

import dk.webbies.tscreate.analysis.declarations.types.typeCombiner.TypeReducer;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 16-10-2015.
 */
public class CombinationType implements DeclarationType {
    private final List<DeclarationType> types = new ArrayList<>();
    private final TypeReducer combiner;

    private boolean hasBeenUnfolded = false;

    public CombinationType(TypeReducer combiner, DeclarationType... types) {
        this(combiner, Arrays.asList(types));
    }

    public CombinationType(TypeReducer combiner, Collection<DeclarationType> types) {
        this.combiner = combiner;
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

    public DeclarationType getCombined() {
        this.hasBeenUnfolded = true;

        if (this.types.size() == 0) {
            return PrimitiveDeclarationType.VOID;
        } else if (this.types.size() == 1 && !(this.types.get(0) instanceof CombinationType)) {
            return this.types.get(0);
        } else {
            Set<DeclarationType> unfolded = unfold(this, new HashSet<>());

            if (combiner.combinationTypeCache.containsKey(unfolded)) {
                return combiner.combinationTypeCache.get(unfolded);
            }

            DeclarationType result;
            if (unfolded.isEmpty()) {
                result = PrimitiveDeclarationType.VOID;
            } else if (unfolded.size() == 1) {
                result = unfolded.iterator().next();
            } else {
                result = combiner.combineTypes(unfolded);
            }
            this.types.clear();
            this.types.add(result);

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
            return unfold(((UnresolvedDeclarationType)type).getResolvedType(), seen);
        } else if (type instanceof UnionDeclarationType) {
            Set<DeclarationType> result = new HashSet<>();
            for (DeclarationType subType : ((UnionDeclarationType) type).getTypes()) {
                result.addAll(unfold(subType, seen));
            }

            return result;
        } else {
            return new HashSet<>(Arrays.asList(type));
        }
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
