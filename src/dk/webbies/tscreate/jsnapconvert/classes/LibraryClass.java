package dk.webbies.tscreate.jsnapconvert.classes;

import dk.webbies.tscreate.analysis.unionFind.nodes.EmptyUnionNode;
import dk.webbies.tscreate.analysis.unionFind.nodes.UnionNode;
import dk.webbies.tscreate.jsnapconvert.Snap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by webbies on 02-09-2015.
 */
public class LibraryClass {
    public List<String> pathsSeen = new ArrayList<>();
    public LibraryClass superClass;

    public UnionNode thisNode = new EmptyUnionNode();

    public boolean isUsedAsClass = false; // All functions are potential library classes, this marks if it is actually used as a class.

    public Snap.Obj prototype;

    public LibraryClass(String pathSeen, Snap.Obj prototype) {
        this.pathsSeen.add(pathSeen);
        this.prototype = prototype;
    }
}
