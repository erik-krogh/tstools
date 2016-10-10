package dk.webbies.tscreate.main;

import dk.webbies.tscreate.BenchMark;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.util.Pair;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.BenchMark.*;
import static dk.webbies.tscreate.Options.*;
import static dk.webbies.tscreate.Options.StaticAnalysisMethod.*;
import static dk.webbies.tscreate.main.CompareMethods.*;

/**
 * Created by erik1 on 23-08-2016.
 */
@SuppressWarnings("Duplicates")
public class PerformanceTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        Config none = disableHeavy(NONE, "none");
        Config noneHeavy = withHeavy(NONE, "none+");
        Config tsinfer = disableHeavy(UPPER_LOWER, "tsinfer");
        Config tsinferHeavy = withHeavy(UPPER_LOWER, "tsinfer+");
        Config tscheck = disableHeavy(UNIFICATION_CONTEXT_SENSITIVE, "tscheck");
        Config tscheckHeavy = withHeavy(UNIFICATION_CONTEXT_SENSITIVE, "tscheck+");
//        List<CompareMethods.Config> configs = Arrays.asList(noneHeavy, none, tsinfer, noneHeavy, tsinferHeavy, none, tscheck, noneHeavy, tscheckHeavy);
        List<CompareMethods.Config> configs = Arrays.asList(noneHeavy, none, tsinfer, tsinferHeavy, tscheck, tscheckHeavy);
//        List<CompareMethods.Config> configs = Arrays.asList(tsinfer, tsinferHeavy);
//        List<CompareMethods.Config> configs = Arrays.asList(none, noneHeavy);
//        compareConfigs(Arrays.asList(ace, angular, async201, backbone133, D3, ember27, FabricJS16, hammer, handlebars4, jQuery, knockout, leaflet, moment_214, vue, jasmine24, PIXI_4_0, react15, polymer16, three, underscore18), configs, 30 * 60 * 1000);
        compareConfigs(Arrays.asList(three), configs, 30 * 60 * 1000);
    }

    public static Config disableHeavy(StaticAnalysisMethod method, String prettyString) {
        return new Config(prettyString, (options) -> {
            options.staticMethod = method;
            options.useJSDoc = false;
            options.recordCalls = false;
            options.createInstances = false;
            options.createInstances = false;
            options.classOptions.useInstancesForThis = false;
        });
    }

    public static Config withHeavy(StaticAnalysisMethod method, String prettyString) {
        return new Config(prettyString, (options) -> {
            options.staticMethod = method;
            options.useJSDoc = false;
        });
    }

    public static void compareConfigs(List<BenchMark> benchMarks, Collection<CompareMethods.Config> configs, long timeout) throws IOException, InterruptedException {
        Map<Pair<BenchMark, Config>, Double> results = new LinkedHashMap<>();
        int runs = 0;
        while (runs++ <= 10) {
            for (Config config : configs) {
                for (BenchMark benchMark : benchMarks) {
                    if (config.prettyString.toLowerCase().contains("tscheck")) {
                        System.out.println();
                    }
                    System.out.println("Benchmark: " + benchMark.name + " config: " + config.prettyString);
                    Pair<BenchMark, Config> key = new Pair<>(benchMark, config);

                    if (results.containsKey(key) && results.get(key) == -1.0) {
                        continue;
                    }

                    benchMark.resetOptions();
                    config.applier.accept(benchMark.getOptions());

                    System.out.println("With method: " + config.prettyString);

                    Map<String, DeclarationType> result = Main.createDeclarationWithTimeout(benchMark, timeout);


                    if (result != null) {
                        double time = Main.staticAnalysisTime / 1000.0;
                        System.out.println("Execution of " + benchMark.name + " with config " + config.prettyString + " completed in: " + time + "s");

                        results.put(key, results.containsKey(key) ? Math.min(results.get(key), time) : time);
                    } else {
                        System.out.println("Execution of " + benchMark.name + " with config " + config.prettyString + " crashed!");
                        results.put(key, results.containsKey(key) ? Math.min(results.get(key), -1) : -1);
                    }

                    printResults(results, configs);

                    Thread.sleep(5 * 1000);
                }
            }
        }
    }

    private static void printResults(Map<Pair<BenchMark, Config>, Double> results, Collection<Config> configs) {
        List<Config> distinctConfigs = configs.stream().distinct().collect(Collectors.toList());

        System.out.println("Results: \n");
        for (Map.Entry<Pair<BenchMark, Config>, Double> entry : results.entrySet()) {
            BenchMark benchMark = entry.getKey().getLeft();
            Config config = entry.getKey().getRight();
            Double time = entry.getValue();
            System.out.println(benchMark.name + " - " + config.prettyString + ": " + time);
        }

        System.out.println("Pretty format");

        System.out.print("Name");
        for (Config config : distinctConfigs) {
            System.out.print("\t" + config.prettyString);
        }
        System.out.println();

        results.keySet().stream().map(Pair::getLeft).distinct().forEach(bench -> {
            System.out.print(bench.name);
            for (Config config : distinctConfigs) {
                System.out.print("\t" + results.get(new Pair<>(bench, config)));
            }
            System.out.println();
        });

    }
}
