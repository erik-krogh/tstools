package dk.webbies.tscreate.main;

import com.google.common.collect.ArrayListMultimap;
import dk.webbies.tscreate.BenchMark;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.Score;
import dk.webbies.tscreate.util.Pair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
        blackList.add(new Pair<>(BenchMark.jQuery, Options.StaticAnalysisMethod.UPPER_CONTEXT_SENSITIVE));
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
        blackList.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.UPPER_CONTEXT_SENSITIVE));
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
        blackList.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.COMBINED));
        blackList.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.UPPER_LOWER));
        blackList.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.UPPER));
        blackList.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.MIXED));
        blackList.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.UNIFICATION));

        blackList.add(new Pair<>(BenchMark.box2d, Options.StaticAnalysisMethod.UNIFICATION));
        blackList.add(new Pair<>(BenchMark.box2d, Options.StaticAnalysisMethod.UNIFICATION_CONTEXT_SENSITIVE));
    }

    public static void compareMethods(List<BenchMark> benchMarks, Collection<Options.StaticAnalysisMethod> methods, long timeout) throws IOException {
        compareMethods(benchMarks, methods, timeout, false);
    }

    public static void compareMethods(List<BenchMark> benchMarks, Collection<Options.StaticAnalysisMethod> methods, long timeout, boolean skipGeneratingDeclarations) throws IOException {
        Map<BenchMark, Map<Options.StaticAnalysisMethod, Score>> benchmarkScores = new HashMap<>();
        int maxMethodLength = methods.stream().map(method -> method.prettyString.length()).max(Integer::compare).get();
        int decimals = 4;
        for (BenchMark benchMark : benchMarks) {
            if (benchMark.declarationPath == null) {
                continue;
            }
            Map<Options.StaticAnalysisMethod, Score> scores = new HashMap<>();
            benchmarkScores.put(benchMark, scores);
            for (Options.StaticAnalysisMethod method : methods) {
                if (blackList.contains(new Pair<>(benchMark, method)) || (benchMark.options.combineInterfacesAfterAnalysis && blacklistWhenCombining.contains(new Pair<>(benchMark, method)))) {
                    scores.put(method, new Score(-1, -1, -1));
                    continue;
                }
                benchMark.options.staticMethod = method;
                println("With method: " + method.prettyString);
                Score score = null;
                if (skipGeneratingDeclarations) {
                    score = Main.runEvaluation(benchMark);
                } else {
                    score = Main.runAnalysisWithTimeout(benchMark, timeout);
                }
                if (score == null) {
                    score = new Score(-1, -1, -1);
                }
                scores.put(method, score);
            }

            printMethods(benchmarkScores, maxMethodLength, decimals);
        }

        printResultingEvaluations(methods, benchmarkScores, maxMethodLength, decimals);
    }

    private static void printResultingEvaluations(Collection<Options.StaticAnalysisMethod> methods, Map<BenchMark, Map<Options.StaticAnalysisMethod, Score>> benchmarkScores, int maxMethodLength, int decimals) {
        print("\n\n\n\n\n");

        printMethods(benchmarkScores, maxMethodLength, decimals);

        ArrayListMultimap<Options.StaticAnalysisMethod, Score> scores = ArrayListMultimap.create();
        for (Map<Options.StaticAnalysisMethod, Score> map : benchmarkScores.values()) {
            map.entrySet().stream().forEach(entry -> scores.put(entry.getKey(), entry.getValue()));
        }

        Map<Options.StaticAnalysisMethod, Score> sumScores = new LinkedHashMap<>();

        for (Map.Entry<Options.StaticAnalysisMethod, Collection<Score>> entry : scores.asMap().entrySet()) {
            Options.StaticAnalysisMethod method = entry.getKey();
            Score score = entry.getValue().stream().reduce(new Score(0, 0, 0), (a, b) -> new Score(a.fMeasure + b.fMeasure, a.precision + b.precision, a.recall + b.recall));
            sumScores.put(method, score);
        }

        print("\n\n\n\n\n");

        println("The methods summed up: ");
        // Ugly, but to keep it sorted.
        Set<Map.Entry<Options.StaticAnalysisMethod, Score>> sumScoresClone = new HashMap<>(sumScores).entrySet();
        sumScores.clear();
        sumScoresClone.stream().sorted((a, b) -> Double.compare(a.getValue().fMeasure, b.getValue().fMeasure)).forEach(entry -> sumScores.put(entry.getKey(), entry.getValue()));
        printScores(sumScores, maxMethodLength, decimals);


        Map<Options.StaticAnalysisMethod, Integer> methodCounts = new HashMap<>();
        methods.forEach(method -> methodCounts.put(method, 0));

        for (Map.Entry<BenchMark, Map<Options.StaticAnalysisMethod, Score>> entry : benchmarkScores.entrySet()) {
            AtomicInteger counter = new AtomicInteger(0);
            entry.getValue().entrySet().stream().sorted((a, b) -> Double.compare(b.getValue().fMeasure, a.getValue().fMeasure)).forEach(scoreEntry -> {
                Options.StaticAnalysisMethod method = scoreEntry.getKey();
                methodCounts.put(method, methodCounts.get(method) + counter.incrementAndGet());
            });
        }

        print("\n\n\n\n\n");
        println("\"Scores\" for the different analysis");

        methodCounts.entrySet().stream().sorted((a, b) -> Double.compare(b.getValue(), a.getValue())).forEach(entry -> {
            println(entry.getKey().prettyString + "; " + entry.getValue());
        });
    }

    private static void printMethods(Map<BenchMark, Map<Options.StaticAnalysisMethod, Score>> benchmarkScores, int maxMethodLength, int decimals) {
        for (Map.Entry<BenchMark, Map<Options.StaticAnalysisMethod, Score>> entry : benchmarkScores.entrySet()) {
            Map<Options.StaticAnalysisMethod, Score> dupScores = entry.getValue();
            println("Benchmark: " + entry.getKey().name);
            printScores(dupScores, maxMethodLength, decimals);
        }
    }

    private static void printScores(Map<Options.StaticAnalysisMethod, Score> scores, int maxMethodLength, int decimals) {
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
