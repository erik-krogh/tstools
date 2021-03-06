package dk.webbies.tscreate.jsnap.classes;

import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.jsnap.Snap;

import java.util.*;
import java.util.function.Function;

/**
 * Created by webbies on 31-08-2015.
 */
public class ClassHierarchyExtractor {
    private Snap.Obj globalObject;
    private Options options;

    public ClassHierarchyExtractor(Snap.Obj globalObject, Options options) {
        this.globalObject = globalObject;
        this.options = options;
    }

    private List<Snap.Obj> extractClasses(String prefixPath, Snap.Obj obj, Map<Snap.Obj, LibraryClass> classes, Set<Snap.Obj> seenObjects) {
        if (seenObjects.contains(obj)) {
            Snap.Property prototype = obj.getProperty("prototype");
            if (prototype != null && prototype.value != null && prototype.value instanceof Snap.Obj) {
                LibraryClass klass = classes.get(prototype.value);
                if (klass != null) {
                    klass.addPathSeen(prefixPath);
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
        LibraryClass libraryClass = new LibraryClass(path, prototype, options);
        if (prototype.properties.size() > 1) {
            libraryClass.setUsedAsClass(true);
        }
        classes.put(prototype, libraryClass);

        libraryClass.superClass = protoTypeToClass(path + ".[proto]", classes, prototype.prototype);

        return libraryClass;
    }

    public HashMap<Snap.Obj, LibraryClass> extract() {
        HashMap<Snap.Obj, LibraryClass> libraryClasses = new HashMap<>();
        HashSet<Snap.Obj> seen = new HashSet<>();
        // Reason for two passes: Names look prettier when we don't go through the environment.
        List<Snap.Obj> missingEnvs = this.extractClasses("window", this.globalObject, libraryClasses, seen);

        for (Snap.Obj missinEnv : missingEnvs) {
            for (Snap.Property property : missinEnv.properties) {
                if (property.value instanceof Snap.Obj) {
                    Snap.Obj obj = (Snap.Obj) property.value;
                    extractClasses("[ENV]." + property.name, obj, libraryClasses, seen);
                }
            }
        }

        VisitEntireHeap.visit(this.globalObject, (value) -> {
            if (value instanceof Snap.Obj) {
                // Marking every class, that has an instance in the heap.
                Snap.Obj obj = (Snap.Obj) value;
                if (obj.prototype != null) {
                    LibraryClass libraryClass = libraryClasses.get(obj.prototype);
                    if (libraryClass != null) {
//                        libraryClass.setUsedAsClass(true);
                    }
                }

                // Checking if it is an instance of a class, and saving it.
                if (obj.prototype != null && libraryClasses.containsKey(obj.prototype)) {
                    LibraryClass clazz = libraryClasses.get(obj.prototype);
                    clazz.addInstance(obj);
                }

                if (obj.getProperty("prototype") != null && obj.getProperty("prototype").value instanceof Snap.Obj) {
                    Snap.Obj prototype = (Snap.Obj) obj.getProperty("prototype").value;
                    if (libraryClasses.get(prototype) != null) {
                        libraryClasses.get(prototype).setConstructor(obj);
                    }
                }
            }


            return null;
        });

        markPrototypeFunctions(libraryClasses);

        for (LibraryClass libraryClass : libraryClasses.values()) {
            if (libraryClass.hasInstanceLookingLikeAClass()) {
                libraryClass.setUsedAsClass(true);
            }
        }

        fixIsUsedAsClass(libraryClasses.values());

        return libraryClasses;
    }

    private void markPrototypeFunctions(HashMap<Snap.Obj, LibraryClass> classes) {
        for (LibraryClass libraryClass : classes.values()) {
            for (Snap.Property property : libraryClass.getPrototype().getPropertyMap().values()) {
                if (property.name.equals("constructor")) {
                    continue;
                }
                Snap.Value value = property.value;
                if (value instanceof Snap.Obj && ((Snap.Obj) value).function != null && ((Snap.Obj) value).getProperty("prototype") != null) {
                    Snap.Obj prototype = (Snap.Obj) ((Snap.Obj) value).getProperty("prototype").value;
                    classes.get(prototype).prototypeMethod = true;
                }
            }
        }
    }

    // Sometimes, something is marked as "not a class", even though its super-class is a class.
    // So if one class in a "chain" is a class, then all of them is.
    private void fixIsUsedAsClass(Collection<LibraryClass> classes) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (LibraryClass aClass : classes) {
                boolean isUsedAsClass = aClass.isUsedAsClass();
                Set<LibraryClass> chain = new HashSet<>();
                while (aClass != null && !chain.contains(aClass)) {
                    isUsedAsClass |= aClass.isUsedAsClass();
                    chain.add(aClass);
                    aClass = aClass.superClass;
                }
                if (isUsedAsClass) {
                    for (LibraryClass libraryClass : chain) {
                        if (!libraryClass.isUsedAsClass()) {
                            changed = true;
                        }
                        libraryClass.setUsedAsClass(true);
                    }
                }
            }
        }
    }

    private static final class VisitEntireHeap {
        private final HashSet<Snap.Obj> seen = new HashSet<>();
        private List<Function<Snap.Value, Void>> callbacks;

        private VisitEntireHeap(List<Function<Snap.Value, Void>> callbacks) {
            this.callbacks = callbacks;
        }

        public static void visit(Snap.Obj globalObject, Function<Snap.Value, Void>... functions) {
            new VisitEntireHeap(Arrays.asList(functions)).visit(globalObject);
        }


        private void visit(Snap.Value value) {
            //noinspection RedundantCast
            if (value instanceof Snap.Obj && seen.contains((Snap.Obj)value)) {
                return;
            }

            for (Function<Snap.Value, Void> function : callbacks) {
                function.apply(value);
            }

            if (!(value instanceof Snap.Obj)) {
                return;
            }
            Snap.Obj obj = (Snap.Obj) value;
            seen.add(obj);

            for (Snap.Property prop : obj.getPropertyMap().values()) {
                visitProp(prop);
            }

            if (obj.env != null && obj.env.properties != null) {
                for (Snap.Property prop : obj.env.getPropertyMap().values()) {
                    visitProp(prop);
                }
            }

            if (obj.recordedCalls != null) {
                for (Snap.Property prop : obj.recordedCalls.getPropertyMap().values()) {
                    visitProp(prop);
                }
            }

            if (obj.prototype != null && obj.prototype.properties != null) {
                for (Snap.Property prop : obj.prototype.getPropertyMap().values()) {
                    visitProp(prop);
                }
            }
        }

        private void visitProp(Snap.Property prop) {
            if (prop.value instanceof Snap.Obj) {
                visit(prop.value);
            }
            if (prop.get != null && !(prop.get instanceof Snap.UndefinedConstant)) {
                visit(prop.get);
            }
            if (prop.set != null && !(prop.set instanceof Snap.UndefinedConstant)) {
                visit(prop.set);
            }
        }
    }
}
