package dk.webbies.tscreate.analysis.declarations.types;

import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.util.Pair;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 16-10-2015.
 */
public class CombinationType implements DeclarationType {
    public final List<DeclarationType> types = new ArrayList<>();
    private final TypeReducer combiner;

    private boolean hasBeenUnfolded = false;

    public CombinationType(TypeReducer combiner, DeclarationType... types) {
        this.combiner = combiner;
        this.combiner.registerUnresolved(this);
        for (DeclarationType type : types) {
            addType(type);
        }
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
            combined = PrimitiveDeclarationType.VOID;
            return PrimitiveDeclarationType.VOID;
        } else {
            Set<DeclarationType> unfolded = unfold(this, new HashSet<>());

            if (combiner.combinationTypeCache.containsKey(unfolded)) {
                DeclarationType result = combiner.combinationTypeCache.get(unfolded);
                this.combined = result;
                return result;
            }

            DeclarationType result;
            if (unfolded.isEmpty()) {
                result = PrimitiveDeclarationType.VOID;
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

    public void partiallyResolve() {
        if (this.types.size() <= 1) {
            return;
        }
        Pair<Set<DeclarationType>, Set<UnresolvedDeclarationType>> unfolded = unfoldPartially(this, new HashSet<>());
        Set<DeclarationType> types = unfolded.left;
        Set<UnresolvedDeclarationType> unresolved = unfolded.right;
        DeclarationType result;
        if (types.isEmpty()) {
            result = PrimitiveDeclarationType.VOID;
        } else if (combiner.combinationTypeCache.containsKey(types)) {
            result = combiner.combinationTypeCache.get(types);
        } else {
            result = combiner.combineTypes(types, true);
            combiner.combinationTypeCache.put(types, result);
        }
        this.types.clear();
        this.types.add(result);
        this.types.addAll(unresolved);
    }

    private static Pair<Set<DeclarationType>, Set<UnresolvedDeclarationType>> unfoldPartially(DeclarationType type, Set<CombinationType> seen) {
        if (type instanceof CombinationType) {
            CombinationType combination = (CombinationType) type;
            if (seen.contains(combination)) {
                return new Pair<>(Collections.EMPTY_SET, Collections.EMPTY_SET);
            }
            seen.add(combination);

            combination.hasBeenUnfolded = true;

            Set<DeclarationType> types = new HashSet<>();
            Set<UnresolvedDeclarationType> unresolved = new HashSet<>();
            for (DeclarationType subType : combination.types) {
                Pair<Set<DeclarationType>, Set<UnresolvedDeclarationType>> subResult = unfoldPartially(subType, seen);
                types.addAll(subResult.left);
                unresolved.addAll(subResult.right);
            }

            return new Pair<>(types, unresolved);
        } else if (type instanceof UnresolvedDeclarationType) {
            UnresolvedDeclarationType unresolved = (UnresolvedDeclarationType) type;
            if (unresolved.isResolved()) {
                return unfoldPartially(unresolved.getResolvedType(), seen);
            } else {
                return new Pair<>(Collections.EMPTY_SET, new HashSet<>(Arrays.asList(unresolved)));
            }
        } else if (type instanceof UnionDeclarationType) {
            Set<DeclarationType> types = new HashSet<>();
            Set<UnresolvedDeclarationType> unresolved = new HashSet<>();
            for (DeclarationType subType : ((UnionDeclarationType) type).getTypes()) {
                Pair<Set<DeclarationType>, Set<UnresolvedDeclarationType>> subResult = unfoldPartially(subType, seen);
                types.addAll(subResult.left);
                unresolved.addAll(subResult.right);
            }

            return new Pair<>(types, unresolved);
        } else if (type instanceof InterfaceType) {
            Set<DeclarationType> types = new HashSet<>();
            Set<UnresolvedDeclarationType> unresolved = new HashSet<>();
            InterfaceType interfaceType = (InterfaceType) type;
            if (interfaceType.function != null) {
                Pair<Set<DeclarationType>, Set<UnresolvedDeclarationType>> subResult = unfoldPartially(interfaceType.function, seen);
                types.addAll(subResult.left);
                unresolved.addAll(subResult.right);
            }
            if (interfaceType.object != null) {
                Pair<Set<DeclarationType>, Set<UnresolvedDeclarationType>> subResult = unfoldPartially(interfaceType.object, seen);
                types.addAll(subResult.left);
                unresolved.addAll(subResult.right);
            }
            if (interfaceType.getDynamicAccess() != null) {
                Pair<Set<DeclarationType>, Set<UnresolvedDeclarationType>> subResult = unfoldPartially(interfaceType.getDynamicAccess(), seen);
                types.addAll(subResult.left);
                unresolved.addAll(subResult.right);
            }

            return new Pair<>(types, unresolved);
        } else {
            return new Pair<>(new HashSet<>(Arrays.asList(type)), Collections.EMPTY_SET);
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
