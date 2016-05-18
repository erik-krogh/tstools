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

    public boolean debugPrint = true;

    public boolean FunctionDotBind = false; // Emulating Function.bind can lead to some pretty nasty things, so i leave it out for now.

    public Integer maxEvaluationDepth = null; // If set to a number, the evaluation will ignore recursive types and all that stuff, and simply follow EVERYTHING until a certain depth is reached. Note, some things that are at depth n will affect down to depth n+2 (such as for signatures with parameters).
    public boolean asyncTest = false;

    public boolean recordCalls = true;
    public int maxObjects = 1000;

    // If enabled, then for functions where we have information about all arguments and the return, we use that information instead of doing the static analysis.
    public boolean skipStaticAnalysisWhenPossible = true;

    // Deactivates all but the trivial part of the type-inference. When doing this, there is union-types everywhere, instead of more readable types.
    public boolean reduceNothing = false;

    public boolean printStringIndexers = false;

    public EvaluationMethod evaluationMethod = EvaluationMethod.ONLY_FUNCTIONS;
    public boolean filterResultBasedOnDeclaration = false;

    public enum EvaluationMethod {
        ONLY_FUNCTIONS,
        ONLY_HEAP,
        EVERYTHING;
    }

    /*
    foo(arguments) <- callsite

    function foo(parameters) {
        return return; <- return
    }
     */
    public boolean disableFlowFromParamsToArgs = false;
    public boolean disableFlowFromArgsToParams = false;
    public boolean disableFlowFromReturnToCallsite = false;
    public boolean disableFlowFromCallsiteToReturn = false;

    public boolean evaluationSkipExcessProperties = true;  // TODO: Look at this again later.

    public boolean allArgumentsAreOptional = false;

    public boolean unifyShortCurcuitOrsAtAssignments = false;
    public boolean combineInterfacesAfterAnalysis = false;

    public boolean neverPrintModules = false;
    public boolean evaluationAnyAreOK = false; // In the evaluation, any and anything is a true-positive.
    public boolean useJSDoc = false; // TODO: Stuff and things.

    public enum StaticAnalysisMethod {
        NONE("none", "none"),
        COMBINED("combined", "combined"),
        MIXED("mixed", "mixed"),
        UPPER("upper", "upper"),
        LOWER_CONTEXT_SENSITIVE("lower, cs", "lower_cs"),
        UPPER_LOWER("upper_lower", "upper_lower"),
        UPPER_LOWER_CONTEXT_SENSITIVE("upper_lower, cs", "upper_lower_cs"),
        ANDERSON("subsets", "subsets"),
        COMBINED_CONTEXT_SENSITIVE("combined, CS", "combined_cs"),
        MIXED_CONTEXT_SENSITIVE("mixed, CS", "mixed_cs"),
        ANDERSON_CONTEXT_SENSITIVE("subsets, CS", "subsets_cs"),
        OLD_UNIFICATION_CONTEXT_SENSITIVE("old unify, CS", "oldUnify_cs"),
        OLD_UNIFICATION("old unify", "oldUnify"),
        UNIFICATION_CONTEXT_SENSITIVE("unify, CS", "unify_cs"),
        UNIFICATION("unify", "unify");
        public final String prettyString;
        public final String fileSuffix;

        StaticAnalysisMethod(String prettyString, String fileSuffix) {
            this.prettyString = prettyString;
            this.fileSuffix = fileSuffix;
        }
    }

    public StaticAnalysisMethod staticMethod = StaticAnalysisMethod.MIXED;

    public enum Runtime {
        PHANTOM,
        CHROME,
        NODE
    }

    public static class ClassOptions {
        public boolean onlyUseThisWithFieldAccesses = false; // So as an example, in "foo(this)", this will not be unified with the functions this-node. But in "this.foo" it will.

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
