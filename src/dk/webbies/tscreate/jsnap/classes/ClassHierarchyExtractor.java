package dk.webbies.tscreate.jsnap.classes;

import dk.webbies.tscreate.jsnap.Snap;

import java.util.*;

/**
 * Created by webbies on 31-08-2015.
 */
public class ClassHierarchyExtractor {
    private Snap.Obj snapshot;

    public ClassHierarchyExtractor(Snap.Obj librarySnapshot) {
        this.snapshot = librarySnapshot;
    }

    private List<Snap.Obj> extractClasses(String prefixPath, Snap.Obj obj, Map<Snap.Obj, LibraryClass> classes, Set<Snap.Obj> seenObjects) {
        if (seenObjects.contains(obj)) {
            Snap.Property prototype = obj.getProperty("prototype");
            if (prototype != null && prototype.value != null && prototype.value instanceof Snap.Obj) {
                LibraryClass klass = classes.get(prototype.value);
                if (klass != null && !klass.pathsSeen.contains(prefixPath)) {
                    klass.pathsSeen.add(prefixPath);
                }
            }
            return Collections.EMPTY_LIST;
        }
        seenObjects.add(obj);
        ArrayList<Snap.Obj> missingEnvs = new ArrayList<>();

        if (obj.function != null && obj.properties != null) {
            Snap.Property prototypeProperty = obj.getProperty("prototype");
            if (prototypeProperty != null && prototypeProperty.value instanceof Snap.Obj) {
                createLibraryClass(prefixPath, obj, classes);
            }
        }

        if (obj.properties != null) {
            for (Snap.Property property : obj.properties) {
                if (property.value instanceof Snap.Obj) {
                    missingEnvs.addAll(extractClasses(prefixPath + "." + property.name, (Snap.Obj) property.value, classes, seenObjects));
                }
            }
        }

        if (obj.prototype != null) {
            for (Snap.Property property : obj.prototype.properties) {
                if (property.value instanceof Snap.Obj) {
                    missingEnvs.addAll(extractClasses(prefixPath + ".[prototype]." + property.name, (Snap.Obj) property.value, classes, seenObjects));
                }
            }
        }


        if (obj.env != null) {
            missingEnvs.add(obj.env);
        }
        return missingEnvs;
    }

    private void createLibraryClass(String path, Snap.Obj obj, Map<Snap.Obj, LibraryClass> classes) {
        if (obj == null) {
            return;
        }
        Snap.Obj prototype = (Snap.Obj) obj.getProperty("prototype").value;

        if (classes.containsKey(prototype)) {
            return;
        }
        protoTypeToClass(path, classes, prototype);
    }

    private LibraryClass protoTypeToClass(String path, Map<Snap.Obj, LibraryClass> classes, Snap.Obj prototype) {
        if (prototype == null) {
            return null;
        }
        // This is the case when we are dealing with Object.prototype, since it doesn't have any super-class.
        if (prototype.prototype == null) {
            return null;
        }
        if (classes.containsKey(prototype)) {
            return classes.get(prototype);
        }
        LibraryClass libraryClass = new LibraryClass(path, prototype);
        if (prototype.properties.size() > 1) {
            libraryClass.isUsedAsClass = true;
        }
        classes.put(prototype, libraryClass);

        libraryClass.superClass = protoTypeToClass(path + ".[proto]", classes, prototype.prototype);

        return libraryClass;
    }

    public HashMap<Snap.Obj, LibraryClass> extract() {
        HashMap<Snap.Obj, LibraryClass> classes = new HashMap<>();
        HashSet<Snap.Obj> seen = new HashSet<>();
        // Reason for two passes: Names look prettier when we don't go through the environment.
        List<Snap.Obj> missingEnvs = this.extractClasses("window", this.snapshot, classes, seen);

        for (Snap.Obj missinEnv : missingEnvs) {
            for (Snap.Property property : missinEnv.properties) {
                if (property.value instanceof Snap.Obj) {
                    Snap.Obj obj = (Snap.Obj) property.value;
                    extractClasses("[ENV]." + property.name, obj, classes, seen);
                }
            }
        }

        return classes;
    }
}
