package dk.webbies.tscreate;

/**
 * Created by Erik Krogh Kristensen on 15-09-2015.
 */
public class Options {
    public boolean separateFunctions = false;

    // If false: || and && just produces new EmptyUnionNode. Otherwise they are unioned and the result is returned.
    public boolean unionShortCircuitLogic = false;

    public Options() {

    }

    public static Options separateFunctions() {
        Options result = new Options();
        result.separateFunctions = true;
        return result;
    }

    public static Options unionedFunctions() {
        Options result = new Options();
        result.separateFunctions = false;
        return result;
    }
}
