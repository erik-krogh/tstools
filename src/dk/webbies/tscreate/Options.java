package dk.webbies.tscreate;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Erik Krogh Kristensen on 15-09-2015.
 */
public class Options {
    public boolean includeThisNodeFromHeap = true;
    public boolean includeThisNodeFromConstructor = false;
    public boolean includeThisNodeFromPrototypeMethods = false;

    // Whether or not the heap should be resolved when recursively resolving other functions than the one we are currently analyzing (potentially expensive).
    public boolean interProceduralAnalysisWithHeap = true;
    public Collection<String> isClassNames = Arrays.asList("_");

    public static class ClassOptions {
        public boolean useConstructorUsages = false;
        public boolean useClassInstancesFromHeap = true;

        // For every prototype method, use the information gained by the "this" calls.
        public boolean unionThisFromPrototypeMethods = false;
        public boolean unionThisFromConstructor = false;
        public boolean unionThisFromObjectsInTheHeap = false;
        public boolean unionThisFromConstructedObjects = false;
    }

    public final ClassOptions classOptions;
    public Options() {
        this.classOptions = new ClassOptions();
    }
}
