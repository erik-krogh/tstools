package dk.webbies.tscreate.main;

import com.google.common.collect.ArrayListMultimap;
import dk.webbies.tscreate.BenchMark;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.Score;
import dk.webbies.tscreate.util.Pair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.util.Util.toFixed;

/**
 * Created by Erik Krogh Kristensen on 22-02-2016.
 */
public class CompareMethods {
    private static Set<Pair<BenchMark, Options.StaticAnalysisMethod>> blackList = new HashSet<>();
    static {
        blackList.add(new Pair<>(BenchMark.jQuery, Options.StaticAnalysisMethod.UNIFICATION));
        blackList.add(new Pair<>(BenchMark.jQuery, Options.StaticAnalysisMethod.UNIFICATION_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.jQuery, Options.StaticAnalysisMethod.COMBINED_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.jQuery, Options.StaticAnalysisMethod.MIXED_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.jQuery, Options.StaticAnalysisMethod.ANDERSON_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.jQuery, Options.StaticAnalysisMethod.LOWER_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.jQuery, Options.StaticAnalysisMethod.UPPER_LOWER_CONTEXT_SENSITIVE));

        blackList.add(new Pair<>(BenchMark.PIXI, Options.StaticAnalysisMethod.UNIFICATION));
        blackList.add(new Pair<>(BenchMark.PIXI, Options.StaticAnalysisMethod.OLD_UNIFICATION));
        blackList.add(new Pair<>(BenchMark.PIXI, Options.StaticAnalysisMethod.UNIFICATION_CONTEXT_SENSITIVE));

        blackList.add(new Pair<>(BenchMark.FabricJS, Options.StaticAnalysisMethod.UNIFICATION_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.FabricJS, Options.StaticAnalysisMethod.OLD_UNIFICATION_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.FabricJS, Options.StaticAnalysisMethod.UNIFICATION));

        blackList.add(new Pair<>(BenchMark.angular, Options.StaticAnalysisMethod.OLD_UNIFICATION));

        blackList.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.UNIFICATION_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.OLD_UNIFICATION));
        blackList.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.UNIFICATION));
        blackList.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.COMBINED_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.ANDERSON_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.MIXED_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.LOWER_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.UPPER_LOWER_CONTEXT_SENSITIVE));

        blackList.add(new Pair<>(BenchMark.leaflet, Options.StaticAnalysisMethod.UNIFICATION_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.leaflet, Options.StaticAnalysisMethod.UNIFICATION));

        blackList.add(new Pair<>(BenchMark.react, Options.StaticAnalysisMethod.UNIFICATION_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.react, Options.StaticAnalysisMethod.UNIFICATION));

        /*blackList.add(new Pair<>(BenchMark.ember, Options.StaticAnalysisMethod.OLD_UNIFICATION_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.ember, Options.StaticAnalysisMethod.OLD_UNIFICATION));
        blackList.add(new Pair<>(BenchMark.ember, Options.StaticAnalysisMethod.UNIFICATION_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.ember, Options.StaticAnalysisMethod.UNIFICATION));
        blackList.add(new Pair<>(BenchMark.ember, Options.StaticAnalysisMethod.MIXED_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.ember, Options.StaticAnalysisMethod.ANDERSON_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.ember, Options.StaticAnalysisMethod.COMBINED_CONTEXT_SENSITIVE));*/

        blackList.add(new Pair<>(BenchMark.angular, Options.StaticAnalysisMethod.OLD_UNIFICATION));

        blackList.add(new Pair<>(BenchMark.D3, Options.StaticAnalysisMethod.UNIFICATION_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.D3, Options.StaticAnalysisMethod.UNIFICATION));
        blackList.add(new Pair<>(BenchMark.D3, Options.StaticAnalysisMethod.COMBINED_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.D3, Options.StaticAnalysisMethod.UPPER_LOWER_CONTEXT_SENSITIVE));

        blackList.add(new Pair<>(BenchMark.PIXI, Options.StaticAnalysisMethod.UNIFICATION_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.PIXI, Options.StaticAnalysisMethod.OLD_UNIFICATION));
        blackList.add(new Pair<>(BenchMark.PIXI, Options.StaticAnalysisMethod.UNIFICATION));

        blackList.add(new Pair<>(BenchMark.ace, Options.StaticAnalysisMethod.UNIFICATION_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.ace, Options.StaticAnalysisMethod.OLD_UNIFICATION));
        blackList.add(new Pair<>(BenchMark.ace, Options.StaticAnalysisMethod.UNIFICATION));

        blackList.add(new Pair<>(BenchMark.leaflet, Options.StaticAnalysisMethod.UNIFICATION_CONTEXT_SENSITIVE));
        blackList.add(new Pair<>(BenchMark.leaflet, Options.StaticAnalysisMethod.UNIFICATION));
    }

    private static Set<Pair<BenchMark, Options.StaticAnalysisMethod>> blacklistWhenCombining = new HashSet<>();
    static {
        blacklistWhenCombining.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.COMBINED));
        blacklistWhenCombining.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.UPPER_LOWER));
        blacklistWhenCombining.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.UPPER));
        blacklistWhenCombining.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.MIXED));
        blacklistWhenCombining.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.UNIFICATION));

        blacklistWhenCombining.add(new Pair<>(BenchMark.box2d, Options.StaticAnalysisMethod.UNIFICATION));
        blacklistWhenCombining.add(new Pair<>(BenchMark.box2d, Options.StaticAnalysisMethod.UNIFICATION_CONTEXT_SENSITIVE));
    }

    public static final class Config {
        final Consumer<Options> applier;
        final String prettyString;

        public Config(String prettyString, Consumer<Options> applier) {
            this.applier = applier;
            this.prettyString = prettyString;
        }
    }

    public static void compareMethods(List<BenchMark> benchMarks, Collection<Options.StaticAnalysisMethod> methods, long timeout) throws IOException {
        compareConfigs(benchMarks, methods.stream().map(method -> new Config(method.prettyString, (options) -> options.staticMethod = method)).collect(Collectors.toList()), timeout);
    }

    public static void compareConfigs(List<BenchMark> benchMarks, Collection<Config> configs, long timeout) throws IOException {
        Map<BenchMark, Map<Config, Score>> benchmarkScores = new HashMap<>();
        int maxMethodLength = configs.stream().map(method -> method.prettyString.length()).max(Integer::compare).get();
        int decimals = 4;
        for (BenchMark benchMark : benchMarks) {
            if (benchMark.declarationPath == null) {
                continue;
            }
            Map<Config, Score> scores = new HashMap<>();
            benchmarkScores.put(benchMark, scores);
            for (Config config : configs) {
                benchMark.resetOptions();
                config.applier.accept(benchMark.getOptions());
                if (blackList.contains(new Pair<>(benchMark, benchMark.getOptions().staticMethod)) || (benchMark.getOptions().combineInterfacesAfterAnalysis && blacklistWhenCombining.contains(new Pair<>(benchMark, benchMark.getOptions().staticMethod)))) {
                    scores.put(config, new Score(-1, -1, -1));
                    continue;
                }

                println("With method: " + config.prettyString);
                Score score = Main.runEvaluation(benchMark);
                if (score.precision == -1) {
                    score = Main.runAnalysisWithTimeout(benchMark, timeout);
                }
                if (score == null) {
                    score = new Score(-1, -1, -1);
                }
                scores.put(config, score);
            }

            printMethods(benchmarkScores, maxMethodLength, decimals);
        }

        printResultingEvaluations(configs, benchmarkScores, maxMethodLength, decimals);
    }

    private static void printResultingEvaluations(Collection<Config> methods, Map<BenchMark, Map<Config, Score>> benchmarkScores, int maxMethodLength, int decimals) {
        benchmarkScores = benchmarkScores.entrySet().stream().filter(scores -> {
            return !scores.getValue().values().stream().anyMatch(score -> score.precision == -1);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        print("\n\n\n\n\n");

        printMethods(benchmarkScores, maxMethodLength, decimals);

        ArrayListMultimap<Config, Score> scores = ArrayListMultimap.create();
        for (Map<Config, Score> map : benchmarkScores.values()) {
            map.entrySet().stream().forEach(entry -> scores.put(entry.getKey(), entry.getValue()));
        }

        Map<Config, Score> sumScores = new LinkedHashMap<>();

        for (Map.Entry<Config, Collection<Score>> entry : scores.asMap().entrySet()) {
            Config method = entry.getKey();
            Score score = entry.getValue().stream().reduce(new Score(0, 0, 0), (a, b) -> new Score(a.fMeasure + b.fMeasure, a.precision + b.precision, a.recall + b.recall));
            sumScores.put(method, score);
        }

        print("\n\n\n\n\n");

        println("The methods summed up: ");
        // Ugly, but to keep it sorted.
        Set<Map.Entry<Config, Score>> sumScoresClone = new HashMap<>(sumScores).entrySet();
        sumScores.clear();
        sumScoresClone.stream().sorted((a, b) -> Double.compare(a.getValue().fMeasure, b.getValue().fMeasure)).forEach(entry -> sumScores.put(entry.getKey(), entry.getValue()));
        printScores(sumScores, maxMethodLength, decimals);


        Map<Config, Integer> methodCounts = new HashMap<>();
        methods.forEach(method -> methodCounts.put(method, 0));

        for (Map.Entry<BenchMark, Map<Config, Score>> entry : benchmarkScores.entrySet()) {
            AtomicInteger counter = new AtomicInteger(0);

            List<Map.Entry<Config, Score>> sortedScores = entry.getValue().entrySet().stream().sorted((a, b) -> Double.compare(b.getValue().fMeasure, a.getValue().fMeasure)).collect(Collectors.toList());

            double prevFMeasure = Double.NaN;
            for (Map.Entry<Config, Score> scoreEntry : sortedScores) {
                Config method = scoreEntry.getKey();
                double currentFMeasure = scoreEntry.getValue().fMeasure;
                int count;
                if (!Double.isNaN(prevFMeasure) && prevFMeasure == currentFMeasure) {
                    count = counter.get();
                } else {
                    count = counter.incrementAndGet();
                }
                prevFMeasure = currentFMeasure;
                methodCounts.put(method, methodCounts.get(method) + count);
            }
        }

        print("\n\n\n\n\n");
        println("\"Scores\" for the different analysis, " + benchmarkScores.size() + " benchmarks");

        methodCounts.entrySet().stream().sorted((a, b) -> Double.compare(b.getValue(), a.getValue())).forEach(entry -> {
            String prettyString = entry.getKey().prettyString;
            while (prettyString.length() < maxMethodLength) {
                prettyString = prettyString + " ";
            }
            println(prettyString + ": " + entry.getValue());
        });
    }

    private static void printMethods(Map<BenchMark, Map<Config, Score>> benchmarkScores, int maxMethodLength, int decimals) {
        for (Map.Entry<BenchMark, Map<Config, Score>> entry : benchmarkScores.entrySet()) {
            Map<Config, Score> dupScores = entry.getValue();
            println("Benchmark: " + entry.getKey().name);
            printScores(dupScores, maxMethodLength, decimals);
        }
    }

    private static void printScores(Map<Config, Score> scores, int maxMethodLength, int decimals) {
        scores.entrySet().stream().sorted((o1, o2) -> Double.compare(o2.getValue().fMeasure, o1.getValue().fMeasure)).forEach(scoreEntry -> {
            Score score = scoreEntry.getValue();
            String prettyString = scoreEntry.getKey().prettyString;
            while (prettyString.length() < maxMethodLength) {
                prettyString = prettyString + " ";
            }
            println(prettyString + " : " + toFixed(score.fMeasure, decimals) + " - " + toFixed(score.recall, decimals) + " - " + toFixed(score.precision, decimals));
        });
    }

    /*static File file = new File("methods 5 depth.txt");
    static BufferedWriter writer;
    static {
        try {
            writer = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
    */
    private static void print(String str) {
        try {
//            writer.write(str);
//            writer.flush();
            System.out.print(str);
            System.out.flush();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private static void println(String string) {
        print(string + "\n");
    }
}
