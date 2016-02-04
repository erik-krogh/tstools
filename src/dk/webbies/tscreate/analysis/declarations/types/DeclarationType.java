package dk.webbies.tscreate.analysis.declarations.types;

import dk.webbies.tscreate.util.MappedCollection;
import dk.webbies.tscreate.util.Tarjan;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public abstract class DeclarationType {
    private static int instanceCounter = 0;
    public final int counter;

    public DeclarationType() {
        this.counter = instanceCounter++;
    }

    public abstract <T> T accept(DeclarationTypeVisitor<T> visitor);

    public abstract <T, A> T accept(DeclarationTypeVisitorWithArgument<T, A> visitor, A argument);

    public DeclarationType resolve() {
        return DeclarationType.resolve(this);
    }

    public static DeclarationType resolve(DeclarationType type) {
        if (type instanceof UnresolvedDeclarationType) {
            return DeclarationType.resolve(((UnresolvedDeclarationType) type).getResolvedType());
        } else if (type instanceof CombinationType) {
            return ((CombinationType) type).getCombined();
        } else {
            return type;
        }
    }


    private FindReachableTarjanNode reachableTypesTarjanNode = new FindReachableTarjanNode();

    private List<? extends DeclarationType> getChildren() {
        if (this instanceof UnresolvedDeclarationType) {
            UnresolvedDeclarationType unresolved = (UnresolvedDeclarationType) this;
            if (unresolved.isResolved()) {
                return Arrays.asList(unresolved.getResolvedType());
            } else {
                return Collections.EMPTY_LIST;
            }
        } else if (this instanceof CombinationType) {
            return ((CombinationType) this).types;
        } else if (this instanceof UnionDeclarationType) {
            return ((UnionDeclarationType) this).getTypes();
        } if (this instanceof InterfaceType) {
            List<DeclarationType> result = new ArrayList<>();
            InterfaceType interfaceType = (InterfaceType) this;
            if (interfaceType.function != null) {
                result.add(interfaceType.function);
            }
            if (interfaceType.object != null) {
                result.add(interfaceType.object);
            }
            if (interfaceType.getDynamicAccess() != null) {
                result.add(interfaceType.getDynamicAccess());
            }
            return result;
        }
        return Collections.EMPTY_LIST;
    }

    private class FindReachableTarjanNode extends Tarjan.Node<FindReachableTarjanNode> {
        @Override
        public Collection<FindReachableTarjanNode> getEdges() {
            return new MappedCollection<>(getChildren(), (dec) -> dec.reachableTypesTarjanNode);
        }

        DeclarationType getType() {
            return DeclarationType.this;
        }
    }

    public List<DeclarationType> getReachable() {
        return new Tarjan<FindReachableTarjanNode>().getReachableSet(reachableTypesTarjanNode).stream().map(FindReachableTarjanNode::getType).collect(Collectors.toList());
    }

    public static List<List<DeclarationType>> getLevels(Collection<? extends DeclarationType> types) {
        List<List<FindReachableTarjanNode>> levels = new Tarjan<FindReachableTarjanNode>().getLevels(new MappedCollection<>(types, (type) -> type.reachableTypesTarjanNode));
        return levels.stream().map(nodes -> nodes.stream().map(FindReachableTarjanNode::getType).collect(Collectors.toList())).collect(Collectors.toList());
    }
}
