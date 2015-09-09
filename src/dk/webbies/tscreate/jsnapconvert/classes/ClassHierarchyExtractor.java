package dk.webbies.tscreate.jsnapconvert.classes;

import dk.webbies.tscreate.jsnapconvert.Snap;

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
        if (obj.env != null) {
            missingEnvs.add(obj.env);
        }
        return missingEnvs;
    }

    private void createLibraryClass(String path, Snap.Obj obj, Map<Snap.Obj, LibraryClass> classes) {
        if (obj == null) {
            return;
        }

        LibraryClass libraryClass = null;
        Snap.Obj prototype = (Snap.Obj) obj.getProperty("prototype").value;

        if (classes.containsKey(prototype)) {
            libraryClass = classes.get(prototype);
            if (libraryClass.collectedStaticFields) { // TODO: I don't need the static fields here, right?
                return;
            }
        }
        if (libraryClass == null) {
            libraryClass = protoTypeToClass(path, classes, prototype);
        }
        if (libraryClass == null) {
            return;
        }
        libraryClass.collectedStaticFields = true;
        for (Snap.Property property : obj.properties) {
            String name = property.name;
            if (!(name.equals("caller") || name.equals("length") || name.equals("arguments") || name.equals("prototype") || name.equals("name"))) {
                libraryClass.staticFields.put(property.name, property.value);
            }
        }
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

        libraryClass.superClass = protoTypeToClass(path + ".proto", classes, prototype.prototype);

        return libraryClass;
    }

    public HashMap<Snap.Obj, LibraryClass> extract() {
        HashMap<Snap.Obj, LibraryClass> classes = new HashMap<>();
        HashSet<Snap.Obj> seen = new HashSet<>();
        // Reason for two passes: Names look prettier when we don't go through the environment.
        List<Snap.Obj> missinEnvs = this.extractClasses("window", (Snap.Obj) this.snapshot, classes, seen);
        for (Snap.Obj missinEnv : missinEnvs) {
            for (Snap.Property property : missinEnv.properties) {
                if (property.value instanceof Snap.Obj) {
                    Snap.Obj obj = (Snap.Obj) property.value;
                    extractClasses("[ENV]." + property.name, obj, classes, seen);
                }
            }
        }

        return classes;
    }

    public static void main(String[] args) throws IOException {
        /*FunctionExpression emptyProgram = new FunctionExpression(null, new Identifier(null, ":program"), new BlockStatement(null, Collections.EMPTY_LIST), Collections.EMPTY_LIST);
        Snap.Obj librarySnapshot = JSNAPConverter.getStateDumpFromFile("lib/tscheck/tests/jquery.jsnap", emptyProgram);
        Snap.Obj domSnapshot = JSNAPConverter.getStateDumpFromFile("src/dk/webbies/tscreate/jsnapconvert/onlyDom.jsnap", emptyProgram);

        Snap.Obj libraryUnique = JSNAPConverter.extractUnique(librarySnapshot, domSnapshot);

        ClassHierarchyExtractor extractor = new ClassHierarchyExtractor(libraryUnique);

        HashMap<Snap.Obj, LibraryClass> classes = new HashMap<>();
        HashSet<Snap.Obj> seen = new HashSet<>();
        extractor.extractClasses("window", (Snap.Obj) libraryUnique, classes, seen, false);
        extractor.extractClasses("window", (Snap.Obj) libraryUnique, classes, seen, true);
        System.out.println();*/

    }
}
