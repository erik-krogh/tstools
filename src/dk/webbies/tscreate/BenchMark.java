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
@SuppressWarnings("unused")
public class BenchMark {
    public final String name;
    public final String scriptPath;
    public String declarationPath;
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
        return new BenchMark("Underscore.js", "tests/underscore/underscore.js", "tests/underscore/underscore.d.ts", options, ES5);
    });

    // FIXME: Try to make an issue on pixi-typescript about the declarations that aren't on the object, make them into interfaces instead.
    public static final BenchMark PIXI = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("Pixi.js", "tests/pixi/pixi.js", "tests/pixi/pixi.js.d.ts", options, ES5);
    });

    public static final BenchMark FabricJS = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("Fabric.js", "tests/fabric/fabric.js", "tests/fabric/fabricjs.d.ts", options, ES5);
    });

    public static final BenchMark jQuery = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("jQuery", "tests/jquery/jquery.js", "tests/jquery/jquery.d.ts", options, ES5);
    });

    public static final BenchMark angular = evaluate(() -> {
        Options options = new Options();
        options.createInstancesClassFilter = true;
        return new BenchMark("AngularJS", "tests/angular/angular.js", "tests/angular/angular.d.ts", options, ES5);
    });

    public static final BenchMark three = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("three.js", "tests/three/three.js", "tests/three/three.d.ts", options, ES6);
    });

    // Kind of a useless benchmark, since the hand-written .d.ts file says that it exposes 0 global variables. (But it does, there is the Sugar object).
    public static final BenchMark sugar = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("Sugar", "tests/sugar/sugar.js", "tests/sugar/sugar.d.ts", options, ES5);
    });

    public static final BenchMark leaflet = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("Leaflet", "tests/leaflet/leaflet.js", "tests/leaflet/leaflet.d.ts", options, ES5);
    });

    public static final BenchMark D3 = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("D3.js", "tests/d3/d3.js", "tests/d3/d3.d.ts", options, ES5);
    });

    public static final BenchMark react = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("React", "tests/react/react.js", "tests/react/react.d.ts", options, ES5);
    });

    public static final BenchMark knockout = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("Knockout", "tests/knockout/knockout.js", "tests/knockout/knockout.d.ts", options, ES5);
    });

    // The stress test to rule all stress-tests. 10MB of JavaScript, 220000 lines of code.
    public static final BenchMark ExtJS = evaluate(() -> {
        Options options = new Options();
        // All of these are disabled, because the program is so big, and it is needed, otherwise stuff runs out of memory/time.
        options.createInstances = false;
        options.classOptions.useClassInstancesFromHeap = false;
        options.classOptions.useInstancesForThis = false;
        return new BenchMark("Ext JS", "tests/extjs/ext.js", "tests/extjs/ext.d.ts", options, ES5);
    });

    public static final BenchMark ember = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("Ember.js", "tests/ember/ember.js", "tests/ember/ember.d.ts", options, ES5, Arrays.asList(Dependency.jQuery));
    });

    public static final BenchMark backbone = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("Backbone.js", "tests/backbone/backbone.js", "tests/backbone/backbone.d.ts", options, ES5, Arrays.asList(Dependency.underscore, Dependency.jQuery));
    });

    public static final BenchMark materialize = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("MaterializeCSS", "tests/materialize/materialize.js", null, options, ES5, Arrays.asList(Dependency.jQuery));
    });

    public static final BenchMark mooTools = evaluate(() -> {
        Options options = new Options();
        options.createInstancesClassFilter = true; // Infinite loop otherwise.
        return new BenchMark("MooTools", "tests/mootools/mootools.js", null, options, ES5); // I couldn't actually find a declaration file, posting one could be an option. (When i get rid of the duplicates).
    });

    public static final BenchMark prototype = evaluate(() -> {
        Options options = new Options();
        options.createInstancesClassFilter = true; // Needed, otherwise instances of functions end up in the wrong places, causing everything to become a method.
        return new BenchMark("Prototype", "tests/prototype/prototype.js", null, options, ES5); // TODO: Does a declaration file exist? Can it?
    });

    public static final BenchMark ace = evaluate(() -> {
        Options options = new Options();
        options.createInstancesClassFilter = true; // Otherwise prombt appears
        return new BenchMark("Ace", "tests/ace/ace.js", "tests/ace/ace.d.ts", options, ES5);
    });

    public static final BenchMark require = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("RequireJS", "tests/requireJS/require.js", "tests/requireJS/require.d.ts", options, ES5);
    });

    public static final BenchMark handlebars = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("Handlebars.js", "tests/handlebars/handlebars-v4.0.5.js", "tests/handlebars/handlebars.d.ts", options, ES5);
    });

    public static final BenchMark box2d = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("Box2dWeb", "tests/box2dweb/box2dweb.js", "tests/box2dweb/box2dweb.d.ts", options, ES5);
    });

    // TODO: Evaluation is basically 0.
    public static final BenchMark Q = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("Q", "tests/q/q.js", "tests/q/q.d.ts", options, ES5);
    });

    public static final BenchMark moment = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("Moment.js", "tests/moment/moment.js", "tests/moment/moment.d.ts", options, ES5);
    });



    public static final BenchMark test = evaluate(() -> {
        Options options = new Options();
        options.debugPrint = true;

        return new BenchMark("Test file", "tests/test/test.js", "tests/test/test.d.ts", options, ES5, Arrays.asList());
    });

    // SSA tests
    public static final BenchMark MPL = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("mpl", "tests/mpl/mpl.js", null, options, ES5);
    });

    public static final BenchMark if0 = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("mpl", "tests/ssa/if0.js", null, options, ES5);
    });

    public static final BenchMark ifNested = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("mpl", "tests/ssa/if_nested.js", null, options, ES5);
    });

    public static final BenchMark funcs = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("mpl", "tests/ssa/funcs.js", null, options, ES5);
    });

    public static final BenchMark fa = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("mpl", "tests/ssa/fa.js", null, options, ES5);
    });


    public static final BenchMark If = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("mpl", "tests/ssa/if.js", null, options, ES5);
    });

    public static final BenchMark if01 = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("mpl", "tests/ssa/if01.js", null, options, ES5);
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