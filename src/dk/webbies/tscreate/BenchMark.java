package dk.webbies.tscreate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.main.Main.LanguageLevel;
import static dk.webbies.tscreate.main.Main.LanguageLevel.ES5;
import static dk.webbies.tscreate.main.Main.LanguageLevel.ES6;
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
    public final List<Dependency> dependencies = new ArrayList<>();
    public List<String> testFiles = new ArrayList<>();

    public List<String> dependencyScripts() {
        return this.dependencies.stream().map(dependency -> dependency.scriptPath).collect(Collectors.toList());
    }

    public List<String> dependencyDeclarations() {
        return this.dependencies.stream().map(dependency -> dependency.declarationPath).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static void addTestFiles(BenchMark bench, String folderPath) {
        try {
            Files.walk(Paths.get(folderPath))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile).map(File::getPath)
                    .forEach(bench.testFiles::add);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public static final List<BenchMark> allBenchmarks = new ArrayList<>();

    private BenchMark (String name, String scriptPath, String declarationPath, Options options, LanguageLevel languageLevel) {
        BenchMark.allBenchmarks.add(this);
        this.name = name;
        this.scriptPath = scriptPath;
        this.declarationPath = declarationPath;
        this.options = options;
        this.languageLevel = languageLevel;
    }

    public static final BenchMark underscore = evaluate(() -> {
        Options options = new Options();
        options.createInstances = true;
        options.createInstancesClassFilter = true;
        options.asyncTest = false;
        options.recordCalls = true;
        BenchMark bench = new BenchMark("Underscore.js", "tests/underscore/underscore.js", "tests/underscore/underscore.d.ts", options, ES5);

//        bench.dependencies.add(Dependency.QUnit);

        /* bench.testFiles.add("tests/underscore/tests/arrays.js");
        bench.testFiles.add("tests/underscore/tests/chaining.js");
        bench.testFiles.add("tests/underscore/tests/collections.js");
//        bench.testFiles.add("tests/underscore/tests/cross-document.js");
        bench.testFiles.add("tests/underscore/tests/functions.js");
        bench.testFiles.add("tests/underscore/tests/objects.js");
        bench.testFiles.add("tests/underscore/tests/utility.js"); */
        return bench;
    });

    public static final BenchMark PIXI = evaluate(() -> {
        Options options = new Options();
        options.recordCalls = false;
        return new BenchMark("Pixi.js", "tests/pixi/pixi.js", "tests/pixi/pixi.js.d.ts", options, ES5);
    });

    public static final BenchMark FabricJS = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("Fabric.js", "tests/fabric/fabric.js", "tests/fabric/fabricjs.d.ts", options, ES5);
    });

    public static final BenchMark jQuery = evaluate(() -> {
        Options options = new Options();
        options.recordCalls = true;
        options.asyncTest = true;
        BenchMark bench = new BenchMark("jQuery", "tests/jquery/jquery.js", "tests/jquery/jquery.d.ts", options, ES5);

        bench.dependencies.add(Dependency.QUnit);


//        bench.testFiles.add("tests/jquery/tests/ajax.js");
//        bench.testFiles.add("tests/jquery/tests/animation.js");
//        bench.testFiles.add("tests/jquery/tests/attributes.js");
//        bench.testFiles.add("tests/jquery/tests/basic.js");
//        bench.testFiles.add("tests/jquery/tests/callbacks.js");
//        bench.testFiles.add("tests/jquery/tests/core.js");
//        bench.testFiles.add("tests/jquery/tests/css.js");
//        bench.testFiles.add("tests/jquery/tests/data.js");
//        bench.testFiles.add("tests/jquery/tests/deferred.js");
//        bench.testFiles.add("tests/jquery/tests/deprecated.js");
//        bench.testFiles.add("tests/jquery/tests/dimensions.js");
//        bench.testFiles.add("tests/jquery/tests/effects.js");
//        bench.testFiles.add("tests/jquery/tests/event.js");
//        bench.testFiles.add("tests/jquery/tests/exports.js");
//        bench.testFiles.add("tests/jquery/tests/manipulation.js");
//        bench.testFiles.add("tests/jquery/tests/offset.js");
//        bench.testFiles.add("tests/jquery/tests/queue.js");
//        bench.testFiles.add("tests/jquery/tests/ready.js");
//        bench.testFiles.add("tests/jquery/tests/selector.js");
//        bench.testFiles.add("tests/jquery/tests/serialize.js");
//        bench.testFiles.add("tests/jquery/tests/support.js");
//        bench.testFiles.add("tests/jquery/tests/traversing.js");
//        bench.testFiles.add("tests/jquery/tests/tween.js");
//        bench.testFiles.add("tests/jquery/tests/wrap.js");

        return bench;
    });

    public static final BenchMark angular = evaluate(() -> {
        Options options = new Options();
        options.createInstancesClassFilter = true;
        options.recordCalls = false;
        return new BenchMark("AngularJS", "tests/angular/angular.js", "tests/angular/angular.d.ts", options, ES5);
    });

    public static final BenchMark three = evaluate(() -> {
        Options options = new Options();
//        options.debugPrint = true;
        options.maxEvaluationDepth = null;
        options.recordCalls = true;
        options.asyncTest = true;
        BenchMark bench = new BenchMark("three.js", "tests/three/three.js", "tests/three/three.d.ts", options, ES6);
        bench.dependencies.add(Dependency.underscore);
        bench.dependencies.add(Dependency.QUnit);
        bench.dependencies.add(Dependency.QUnit_Utils);

        // addTestFiles(bench, "tests/three/tests/");
        bench.testFiles.add("tests/three/tests/cameras/Camera.js");
        bench.testFiles.add("tests/three/tests/cameras/OrthographicCamera.js");
        bench.testFiles.add("tests/three/tests/cameras/PerspectiveCamera.js");
        bench.testFiles.add("tests/three/tests/core/BufferAttribute.js");
        bench.testFiles.add("tests/three/tests/core/BufferGeometry.js");
        bench.testFiles.add("tests/three/tests/core/Clock.js");
        bench.testFiles.add("tests/three/tests/core/EventDispatcher.js");
        bench.testFiles.add("tests/three/tests/core/Object3D.js");
        bench.testFiles.add("tests/three/tests/extras/geometries/BoxGeometry.tests.js");
        bench.testFiles.add("tests/three/tests/extras/geometries/CircleBufferGeometry.tests.js");
//        bench.testFiles.add("tests/three/tests/extras/geometries/CircleGeometry.tests.js");
//        bench.testFiles.add("tests/three/tests/extras/geometries/CylinderGeometry.tests.js");
//        bench.testFiles.add("tests/three/tests/extras/geometries/DodecahedronGeometry.tests.js");
//        bench.testFiles.add("tests/three/tests/extras/geometries/ExtrudeGeometry.tests.js");
//        bench.testFiles.add("tests/three/tests/extras/geometries/IcosahedronGeometry.tests.js");
//        bench.testFiles.add("tests/three/tests/extras/geometries/LatheGeometry.tests.js");
//        bench.testFiles.add("tests/three/tests/extras/geometries/OctahedronGeometry.tests.js");
//        bench.testFiles.add("tests/three/tests/extras/geometries/ParametricGeometry.tests.js");
//        bench.testFiles.add("tests/three/tests/extras/geometries/PlaneBufferGeometry.tests.js");
//        bench.testFiles.add("tests/three/tests/extras/geometries/PlaneGeometry.tests.js");
//        bench.testFiles.add("tests/three/tests/extras/geometries/RingGeometry.tests.js");
//        bench.testFiles.add("tests/three/tests/extras/geometries/SphereBufferGeometry.tests.js");
//        bench.testFiles.add("tests/three/tests/extras/geometries/SphereGeometry.tests.js");
//        bench.testFiles.add("tests/three/tests/extras/geometries/TetrahedronGeometry.tests.js");
//        bench.testFiles.add("tests/three/tests/extras/geometries/TorusGeometry.tests.js");
//        bench.testFiles.add("tests/three/tests/extras/geometries/TorusKnotGeometry.tests.js");
//        bench.testFiles.add("tests/three/tests/extras/ImageUtils.test.js");
        bench.testFiles.add("tests/three/tests/geometry/EdgesGeometry.js");
        bench.testFiles.add("tests/three/tests/lights/AmbientLight.tests.js");
        bench.testFiles.add("tests/three/tests/lights/DirectionalLight.tests.js");
        bench.testFiles.add("tests/three/tests/lights/HemisphereLight.tests.js");
        bench.testFiles.add("tests/three/tests/lights/PointLight.tests.js");
        bench.testFiles.add("tests/three/tests/lights/SpotLight.tests.js");
        bench.testFiles.add("tests/three/tests/math/Box2.js");
        bench.testFiles.add("tests/three/tests/math/Box3.js");
        bench.testFiles.add("tests/three/tests/math/Color.js");
//        bench.testFiles.add("tests/three/tests/math/Constants.js");
        bench.testFiles.add("tests/three/tests/math/Euler.js");
        bench.testFiles.add("tests/three/tests/math/Frustum.js");
        bench.testFiles.add("tests/three/tests/math/Line3.js");
        bench.testFiles.add("tests/three/tests/math/Math.js");
        bench.testFiles.add("tests/three/tests/math/Matrix3.js");
        bench.testFiles.add("tests/three/tests/math/Matrix4.js");
        bench.testFiles.add("tests/three/tests/math/Plane.js");
        bench.testFiles.add("tests/three/tests/math/Quaternion.js");
        bench.testFiles.add("tests/three/tests/math/Ray.js");
        bench.testFiles.add("tests/three/tests/math/Sphere.js");
        bench.testFiles.add("tests/three/tests/math/Triangle.js");
        bench.testFiles.add("tests/three/tests/math/Vector2.js");
        bench.testFiles.add("tests/three/tests/math/Vector3.js");
        bench.testFiles.add("tests/three/tests/math/Vector4.js");
        return bench;
    });

    // Kind of a useless benchmark, since the hand-written .d.ts file says that it exposes 0 global variables. (But it does, there is the Sugar object).
    public static final BenchMark sugar = evaluate(() -> {
        Options options = new Options();
        options.recordCalls = false;
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
        options.createInstancesClassFilter = true;
        return new BenchMark("Knockout", "tests/knockout/knockout.js", "tests/knockout/knockout.d.ts", options, ES5);
    });

    // The stress test to rule all stress-tests. 10MB of JavaScript, 220000 lines of code.
    // Node goes out of memory when ts-spec-reader attempts to parse the declaration file.
    /*public static final BenchMark ExtJS = evaluate(() -> {
        Options options = new Options();
        // All of these are disabled, because the program is so big, and it is needed, otherwise stuff runs out of memory/time.
        options.createInstances = false;
        options.classOptions.useClassInstancesFromHeap = false;
        options.classOptions.useInstancesForThis = false;
        options.recordCalls = false;
        return new BenchMark("Ext JS", "tests/extjs/ext.js", "tests/extjs/ext.d.ts", options, ES5);
    });*/

    /*public static final BenchMark ember = evaluate(() -> { // Somehow the JSnap crashes on this thing now. // TODO: Check out why JSNAP crash on ember.
        Options options = new Options();
        options.createInstances = false;
        options.recordCalls = false;
        options.debugPrint = true;
        BenchMark bench = new BenchMark("Ember.js", "tests/ember/ember.js", "tests/ember/ember.d.ts", options, ES5);
        bench.dependencies.add(Dependency.jQuery);
        return bench;
    });*/

    public static final BenchMark backbone = evaluate(() -> {
        Options options = new Options();
        BenchMark bench = new BenchMark("Backbone.js", "tests/backbone/backbone.js", "tests/backbone/backbone.d.ts", options, ES5);
        bench.dependencies.add(Dependency.underscore);
        bench.dependencies.add(Dependency.jQuery);
        return bench;
    });

    public static final BenchMark materialize = evaluate(() -> {
        Options options = new Options();
        BenchMark bench = new BenchMark("MaterializeCSS", "tests/materialize/materialize.js", "tests/materialize/materialize.d.ts", options, ES5);
        bench.dependencies.add(Dependency.jQuery);
        bench.dependencies.add(Dependency.hammer);
        bench.dependencies.add(Dependency.pickdate);
        return bench;
    });

    public static final BenchMark mooTools = evaluate(() -> {
        Options options = new Options();
        options.createInstancesClassFilter = true; // Infinite loop otherwise.
        return new BenchMark("MooTools", "tests/mootools/mootools.js", null, options, ES5); // I couldn't actually find a declaration file, posting one could be an option. (When i get rid of the duplicates).
    });

    public static final BenchMark prototype = evaluate(() -> {
        Options options = new Options();
        options.createInstances = false; // Otherwise, things goes to shit.
        return new BenchMark("Prototype", "tests/prototype/prototype.js", null, options, ES5); // TODO: Does a declaration file exist? Can it?
    });

    public static final BenchMark ace = evaluate(() -> {
        Options options = new Options();
        options.createInstancesClassFilter = true; // Otherwise prombt appears
        return new BenchMark("Ace", "tests/ace/ace.js", "tests/ace/ace.d.ts", options, ES5);
    });

    public static final BenchMark require = evaluate(() -> {
        Options options = new Options();
        options.createInstances = false;
        options.recordCalls = false;
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

    public static final BenchMark Q = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("Q", "tests/q/q.js", "tests/q/q.d.ts", options, ES5);
    });

    public static final BenchMark moment = evaluate(() -> {
        Options options = new Options();
        return new BenchMark("Moment.js", "tests/moment/moment.js", "tests/moment/moment.d.ts", options, ES5);
    });

    public static final BenchMark hammer = evaluate(() -> {
        Options options = new Options();
        BenchMark bench = new BenchMark("Hammer.js", "tests/hammer/hammer.js", "tests/hammer/hammer.d.ts", options, ES5);
        bench.testFiles.add("tests/hammer/myTest.js"); // Kind of cheating.
        return bench;
    });

    public static final BenchMark please = evaluate(() -> {
        Options options = new Options();
        BenchMark benchMark = new BenchMark("Please.js", "tests/please/please.js", "tests/please/please.d.ts", options, ES5);
        return benchMark;
    });

    public static final BenchMark path = evaluate(() -> {
        Options options = new Options();
        BenchMark benchMark = new BenchMark("path.js", "tests/path/path.js", "tests/path/path.d.ts", options, ES5);
        return benchMark;
    });

    public static final BenchMark p2 = evaluate(() -> {
        Options options = new Options();
        BenchMark benchMark = new BenchMark("p2.js", "tests/p2/p2.js", "tests/p2/p2.d.ts", options, ES5);
        return benchMark;
    });

    public static final BenchMark mathjax = evaluate(() -> {
        Options options = new Options();
        options.recordCalls = false;
        BenchMark benchMark = new BenchMark("MathJax.js", "tests/mathjax/mathjax.js", "tests/mathjax/mathjax.d.ts", options, ES5);
        return benchMark;
    });

    public static final BenchMark photoswipe = evaluate(() -> {
        Options options = new Options();
        BenchMark benchMark = new BenchMark("Photoswipe", "tests/photoswipe/photoswipe.js", "tests/photoswipe/photoswipe.d.ts", options, ES5);
        return benchMark;
    });

    public static final BenchMark peer = evaluate(() -> {
        Options options = new Options();
        BenchMark benchMark = new BenchMark("Peer.js", "tests/peerjs/peer.js", "tests/peerjs/peer.d.ts", options, ES5);
        return benchMark;
    });

    public static final BenchMark async = evaluate(() -> {
        Options options = new Options();
        BenchMark benchMark = new BenchMark("async", "tests/async/async.js", "tests/async/async.d.ts", options, ES5);
        return benchMark;
    });



    // Some i have tried to infer types for.

    public final static List<BenchMark> toInferFor = new ArrayList<>();
    static {
        File inferFor = new File("tests/toInferFor");
        for (File file : inferFor.listFiles()) {
            String path = file.getPath();
            if (path.endsWith(".js")) {
                Options options = new Options();
                options.createInstances = false;
                options.recordCalls = false;
                toInferFor.add(new BenchMark(path, path, null, options, ES6));
            }
        }
    }


    public static final BenchMark test = evaluate(() -> {
        Options options = new Options();
//        options.debugPrint = true;
        options.recordCalls = true;
        options.createInstances = true;

        BenchMark bench = new BenchMark("Test file", "tests/test/test.js", "tests/test/test.d.ts", options, ES5);
        bench.dependencies.add(Dependency.underscore);
        return bench;
    });


    public static final class Dependency {
        public final String scriptPath;
        public final String declarationPath;
        public Dependency(String scriptPath, String declarationPath) {
            assert scriptPath != null;
            this.scriptPath = scriptPath;
            this.declarationPath = declarationPath;
        }

        private static Dependency fromBench(BenchMark benchMark) {
            return new Dependency(benchMark.scriptPath, benchMark.declarationPath);
        }

        private static final Dependency QUnit = new Dependency("tests/qunit/qunit.js", "tests/qunit/qunit.d.ts");
        private static final Dependency QUnit_Utils = new Dependency("tests/qunit/qunit-utils.js", null); // Also requires underscore
        private static final Dependency jQuery = new Dependency("tests/jquery/jquery.js", "tests/jquery/jquery.d.ts");
        private static final Dependency underscore = new Dependency("tests/underscore/underscore.js", "tests/underscore/underscore.d.ts");
        private static final Dependency requireJS = new Dependency("tests/requireJS/require.js", "tests/requireJS/require.d.ts");
        private static final Dependency hammer = new Dependency("tests/hammer/hammer.js", "tests/hammer/hammer.d.ts");
        private static final Dependency pickdate = new Dependency("tests/pickdate/picker.js", "tests/pickdate/picker.d.ts");
    }
}