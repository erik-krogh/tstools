package dk.webbies.tscreate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    public final List<Dependency> dependencies;

    private BenchMark (String name, String scriptPath, String declarationPath, Options options, LanguageLevel languageLevel) {
        this(name, scriptPath, declarationPath, options, languageLevel, Collections.EMPTY_LIST);
    }

    public List<String> dependencyScripts() {
        return this.dependencies.stream().map(dependency -> dependency.scriptPath).collect(Collectors.toList());
    }

    public List<String> dependencyDeclarations() {
        return this.dependencies.stream().map(dependency -> dependency.declarationPath).collect(Collectors.toList());
    }

    private BenchMark (String name, String scriptPath, String declarationPath, Options options, LanguageLevel languageLevel, List<Dependency> dependencies) {
        this.name = name;
        this.scriptPath = scriptPath;
        this.declarationPath = declarationPath;
        this.options = options;
        this.languageLevel = languageLevel;
        this.dependencies = Collections.unmodifiableList(dependencies);
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

        Dependency testDependency = new Dependency("tests/test/dependency.js", "tests/test/dependency.d.ts");

        return new BenchMark("D3.js", "tests/test/test.js", null, options, ES5, Arrays.asList(testDependency, Dependency.jQuery));
    });

    public static final class Dependency {
        public final String scriptPath;
        public final String declarationPath;

        public Dependency(String scriptPath, String declarationPath) {
            assert scriptPath != null;
            assert declarationPath != null;
            this.scriptPath = scriptPath;
            this.declarationPath = declarationPath;
        }

        private static final Dependency jQuery = new Dependency("tests/jquery/jquery.js", "tests/jquery/jquery.d.ts");
    }
}
