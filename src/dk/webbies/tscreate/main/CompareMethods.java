package dk.webbies.tscreate.main;

import dk.webbies.tscreate.BenchMark;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.Score;
import dk.webbies.tscreate.util.Pair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static dk.webbies.tscreate.util.Util.toFixed;

/**
 * Created by Erik Krogh Kristensen on 22-02-2016.
 */
public class CompareMethods {
    private static Set<Pair<BenchMark, Options.StaticAnalysisMethod>> blackList = new HashSet<>();
    static {
        blackList.add(new Pair<>(BenchMark.jQuery, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));

        blackList.add(new Pair<>(BenchMark.PIXI, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));
        blackList.add(new Pair<>(BenchMark.PIXI, Options.StaticAnalysisMethod.OLD_TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));
        blackList.add(new Pair<>(BenchMark.PIXI, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_RECURSIVELY_RESOLVE_CALLGRAPH));

        blackList.add(new Pair<>(BenchMark.FabricJS, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_RECURSIVELY_RESOLVE_CALLGRAPH));
        blackList.add(new Pair<>(BenchMark.FabricJS, Options.StaticAnalysisMethod.OLD_TRADITIONAL_UNIFICATION_RECURSIVELY_RESOLVE_CALLGRAPH));
        blackList.add(new Pair<>(BenchMark.FabricJS, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));

        blackList.add(new Pair<>(BenchMark.angular, Options.StaticAnalysisMethod.OLD_TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));

        blackList.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_RECURSIVELY_RESOLVE_CALLGRAPH));
        blackList.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.OLD_TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));
        blackList.add(new Pair<>(BenchMark.three, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));

        blackList.add(new Pair<>(BenchMark.leaflet, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_RECURSIVELY_RESOLVE_CALLGRAPH));
        blackList.add(new Pair<>(BenchMark.leaflet, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));

        blackList.add(new Pair<>(BenchMark.react, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_RECURSIVELY_RESOLVE_CALLGRAPH));
        blackList.add(new Pair<>(BenchMark.react, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));

        blackList.add(new Pair<>(BenchMark.ember, Options.StaticAnalysisMethod.OLD_TRADITIONAL_UNIFICATION_RECURSIVELY_RESOLVE_CALLGRAPH));
        blackList.add(new Pair<>(BenchMark.ember, Options.StaticAnalysisMethod.OLD_TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));
        blackList.add(new Pair<>(BenchMark.ember, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_RECURSIVELY_RESOLVE_CALLGRAPH));
        blackList.add(new Pair<>(BenchMark.ember, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));

        blackList.add(new Pair<>(BenchMark.angular, Options.StaticAnalysisMethod.OLD_TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));

        blackList.add(new Pair<>(BenchMark.D3, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_RECURSIVELY_RESOLVE_CALLGRAPH));
        blackList.add(new Pair<>(BenchMark.D3, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));

        blackList.add(new Pair<>(BenchMark.PIXI, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_RECURSIVELY_RESOLVE_CALLGRAPH));
        blackList.add(new Pair<>(BenchMark.PIXI, Options.StaticAnalysisMethod.OLD_TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));
        blackList.add(new Pair<>(BenchMark.PIXI, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));

        blackList.add(new Pair<>(BenchMark.ace, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_RECURSIVELY_RESOLVE_CALLGRAPH));
        blackList.add(new Pair<>(BenchMark.ace, Options.StaticAnalysisMethod.OLD_TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));
        blackList.add(new Pair<>(BenchMark.ace, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));

        blackList.add(new Pair<>(BenchMark.leaflet, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_RECURSIVELY_RESOLVE_CALLGRAPH));
        blackList.add(new Pair<>(BenchMark.leaflet, Options.StaticAnalysisMethod.TRADITIONAL_UNIFICATION_UNIFY_EVERYTHING));
    }

    public static void compareMethods(List<BenchMark> benchMarks, long timeout) throws IOException {
        Map<BenchMark, Map<Options.StaticAnalysisMethod, Score>> benchmarkScores = new HashMap<>();
        int maxMethodLength = Arrays.asList(Options.StaticAnalysisMethod.values()).stream().map(method -> method.prettyString.length()).max(Integer::compare).get();
        int decimals = 4;
        for (BenchMark benchMark : benchMarks) {
            if (benchMark.declarationPath == null) {
                continue;
            }
            Map<Options.StaticAnalysisMethod, Score> scores = new HashMap<>();
            benchmarkScores.put(benchMark, scores);
            for (Options.StaticAnalysisMethod method : Options.StaticAnalysisMethod.values()) {
                if (blackList.contains(new Pair<>(benchMark, method))) {
                    scores.put(method, new Score(-1, -1, -1));
                    continue;
                }
                benchMark.options.staticMethod = method;
                println("With method: " + method.prettyString);
                Score score = Main.runAnalysisWithTimeout(benchMark, timeout);
                if (score == null) {
                    score = new Score(-1, -1, -1);
                }
                scores.put(method, score);
            }

            printMethods(benchmarkScores, maxMethodLength, decimals);
        }

        print("\n\n\n\n\n");

        printMethods(benchmarkScores, maxMethodLength, decimals);
    }

    private static void printMethods(Map<BenchMark, Map<Options.StaticAnalysisMethod, Score>> benchmarkScores, int maxMethodLength, int decimals) {
        for (Map.Entry<BenchMark, Map<Options.StaticAnalysisMethod, Score>> entry : benchmarkScores.entrySet()) {
            Map<Options.StaticAnalysisMethod, Score> dupScores = entry.getValue();
            println("Benchmark: " + entry.getKey().name);
            dupScores.entrySet().stream().sorted((o1, o2) -> Double.compare(o2.getValue().fMeasure, o1.getValue().fMeasure)).forEach(scoreEntry -> {
                Score score = scoreEntry.getValue();
                String prettyString = scoreEntry.getKey().prettyString;
                while (prettyString.length() < maxMethodLength) {
                    prettyString = prettyString + " ";
                }
                println(prettyString + " : " + toFixed(score.fMeasure, decimals) + " - " + toFixed(score.recall, decimals) + " - " + toFixed(score.precision, decimals));
            });
        }
    }

    static File file = new File("methods 5 depth.txt");
    static BufferedWriter writer;
    static {
        try {
            writer = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
    
    private static void print(String str) {
        try {
            writer.write(str);
            writer.flush();
            System.out.println(str);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private static void println(String string) {
        print(string + "\n");
    }
}