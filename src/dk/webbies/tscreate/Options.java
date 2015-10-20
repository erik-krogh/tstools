package dk.webbies.tscreate;

/**
 * Created by Erik Krogh Kristensen on 15-09-2015.
 */
public class Options {

    public boolean includeThisNodeFromHeap = true;
    public boolean includeThisNodeFromConstructor = false;
    public boolean includeThisNodeFromPrototypeMethods = false;

    // Whether or not the heap should be resolved when recursively resolving other functions than the one we are currently analyzing (potentially expensive).
    public boolean interProceduralAnalysisWithHeap = true;

    public Options() {

    }
}
