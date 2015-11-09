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
        public boolean onlyUseThisWithFieldAccesses = true; // So as an example, in "foo(this)", this will not be unified with the functions this-node. But in "this.foo" it will.

        public boolean useConstructorUsages = false;
        public boolean useClassInstancesFromHeap = true;

        // If the above is true, these ones are only used as fallback (that is, they are then only used if there are no instances of the class found in the heap).
        public boolean unionThisFromObjectsInTheHeap = true;
        // For every prototype method, use the information gained by the "this" calls.
        public boolean unionThisFromPrototypeMethods = true;
        public boolean unionThisFromConstructor = false; // Except in some very weird situations. This will be more accurately covered when we create an instance of the class.
        public boolean unionThisFromConstructedObjects = false; // As in objects that are constructed using "new Foo()" inside the function we are analyzing.
    }

    public final ClassOptions classOptions = new ClassOptions();
}
