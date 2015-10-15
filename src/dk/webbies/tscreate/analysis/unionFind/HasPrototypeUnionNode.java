package dk.webbies.tscreate.analysis.unionFind;

import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;

import java.util.Map;
import java.util.Objects;

/**
 * Created by Erik Krogh Kristensen on 09-09-2015.
 */
public class HasPrototypeUnionNode extends UnionNode {
    private Snap.Obj prototype;

    private HasPrototypeUnionNode(Snap.Obj prototype) {
        if (prototype == null) {
            throw new RuntimeException();
        }
        this.prototype = prototype;
    }

    public Snap.Obj getPrototype() {
        return prototype;
    }

    public static class Factory {
        private final Map<Snap.Obj, LibraryClass> libraryClasses;

        public Factory(Map<Snap.Obj, LibraryClass> libraryClasses) {
            this.libraryClasses = libraryClasses;
        }

        public HasPrototypeUnionNode create(Snap.Obj prototype) {
            if (libraryClasses.containsKey(prototype)) {
                libraryClasses.get(prototype).isUsedAsClass = true;
            }
            return new HasPrototypeUnionNode(prototype);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HasPrototypeUnionNode that = (HasPrototypeUnionNode) o;
        return Objects.equals(prototype, that.prototype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prototype);
    }
}
