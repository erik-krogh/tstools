package dk.webbies.tscreate.jsnap.classes;

import dk.webbies.tscreate.analysis.unionFind.EmptyUnionNode;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.analysis.unionFind.UnionNode;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by webbies on 02-09-2015.
 */
public class LibraryClass {
    public List<String> pathsSeen = new ArrayList<>();
    public LibraryClass superClass;
    private String name = null;

    public final List<UnionNode> constructorNodes = new ArrayList<>();
    public final List<UnionNode> thisNodes = new ArrayList<>();

    public boolean isUsedAsClass = false; // All functions are potential library classes, this marks if it is actually used as a class.

    public final Snap.Obj prototype;

    public List<Snap.Obj> instances = new ArrayList<>();

    public LibraryClass(String pathSeen, Snap.Obj prototype) {
        this.pathsSeen.add(pathSeen);
        this.prototype = prototype;
    }

    public UnionNode getNewConstructorNode(UnionFindSolver solver) {
        EmptyUnionNode result = new EmptyUnionNode(solver);
        constructorNodes.add(result);
        return result;
    }

    public UnionNode getNewThisNode(UnionFindSolver solver) {
        EmptyUnionNode result = new EmptyUnionNode(solver);
        thisNodes.add(result);
        return result;
    }

    // TODO: Somehow make sure that this doesn't conflict.
    public String getName() {
        if (name == null) {
            name = getNameFromPath(pathsSeen.get(0));
            // Finding the longest name, that preferably starts with an upper case.
            for (int i = 1; i < pathsSeen.size(); i++) {
                String path = pathsSeen.get(i);
                String newName = getNameFromPath(path);
                if (newName.equals("constructor")) {
                    continue;
                }
                if (newName.equals("[proto]")) {
                    continue;
                }

                boolean newIsUpper = newName.charAt(0) == Character.toUpperCase(newName.charAt(0));
                boolean oldIsUpper = name.charAt(0) == Character.toUpperCase(name.charAt(0)) && !(name.charAt(0) == "[".charAt(0));

                if (newIsUpper && !oldIsUpper) {
                    name = newName;
                }
                if (newName.length() > name.length() && !(!newIsUpper && oldIsUpper)) {
                    name = newName;
                }
            }
        }
        return name;
    }

    private String getNameFromPath(String path) {
        String[] split = path.split("\\.");
        return split[split.length - 1];
    }

    public boolean isNativeClass() {
        Snap.Property constructorProp = this.prototype.getProperty("constructor");
        if (constructorProp == null) {
            return false;
        }
        Snap.Obj constructor = (Snap.Obj) constructorProp.value;
        return constructor.function.type.equals("native");
    }

}

