package dk.webbies.tscreate;

import dk.webbies.tscreate.util.Util;

import java.util.function.Function;
import java.util.function.Supplier;

import static dk.webbies.tscreate.Main.*;
import static dk.webbies.tscreate.Main.LanguageLevel.*;
import static dk.webbies.tscreate.util.Util.*;

/**
 * Created by Erik Krogh Kristensen on 18-11-2015.
 */
public class BenchMark {
    public final String name;
    public final String scriptPath;
    public final String declarationPath;
    public final Options options;
    public final LanguageLevel languageLevel;

    // TODO: Which one did run in ES6?
    private BenchMark (String name, String scriptPath, String declarationPath, Options options, LanguageLevel languageLevel) {
        this.name = name;
        this.scriptPath = scriptPath;
        this.declarationPath = declarationPath;
        this.options = options;
        this.languageLevel = languageLevel;
    }

    public static final BenchMark underscore = evaluate(() -> {
        Options options = new Options();
        options.createInstances = true;
        options.resolveIncludesWithFields = true;
        return new BenchMark("Underscore.js", "tests/underscore/underscore.js", null, options, ES5);
    });

    public static final BenchMark PIXI = evaluate(() -> {
        Options options = new Options();
        options.createInstances = true;
        options.resolveIncludesWithFields = true;
        return new BenchMark("PIXI", "tests/pixi/pixi.js", null, options, ES5);
    });

    public static final BenchMark FabricJS = evaluate(() -> {
        Options options = new Options();
        options.createInstances = true;
        options.resolveIncludesWithFields = true;
        return new BenchMark("FabricJS", "tests/fabric/fabric.js", null, options, ES5);
    });

    public static final BenchMark jQuery = evaluate(() -> {
        Options options = new Options();
        options.createInstances = true;
        options.resolveIncludesWithFields = false; // Takes too long... but is not very precise without.
        return new BenchMark("FabricJS", "tests/jquery/jquery.js", null, options, ES5);
    });

    public static final BenchMark three = evaluate(() -> {
        Options options = new Options();
        options.createInstances = true;
        options.resolveIncludesWithFields = true;
        return new BenchMark("three.js", "tests/three/three.js", null, options, ES6);
    });

    public static final BenchMark leaflet = evaluate(() -> {
        Options options = new Options();
        options.createInstances = true;
        options.resolveIncludesWithFields = true;
        return new BenchMark("three.js", "tests/three/three.js", null, options, ES5);
    });

    public static final BenchMark D3 = evaluate(() -> {
        Options options = new Options();
        options.createInstances = true;
        options.resolveIncludesWithFields = true;
        return new BenchMark("D3.js", "tests/d3/d3.js", null, options, ES5);
    });

    public static final BenchMark test = evaluate(() -> {
        Options options = new Options();
        options.createInstances = true;
        options.resolveIncludesWithFields = true;
        return new BenchMark("D3.js", "tests/test.js", null, options, ES5);
    });
}
