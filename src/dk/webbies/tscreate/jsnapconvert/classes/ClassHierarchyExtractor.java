package dk.webbies.tscreate.jsnapconvert.classes;

import dk.webbies.tscreate.jsnapconvert.JSNAPConverter;
import dk.webbies.tscreate.jsnapconvert.Snap;
import dk.webbies.tscreate.paser.BlockStatement;
import dk.webbies.tscreate.paser.FunctionExpression;

import java.io.IOException;
import java.util.*;

/**
 * Created by webbies on 31-08-2015.
 */
public class ClassHierarchyExtractor {
    private Snap.Obj snapshot;

    public ClassHierarchyExtractor(Snap.Obj librarySnapshot) {
        this.snapshot = librarySnapshot;
    }

    private void extractClasses(String prefixPath, Snap.Obj obj, Map<Snap.Obj, LibraryClass> classes, Set<Snap.Obj> seenObjects, boolean includeEnv) {
        if (seenObjects.contains(obj)) {
            Snap.Property prototype = obj.getProperty("prototype");
            if (prototype != null && prototype.value != null && prototype.value instanceof Snap.Obj) {
                LibraryClass klass = classes.get(prototype.value);
                if (klass != null && !klass.pathsSeen.contains(prefixPath)) {
                    klass.pathsSeen.add(prefixPath);
                }
            }
            return;
        }
        seenObjects.add(obj);

        if (obj.function != null && obj.properties != null) {
            Snap.Property prototypeProperty = obj.getProperty("prototype");
            if (prototypeProperty != null && prototypeProperty.value instanceof Snap.Obj) {
                Snap.Obj prototypeObj = (Snap.Obj) prototypeProperty.value;
                if (prototypeObj.properties.size() > 1) {
                    LibraryClass libraryClass = createLibraryClass(prefixPath, obj, classes);
                }
            }
        }

        if (obj.properties != null) {
            for (Snap.Property property : obj.properties) {
                if (property.value instanceof Snap.Obj) {
                    extractClasses(prefixPath + "." + property.name, (Snap.Obj) property.value, classes, seenObjects, includeEnv);
                }
            }
        }
        if (includeEnv) {
            if (obj.env != null && obj.env instanceof Snap.Obj) {
                extractClasses(prefixPath + ".[ENV]", (Snap.Obj) obj.env, classes, seenObjects, includeEnv);
            }
        }
    }

    private LibraryClass createLibraryClass(String path, Snap.Obj obj, Map<Snap.Obj, LibraryClass> classes) {
        if (obj == null) {
            return null;
        }

        LibraryClass libraryClass = null;
        Snap.Obj prototype = (Snap.Obj) obj.getProperty("prototype").value;

        if (classes.containsKey(prototype)) {
            libraryClass = classes.get(prototype);
            if (libraryClass.collectedStaticFields) {
                return libraryClass;
            }
        }
        if (libraryClass == null) {
            libraryClass = protoTypeToClass(path, classes, prototype);
        }
        if (libraryClass == null) {
            return null;
        }
        libraryClass.collectedStaticFields = true;
        for (Snap.Property property : obj.properties) {
            String name = property.name;
            if (!(name.equals("caller") || name.equals("length") || name.equals("arguments") || name.equals("prototype") || name.equals("name"))) {
                libraryClass.staticFields.put(property.name, property.value);
            }
        }

        return libraryClass;
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
        classes.put(prototype, libraryClass);

        for (Snap.Property property : prototype.properties) {
            if (property.name.equals("constructor")) {
                continue;
            }
            libraryClass.prototypeProperties.put(property.name, property.value);
        }

        libraryClass.superClass = protoTypeToClass(path + ".proto", classes, (Snap.Obj) prototype.prototype);

        return libraryClass;
    }

    public HashMap<Snap.Obj, LibraryClass> extract() {
        HashMap<Snap.Obj, LibraryClass> classes = new HashMap<>();
        HashSet<Snap.Obj> seen = new HashSet<>();
        // Reason for two passes: Names look prettier when we don't go through the environment.
        this.extractClasses("window", (Snap.Obj) this.snapshot, classes, seen, false);
        this.extractClasses("window", (Snap.Obj) this.snapshot, classes, seen, true);
        return classes;
    }

    public static void main(String[] args) throws IOException {
        FunctionExpression emptyProgram = new FunctionExpression(":program", new BlockStatement(0, Collections.EMPTY_LIST), Collections.EMPTY_LIST);
        Snap.Obj librarySnapshot = JSNAPConverter.getStateDumpFromFile("lib/tscheck/tests/jquery.jsnap", emptyProgram);
        Snap.Obj domSnapshot = JSNAPConverter.getStateDumpFromFile("src/dk/webbies/tscreate/jsnapconvert/onlyDom.jsnap", emptyProgram);

        Snap.Obj libraryUnique = JSNAPConverter.extractUnique(librarySnapshot, domSnapshot);

        ClassHierarchyExtractor extractor = new ClassHierarchyExtractor(libraryUnique);

        HashMap<Snap.Obj, LibraryClass> classes = new HashMap<>();
        HashSet<Snap.Obj> seen = new HashSet<>();
        extractor.extractClasses("window", (Snap.Obj) libraryUnique, classes, seen, false);
        extractor.extractClasses("window", (Snap.Obj) libraryUnique, classes, seen, true);
        System.out.println();

    }
}
