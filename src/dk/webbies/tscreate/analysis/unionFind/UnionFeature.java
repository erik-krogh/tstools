package dk.webbies.tscreate.analysis.unionFind;

import dk.webbies.tscreate.analysis.declarations.types.PrimitiveDeclarationType;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 16-10-2015.
 */
public class UnionFeature {
    public final UnionClass unionClass;

    Set<Snap.Obj> prototypes = new HashSet<>();
    Set<Snap.Value> heapValues = new HashSet<>();
    Set<PrimitiveDeclarationType> primitives = new HashSet<>();
    boolean nonVoid = false;
    FunctionFeature functionFeature = null;
    Map<String, UnionNode> objectFields = new HashMap<>();
    public Set<String> typeNames = new HashSet<>();

    public UnionFeature(UnionClass unionClass) {
        this.unionClass = unionClass;
    }

    public void takeIn(UnionFeature other) {
        this.prototypes.addAll(other.prototypes);
        this.heapValues.addAll(other.heapValues);
        this.primitives.addAll(other.primitives);
        if (this.functionFeature == null) {
            this.functionFeature = other.functionFeature;
        } else if (other.functionFeature != null) {
            this.functionFeature.takeIn(other.functionFeature);
        }
        this.nonVoid = this.nonVoid || other.nonVoid;

        other.objectFields.forEach((name, node) -> {
            if (!this.objectFields.containsKey(name)) {
                this.objectFields.put(name, node);
            }
        });

        this.typeNames.addAll(other.typeNames);
    }

    public Set<Snap.Value> getHeapValues() {
        return heapValues;
    }

    public Set<PrimitiveDeclarationType> getPrimitives() {
        return primitives;
    }

    public FunctionFeature getFunctionFeature() {
        return functionFeature;
    }

    public Map<String, UnionNode> getObjectFields() {
        return objectFields;
    }

    public Set<String> getTypeNames() {
        return typeNames;
    }

    public Set<Snap.Obj> getPrototypes() {
        return prototypes;
    }

    public boolean getNonVoid() {
        return nonVoid;
    }

    public static class FunctionFeature {
        private final UnionNode thisNode;
        private final UnionNode returnNode;
        private final List<Argument> arguments;
        private final Set<Snap.Obj> closures = new HashSet<>();

        public FunctionFeature(UnionNode thisNode, UnionNode returnNode, List<Argument> arguments, Snap.Obj closure) {
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

        public List<Argument> getArguments() {
            return arguments;
        }

        public Set<Snap.Obj> getClosures() {
            return closures;
        }

        public void takeIn(FunctionFeature other) {
            // Everything except the closure, and some argument stuff, is handled by the fields in the UnionClass
            this.closures.addAll(other.closures);
            for (int i = 0; i < Math.min(this.arguments.size(), other.arguments.size()); i++) {
                Argument thisArg = this.arguments.get(i);
                Argument otherArg = other.arguments.get(i);
                if (thisArg.name.startsWith("arg")) {
                    thisArg.name = otherArg.name;
                } else if (otherArg.name.length() > thisArg.name.length()) {
                    thisArg.name = otherArg.name;
                }
            }
            if (other.arguments.size() > this.arguments.size()) {
                for (int i = this.arguments.size(); i < other.arguments.size(); i++) {
                    this.arguments.add(i, other.arguments.get(i));
                }
            }
        }

        public static class Argument {
            public String name;
            public final UnionNode node;

            public Argument(String name, UnionNode node) {
                this.name = name;
                this.node = node;
            }
        }
    }

    public static List<UnionFeature> getReachable(UnionFeature feature) {
        return feature.unionClass.getReachable(UnionClass::getFeature);
    }
}
