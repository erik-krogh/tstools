package dk.webbies.tscreate;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Erik Krogh Kristensen on 15-09-2015.
 */
public class Options {
    // Whether or not the heap should be resolved when recursively resolving other functions than the one we are currently analyzing (potentially expensive).
    public boolean interProceduralAnalysisWithHeap = true;
    public Collection<String> isClassNames = Arrays.asList("_");

    public static class ClassOptions {
        public boolean useConstructorUsages = false;
        public boolean useClassInstancesFromHeap = true;

        // For every prototype method, use the information gained by the "this" calls.
        public boolean unionThisFromObjectsInTheHeap = true;
        // If the above is true, these ones are only used as fallback (that is, they are then only used if there are no instances of the class found in the heap).
        public boolean unionThisFromPrototypeMethods = false;
        public boolean unionThisFromConstructor = true;
        public boolean unionThisFromConstructedObjects = false;
    }

    public final ClassOptions classOptions = new ClassOptions();
}
