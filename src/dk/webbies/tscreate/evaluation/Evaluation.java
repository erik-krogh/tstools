package dk.webbies.tscreate.evaluation;

import dk.webbies.tscreate.Score;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.function.Function;

/**
 * Created by Erik Krogh Kristensen on 14-12-2015.
 */
public class Evaluation {
    int maxDepth = -1;
    Map<Integer, List<String>> falseNegatives = new HashMap<>();
    Map<Integer, List<String>> falsePositives = new HashMap<>();
    Map<Integer, List<String>> truePositive = new HashMap<>();

    private void add(int depth, Map<Integer, List<String>> map, String description) {
        maxDepth = Math.max(depth, maxDepth);
        if (map.containsKey(depth)) {
            map.get(depth).add(description);
        } else {
            map.put(depth, new ArrayList<>(Arrays.asList(description)));
        }
    }

    public void addFalseNegative(int depth, String description) {
        add(depth, falseNegatives, description);
    }

    public void addFalsePositive(int depth, String description) {
        add(depth, falsePositives, description);
    }

    public void addTruePositive(int depth, String description) {
        add(depth, truePositive, description);
    }

    public String print() {
        StringBuilder builder = new StringBuilder();
        for (int depth = 0; depth <= maxDepth; depth++) {
            if (Double.isNaN(precision(depth)) || Double.isNaN(recall(depth))) {
                continue;
            }
            builder.append("depth: ").append(depth).append(" ");
            builder.append("precision: ").append(Util.toFixed(precision(depth), 2));
            builder.append(" recall: ").append(Util.toFixed(recall(depth), 2));
            builder.append(" thereIs(").append(thereIs(depth)).append(")");
            builder.append(" iFound(").append(IFound(depth)).append(")");
            builder.append("\n");
        }

        builder.append("Score: ").append(score().fMeasure);
        return builder.toString();
    }

    @Override
    public String toString() {
        return print();
    }

    private int IFound(int depth) {
        return get(depth, this.truePositive) + get(depth, this.falsePositives);
    }

    private int thereIs(int depth) {
        return get(depth, this.truePositive) + get(depth, this.falseNegatives);
    }

    private int get(int depth, Map<Integer, List<String>> map) {
        if (map.containsKey(depth)) {
            return map.get(depth).size();
        } else {
            return 0;
        }
    }

    double precision(int depth) {
        double fp = get(depth, this.falsePositives);
        double tp = get(depth, this.truePositive);
        double result = tp / (tp + fp);
        if (Double.isNaN(result)) {
            return 0;
        }
        return result;
    }

    double recall(int depth) {
        double fn = get(depth, this.falseNegatives);
        double tp = get(depth, this.truePositive);
        double result = tp / (tp + fn);
        if (Double.isNaN(result)) {
            return 0;
        }
        return result;
    }

    double fMeasure(int depth) {
        double precision = precision(depth);
        double recall = recall(depth);
        double fMeasure = 2 / (1 / precision + 1 / recall);
        if (Double.isNaN(fMeasure)) {
            throw new RuntimeException();
        }
        return fMeasure;
    }

    public Score score() {
        double fMeasure = score(this::fMeasure);
        double precision = score(this::precision);
        double recall = score(this::recall);
        return new Score(fMeasure, precision, recall);
    }

    private double score(Function<Integer, Double> function) {
        double result = 0;
        double measure = 1;
        for (int depth = 1; depth <= maxDepth; depth++) {
            measure = function.apply(depth) * measure;
            result += measure * Math.pow(2, -depth);
        }
        return result;
    }

    public void add(Evaluation evaluation) {
        addAll(evaluation.falseNegatives, this.falseNegatives);
        addAll(evaluation.falsePositives, this.falsePositives);
        addAll(evaluation.truePositive, this.truePositive);
    }

    private void addAll(Map<Integer, List<String>> from, Map<Integer, List<String>> to) {
        from.forEach((depth, list) -> {
            list.forEach(description -> {
                add(depth, to, description);
            });
        });
    }

    public int getTruePositives(int depth) {
        return get(depth, this.truePositive);
    }

    public void debugPrint() {
        HashMap<Integer, List<String>> falses = new HashMap<>();
        addAll(this.falseNegatives, falses);
        addAll(this.falsePositives, falses);
        ArrayList<Integer> keys = new ArrayList<>(falses.keySet());
        Collections.sort(keys);
        for (Integer key : keys) {
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("Depth: " + key);
            List<String> list = falses.get(key);
            list.forEach(System.out::println);
        }
    }
}
