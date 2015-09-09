package dk.webbies.tscreate.analysis.unionFind.nodes;

import dk.webbies.tscreate.jsnapconvert.classes.LibraryClass;

/**
 * Created by Erik Krogh Kristensen on 09-09-2015.
 */
// TODO: Not used by type-converter.
// TODO: Not used by heapConverter.
// TODO: HasPrototypeUnionNode
public class LibraryClassUnionNode implements UnionNode {
    private LibraryClass clazz;

    public LibraryClassUnionNode(LibraryClass clazz) {
        this.clazz = clazz;
    }

    public LibraryClass getLibaryClass() {
        return clazz;
    }
}
