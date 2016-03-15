package dk.webbies.tscreate.analysis.unionFind;

import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.util.Pair;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 16-10-2015.
 */
public class UnionFeature {
    public final UnionClass unionClass;

    Set<Snap.Obj> prototypes = null;
    Set<PrimitiveDeclarationType.Type> primitives = null;
    FunctionFeature functionFeature = null;
    Map<String, UnionNode> objectFields = null;
    public Set<Pair<String, Boolean>> typeNames = null; // The boolean is if it is a base-type.

    // Dynamic access
    UnionNode dynamicAccessReturnType = null;
    UnionNode dynamicAccessLookupExp = null;
    Set<String> names = null;

    public UnionFeature(UnionClass unionClass) {
        this.unionClass = unionClass;
    }

    public void takeIn(UnionFeature other) {
        if (other.prototypes != null && !other.prototypes.isEmpty()) {
            if (this.prototypes == null) {
                this.prototypes = new LinkedHashSet<>();
            }
            this.prototypes.addAll(other.prototypes);
        }

        if (other.primitives != null && !other.primitives.isEmpty()) {
            if (this.primitives == null) {
                this.primitives = new LinkedHashSet<>();
            }
            this.primitives.addAll(other.primitives);
        }


        if (this.functionFeature == null) {
            this.functionFeature = other.functionFeature;
        } else if (other.functionFeature != null) {
            this.functionFeature.takeIn(other.functionFeature);
        }

        if (other.objectFields != null && !other.objectFields.isEmpty()) {
            if (this.objectFields == null) {
                this.objectFields = new HashMap<>();
            }
            other.objectFields.forEach((name, node) -> {
                if (!this.objectFields.containsKey(name)) {
                    this.objectFields.put(name, node);
                }
            });
        }


        if (other.typeNames != null && !other.typeNames.isEmpty()) {
            if (this.typeNames == null) {
                this.typeNames = new LinkedHashSet<>();
            }
            this.typeNames.addAll(other.typeNames);
        }

        if (other.dynamicAccessLookupExp != null) {
            this.dynamicAccessLookupExp = other.dynamicAccessLookupExp;
        }
        if (other.dynamicAccessReturnType != null) {
            this.dynamicAccessReturnType = other.dynamicAccessReturnType;
        }

        if (other.names != null) {
            if (this.names == null) {
                this.names = other.names;
            } else {
                this.names.addAll(other.names);
            }
        }
    }

    public Set<String> getNames() {
        if (names == null) {
            return Collections.EMPTY_SET;
        }
        return names;
    }

    public Set<PrimitiveDeclarationType.Type> getPrimitives() {
        if (primitives == null) {
            return Collections.EMPTY_SET;
        }
        return primitives;
    }

    public FunctionFeature getFunctionFeature() {
        return functionFeature;
    }

    public Map<String, UnionNode> getObjectFields() {
        if (objectFields == null) {
            return Collections.EMPTY_MAP;
        }
        return objectFields;
    }

    public Set<Pair<String, Boolean>> getTypeNames() {
        if (typeNames == null) {
            return Collections.EMPTY_SET;
        }
        return typeNames;
    }

    public Set<Snap.Obj> getPrototypes() {
        if (this.prototypes == null) {
            return Collections.EMPTY_SET;
        }
        return prototypes;
    }

    public UnionNode getDynamicAccessReturnType() {
        return dynamicAccessReturnType;
    }

    public UnionNode getDynamicAccessLookupExp() {
        return dynamicAccessLookupExp;
    }

    public static class FunctionFeature {
        private final UnionNode thisNode;
        private final UnionNode returnNode;
        private final List<UnionNode> arguments;
        private final Set<Snap.Obj> closures = new HashSet<>();

        public FunctionFeature(UnionNode thisNode, UnionNode returnNode, List<UnionNode> arguments, Snap.Obj closure) {
            this.thisNode = thisNode;
            this.returnNode = returnNode;
            this.arguments = arguments;
            if (closure != null) {
                closures.add(closure);
            }
        }

        public UnionNode getThisNode() {
            return thisNode;
        }

        public UnionNode getReturnNode() {
            return returnNode;
        }

        public List<UnionNode> getArguments() {
            return arguments;
        }

        public Set<Snap.Obj> getClosures() {
            return closures;
        }

        public void takeIn(FunctionFeature other) {
            // Everything except the closure, and getting the argument list to the right size, is handled in UnionClass.
            this.closures.addAll(other.closures);
            if (other.arguments.size() > this.arguments.size()) {
                for (int i = this.arguments.size(); i < other.arguments.size(); i++) {
                    this.arguments.add(i, other.arguments.get(i));
                }
            }
        }
    }

    public static List<UnionFeature> getReachable(UnionFeature feature) {
        return feature.unionClass.getReachable(UnionClass::getFeature);
    }
}
