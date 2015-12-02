package dk.webbies.tscreate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.Main.LanguageLevel;
import static dk.webbies.tscreate.Main.LanguageLevel.ES5;
import static dk.webbies.tscreate.Main.LanguageLevel.ES6;
import static dk.webbies.tscreate.util.Util.evaluate;

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

    public static final List<BenchMark> allBenchmarks = new ArrayList<>();

    private BenchMark (String name, String scriptPath, String declarationPath, Options options, LanguageLevel languageLevel, List<Dependency> dependencies) {
        allBenchmarks.add(this);
        this.name = name;
        this.scriptPath = scriptPath;
        this.declarationPath = declarationPath;
        this.options = options;
        this.languageLevel = languageLevel;
        this.dependencies = Collections.unmodifiableList(dependencies);
    }

    public static final BenchMark underscore = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("Underscore.js", "tests/underscore/underscore.js", null, options, ES5);
    });

    public static final BenchMark PIXI = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("PIXI", "tests/pixi/pixi.js", null, options, ES5);
    });

    public static final BenchMark FabricJS = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("FabricJS", "tests/fabric/fabric.js", null, options, ES5);
    });

    // TODO: test, including includesWithFields.
    public static final BenchMark jQuery = evaluate(() -> {
        Options options = new Options();
        options.resolveIncludesWithFields = true; // Takes too long... but is not very precise without.
        return new BenchMark("jQuery", "tests/jquery/jquery.js", null, options, ES5);
    });

    public static final BenchMark angular = evaluate(() -> {
        Options options = new Options();
        options.createInstancesClassFilter = true;
        return new BenchMark("AngularJS", "tests/angular/angular.js", null, options, ES5);
    });

    public static final BenchMark three = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("three.js", "tests/three/three.js", null, options, ES6);
    });

    public static final BenchMark leaflet = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("leaflet.js", "tests/leaflet/leaflet.js", null, options, ES5);
    });

    public static final BenchMark D3 = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("D3.js", "tests/d3/d3.js", null, options, ES5);
    });

    public static final BenchMark react = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("React", "tests/react/react.js", null, options, ES5);
    });

    public static final BenchMark knockout = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("Knockout", "tests/knockout/knockout.js", null, options, ES5);
    });

    // The stress test to rule all stress-tests. 10MB of JavaScript, 220000 lines of code.
    public static final BenchMark ExtJS = evaluate(() -> {
        Options options = new Options();
        options.createInstances = false;
        return new BenchMark("Ext JS", "tests/extjs/ext.js", null, options, ES5);
    });

    public static final BenchMark ember = evaluate(() -> {
        Options options = new Options();
        options.createInstances = false;
        return new BenchMark("Ember.js", "tests/ember/ember.js", null, options, ES5, Arrays.asList(Dependency.jQuery));
    });

    public static final BenchMark backbone = evaluate(() -> {
        Options options = new Options();
        options.createInstances = false;
        return new BenchMark("Backbone.js", "tests/backbone/backbone.js", null, options, ES5, Arrays.asList(Dependency.underscore));
    });

    // TODO: Lots of classes with duplicate names.
    public static final BenchMark mooTools = evaluate(() -> {
        Options options = new Options();
        options.createInstances = true;
        options.createInstancesClassFilter = true;
        return new BenchMark("MooTools", "tests/mootools/mootools.js", null, options, ES5);
    });

    // TODO: Way way way to much is a class.
    public static final BenchMark prototype = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("Prototype", "tests/prototype/prototype.js", null, options, ES5);
    });

    // Interresting altough useless benchmark, uses RequireJS to get the library. Therefore my analysis cannot find anything on the heap it can output (it is only in an environment).
    public static final BenchMark ace = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("ace.js", "tests/ace/ace.js", null, options, ES5, Arrays.asList(Dependency.requireJS));
    });

    public static final BenchMark require = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("requireJS", "tests/requireJS/require.js", null, options, ES5);
    });

    public static final BenchMark handlebars = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("handlebars", "tests/handlebars/handlebars-v4.0.5.js", null, options, ES5);
    });



    public static final BenchMark test = evaluate(() -> {
        Options options = new Options();

        Dependency testDependency = new Dependency("tests/test/dependency.js", "tests/test/dependency.d.ts");

        return new BenchMark("Test file", "tests/test/test.js", null, options, ES5/*, Arrays.asList(testDependency, Dependency.jQuery)*/);
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
        private static final Dependency underscore = new Dependency("tests/underscore/underscore.js", "tests/underscore/underscore.d.ts");
        private static final Dependency requireJS = new Dependency("tests/requireJS/require.js", "tests/requireJS/require.d.ts");
    }
}