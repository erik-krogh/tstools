package dk.webbies.tscreate;

/**
 * Created by Erik Krogh Kristensen on 15-09-2015.
 */
public class Options {
    // If false: || and && just produces new EmptyUnionNode. Otherwise they are unioned and the result is returned.
    public boolean unionShortCircuitLogic = false;
    public boolean includeThisNodeFromHeap = true;
    public boolean includeThisNodeFromConstructor = false;
    public boolean includeThisNodeFromPrototypeMethods = false;

    public Options() {

    }
}
