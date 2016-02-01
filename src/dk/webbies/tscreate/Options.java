package dk.webbies.tscreate;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Erik Krogh Kristensen on 15-09-2015.
 */
public class Options {
    public Collection<String> isClassNames = Arrays.asList("_");

    public Runtime runtime = Runtime.CHROME;
    public boolean createInstances = true; // Should JSNAP instrument the code to dynamically create instances of every function it meets, which can be used to find the existence and types of fields in classes.
    public boolean createInstancesClassFilter = false; // Filters the above, to only create instances for functions that "look" like a class (very simple filter).

    // When we have some unionNode, that includes another union-node, and they both have fields. Something is done to make sure that the fields also know about that include (if the include has a matching field).
    // This option disables that, since it for some libraries (jQuery), runs EXTREMELY slow.
    public boolean resolveIncludesWithFields = true;

    // If false, then every time a value from the heap is used, it will be treated independently. And will not be unified.
    public boolean unionHeapIdentifiers = false; // makes next to zero difference, but false seems to be better.

    // Run TSCheck on the output.
    public boolean tsCheck = false;

    public boolean debugPrint = false;

    public boolean FunctionDotBind = false; // Emulating Function.bind can lead to some pretty nasty things, so i leave it out for now.

    public Integer maxEvaluationDepth = 1; // If set to a number, the evaluation will ignore recursive types and all that stuff, and simply follow EVERYTHING until a certain depth is reached. Note, some things that are at depth n will affect down to depth n+2 (such as for signatures with parameters).
    public boolean asyncTest = false;

    public boolean recordCalls = true;
    public int maxObjects = 1000;

    public enum Runtime {
        PHANTOM,
        CHROME,
        NODE
    }

    public static class ClassOptions {
        public boolean onlyUseThisWithFieldAccesses = true; // So as an example, in "foo(this)", this will not be unified with the functions this-node. But in "this.foo" it will.

        public boolean useConstructorUsages = false;
        public boolean useClassInstancesFromHeap = true;
        // If the above is true, these ones are only used as fallback (that is, they are then only used if there are no instances of the class found in the heap).

        // For every prototype method, use the information gained by the "this" calls.
        public boolean unionThisFromPrototypeMethods = false;
        public boolean unionThisFromConstructor = true;
        public boolean unionThisFromConstructedObjects = false; // As in objects that are constructed using "new Foo()" inside the function we are analyzing.
        public boolean useInstancesForThis = true; // If true, then the instances on the heap, will be used to in the static analysis to get information about "this".
    }

    public final ClassOptions classOptions = new ClassOptions();
}
