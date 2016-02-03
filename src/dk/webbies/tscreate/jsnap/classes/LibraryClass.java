package dk.webbies.tscreate.jsnap.classes;

import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.HeapValueFactory;
import dk.webbies.tscreate.analysis.declarations.types.InterfaceType;
import dk.webbies.tscreate.analysis.unionFind.EmptyNode;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.analysis.unionFind.UnionNode;
import dk.webbies.tscreate.declarationReader.DeclarationParser.NativeClassesMap;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.paser.AST.Expression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by webbies on 02-09-2015.
 */
public class LibraryClass {
    private List<String> pathsSeen = new ArrayList<>();
    public LibraryClass superClass;
    private String name = null;

    public final List<UnionNode> constructorNodes = new ArrayList<>();
    public final List<UnionNode> thisNodes = new ArrayList<>();

    public boolean isUsedAsClass = false; // All functions are potential library classes, this marks if it is actually used as a class.

    public final Snap.Obj prototype;

    private List<Snap.Obj> instances = new ArrayList<>();
    private Snap.Obj constructor;
    public boolean prototypeMethod = false;
    private Set<Expression> uniqueConstructionSites = new HashSet<>();
    private Options options;

    public LibraryClass(String pathSeen, Snap.Obj prototype, Options options) {
        this.options = options;
        this.pathsSeen.add(pathSeen);
        this.prototype = prototype;
    }

    public Snap.Obj getPrototype() {
        return prototype;
    }

    public UnionNode getNewConstructorNode(UnionFindSolver solver) {
        EmptyNode result = new EmptyNode(solver);
        constructorNodes.add(result);
        return result;
    }

    public void addPathSeen(String path) {
        assert this.name == null;
        if (!this.pathsSeen.contains(path)) {
            this.pathsSeen.add(path);
        }
    }

    public UnionNode getNewThisNode(UnionFindSolver solver) {
        EmptyNode result = new EmptyNode(solver);
        thisNodes.add(result);
        return result;
    }

    public String getName(NativeClassesMap natives, Set<String> takenNames) {
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
                if (newName.matches("[0-9]+.{0,}")) { // Starts with number.
                    continue;
                }
                if (newName.contains("-")) {
                    continue;
                }

                boolean newIsUpper = newName.charAt(0) == Character.toUpperCase(newName.charAt(0));
                boolean oldIsUpper = name.charAt(0) == Character.toUpperCase(name.charAt(0)) && !(name.charAt(0) == "[".charAt(0));

                if (newIsUpper && !oldIsUpper) {
                    name = newName;
                }
                if (newName.length() > name.length() && !(!newIsUpper && oldIsUpper) && !newName.toLowerCase().contains("class")) {
                    name = newName;
                }
            }

            if (name.matches("[0-9]+.{0,}")) {
                name = "interface_" + InterfaceType.interfaceCounter++;
            }
            if (name.contains("-")) {
                name = name.replace("-", "");
            }

            if (isNameTaken(name, natives, takenNames)) {
                int counter = 1;
                while (true) {
                    if (!isNameTaken(name + counter, natives, takenNames)) {
                        name = name + counter;
                        break;
                    } else {
                        counter++;
                    }
                }
            }

            takenNames.add(name);
        }
        return name;
    }

    private boolean isNameTaken(String newName, NativeClassesMap natives, Set<String> takenNames) {
        if (natives.getNativeTypeNames().contains(newName)) {
            return true;
        }
        //noinspection RedundantIfStatement
        if (takenNames.contains(newName)) {
            return true;
        }
        return false;
    }

    private String getNameFromPath(String path) {
        String[] split = path.split("\\.");
        return split[split.length - 1];
    }

    public boolean isNativeClass() {
        Snap.Obj constructor = getConstructor();
        return constructor != null && constructor.function.type.equals("native");
    }

    public void setConstructor(Snap.Obj constructor) {
        if (this.constructor != null && this.constructor != constructor) {
            throw new RuntimeException("Cannot have two constructors to the same class");
        }
        this.constructor = constructor;
        if (constructor.function != null && constructor.function.instance != null) {
            this.addInstance(constructor.function.instance);
        }
        JSNAPUtil.getCalls(constructor.recordedCalls).stream().filter(call -> call.isNew).map(call -> call.callThis).forEach(this::addInstance);
    }

    public void addInstance(Snap.Obj instance) {
        // FIXME: Make sure that the prototype on the instance match.
        this.instances.add(instance);
        if (this.superClass != null && this.superClass != this) {
            this.superClass.addInstance(instance);
        }
    }

    public Snap.Obj getConstructor() {
        if (constructor == null) {
            Snap.Property constructorProp = this.prototype.getProperty("constructor");
            if (constructorProp != null && constructorProp.value instanceof Snap.Obj) {
                Snap.Obj constructor = (Snap.Obj) constructorProp.value;
                if (constructor.function != null) {
                    setConstructor(constructor);
                    return constructor;
                }
            }
        }
        return constructor;
    }

    public boolean hasInstanceLookingLikeAClass() {
        getConstructor(); // Ugly, but makes sure things about the constructor is figured out.
        if (this.instances.isEmpty()) {
            return false;
        }
        if (this.prototypeMethod) {
            return false;
        }
        for (Snap.Obj instance : this.instances) {
            if (!instance.getPropertyMap().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public List<UnionNode> getThisNodeFromInstances(HeapValueFactory heapFactory) {
        List<Snap.Obj> instances = this.instances;
        if (this.instances.size() > options.maxObjects) {
            instances = this.instances.subList(0, options.maxObjects);
        }
        return instances.stream().map(heapFactory::fromValue).collect(Collectors.toList());
    }

    public List<Snap.Obj> getInstances() {
        return this.instances;
    }

    public void addUniqueConstructionSite(Expression expression) {
        this.uniqueConstructionSites.add(expression);
    }

    public void removeUniqueConstructionSite(Expression expression) {
        this.uniqueConstructionSites.remove(expression);
    }

    public Set<Expression> getUniqueConstructionSite() {
        return uniqueConstructionSites;
    }
}

