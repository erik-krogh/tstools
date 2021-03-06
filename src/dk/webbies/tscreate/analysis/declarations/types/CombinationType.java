package dk.webbies.tscreate.analysis.declarations.types;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 16-10-2015.
 */
public class CombinationType extends DeclarationType {
    public final List<DeclarationType> types = new ArrayList<>();
    private final TypeReducer combiner;

    private boolean hasBeenUnfolded = false;

    public CombinationType(TypeReducer combiner, Collection<DeclarationType> types) {
        super(types.stream().filter(Objects::nonNull).map(DeclarationType::getNames).reduce(new HashSet<>(), Util::reduceSet));
        if (combiner == null) {
            throw new NullPointerException();
        }
        this.combiner = combiner;
        for (DeclarationType type : types) {
            if (type != null) {
                addType(type);
            }
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

    public void addTypes(Collection<DeclarationType> types) {
        types.forEach(this::addType);
    }

    private DeclarationType combined = null;
    public DeclarationType createCombined() {
        this.hasBeenUnfolded = true;

        if (this.combined != null) {
            return this.combined;
        }

        if (this.types.size() == 0) {
            combined = PrimitiveDeclarationType.Void(Collections.EMPTY_SET);
            return combined;
        } else {
            Set<DeclarationType> unfolded = unfold(this);

            if (combiner.combinationTypeCache.containsKey(unfolded)) {
                DeclarationType result = combiner.combinationTypeCache.get(unfolded);
                this.combined = result;
                return result;
            }

            DeclarationType result = combiner.combineTypes(unfolded, false);

            this.types.clear();
            this.types.add(result);

            this.combined = result;

            combiner.combinationTypeCache.put(unfolded, result);

            return result;
        }
    }

    @SuppressWarnings("RedundantIfStatement")
    private Set<DeclarationType> unfold(DeclarationType rootType) {
        final Set<DeclarationType> result = new HashSet<>();

        rootType.getReachable().stream().filter((subType) -> {
            // We have the sub-types of these in the reachables, and we don't need the parents.
            if (subType instanceof UnresolvedDeclarationType) {
                return false;
            } else if (subType instanceof CombinationType) {
                return false;
            } else if (subType instanceof UnionDeclarationType) {
                return false;
            } else if (subType instanceof InterfaceDeclarationType) {
                return false;
            } else {
                return true;
            }
        }).forEach(type -> this.addToUnfolded(type, result));

        return result;
    }

    private void addToUnfolded(DeclarationType type, Set<DeclarationType> acc) {
        if (combiner.originals.containsKey(type)) {
            combiner.originals.get(type).forEach(subType -> addToUnfolded(subType, acc));
        } else {
            acc.add(type);
        }
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
            assert this.combined != null;
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
