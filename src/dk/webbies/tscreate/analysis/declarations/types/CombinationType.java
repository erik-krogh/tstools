package dk.webbies.tscreate.analysis.declarations.types;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 16-10-2015.
 */
public class CombinationType extends DeclarationType {
    public final List<DeclarationType> types = new ArrayList<>();
    private final TypeReducer combiner;

    private boolean hasBeenUnfolded = false;

    public CombinationType(TypeReducer combiner, List<DeclarationType> types) {
        this.combiner = combiner;
        this.combiner.registerUnresolved(this);
        for (DeclarationType type : types) {
            addType(type);
        }
    }

    public CombinationType(TypeReducer combiner, DeclarationType... types) {
        this(combiner, Arrays.asList(types));
    }

    public void addType(DeclarationType type) {
        if (hasBeenUnfolded) {
            throw new RuntimeException("Cannot add a type after unfolding");
        }
        if (type != null) {
            this.types.add(type);
        }
    }

    private DeclarationType combined = null;
    public DeclarationType createCombined() {
        this.hasBeenUnfolded = true;

        if (this.types.size() == 0) {
            combined = PrimitiveDeclarationType.Void();
            return PrimitiveDeclarationType.Void();
        } else {
            Set<DeclarationType> unfolded = unfold(this);

            if (combiner.combinationTypeCache.containsKey(unfolded)) {
                DeclarationType result = combiner.combinationTypeCache.get(unfolded);
                this.combined = result;
                return result;
            }

            DeclarationType result;
            if (unfolded.isEmpty()) {
                result = PrimitiveDeclarationType.Void();
            } else {
                result = combiner.combineTypes(unfolded, false);
            }
            this.types.clear();
            this.types.add(result);

            combined = result;

            combiner.combinationTypeCache.put(unfolded, result);

            return result;
        }
    }

    private static Set<DeclarationType> unfold(DeclarationType type) {
        return type.getReachable().stream().filter((subType) -> {
            // We have the sub-types of these in the reachables, and we don't need the parents.
            if (subType instanceof UnresolvedDeclarationType) {
                return false;
            } else if (subType instanceof CombinationType) {
                return false;
            } else if (subType instanceof UnionDeclarationType) {
                return false;
            } else if (subType instanceof InterfaceType) {
                return false;
            } else {
                return true;
            }
        }).collect(Collectors.toSet());
    }

    public DeclarationType getCombined() {
        if (this.combined == null) {
            List<DeclarationType> reachable = this.getReachable().stream().filter(CombinationType.class::isInstance).collect(Collectors.toList());
            List<List<DeclarationType>> levels = DeclarationType.getLevels(reachable);
            for (List<DeclarationType> level : levels) {
                for (DeclarationType type : level) {
                    ((CombinationType) type).createCombined();
                }
            }
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
