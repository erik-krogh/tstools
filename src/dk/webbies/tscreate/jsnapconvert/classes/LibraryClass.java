package dk.webbies.tscreate.jsnapconvert.classes;

import dk.webbies.tscreate.jsnapconvert.Snap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by webbies on 02-09-2015.
 */
public class LibraryClass {
    List<String> pathsSeen = new ArrayList<>();
    Map<String, Snap.Value> prototypeProperties = new HashMap<>();
    Map<String, Snap.Value> staticFields = new HashMap<>();
    LibraryClass superClass;
    Snap.Obj prototype;

    boolean collectedStaticFields = false;

    public LibraryClass(String pathSeen, Snap.Obj prototype) {
        this.pathsSeen.add(pathSeen);
        this.prototype = prototype;
    }
}
