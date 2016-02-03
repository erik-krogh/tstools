package dk.webbies.tscreate.evaluation;

import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.Score;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.function.Function;

/**
 * Created by Erik Krogh Kristensen on 14-12-2015.
 */
public class Evaluation {
    int maxDepth = -1;
    Map<Integer, Object> falseNegatives = new HashMap<>();
    Map<Integer, Object> falsePositives = new HashMap<>();
    Map<Integer, Object> truePositive = new HashMap<>();

    private final boolean debug;

    public Evaluation(boolean saveDebug) {
        this.debug = saveDebug;
    }

    private void add(int depth, Map<Integer, Object> map, String description, String typePath) {
        if (debug) {
            description += " Path: " + typePath;
        } else {
            description = null;
        }
        add(depth, map, description);
    }

    private void add(int depth, Map<Integer, Object> map, String description) {
        maxDepth = Math.max(depth, maxDepth);
        if (map.containsKey(depth)) {
            if (debug) {
                ((List<String>)map.get(depth)).add(description);
            } else {
                map.put(depth, ((Integer) map.get(depth) + 1));
            }
        } else {
            if (debug) {
                map.put(depth, new ArrayList<>(Arrays.asList(description)));
            } else {
                map.put(depth, 1);
            }
        }
    }

    public void addFalseNegative(int depth, String description, String typePath) {
        add(depth, falseNegatives, description, typePath);
    }

    public void addFalsePositive(int depth, String description, String typePath) {
        add(depth, falsePositives, description, typePath);
    }

    public void addTruePositive(int depth, String description, String typePath) {
        add(depth, truePositive, description, typePath);
    }

    public String print() {
        StringBuilder builder = new StringBuilder();
        for (int depth = 1; depth <= maxDepth; depth++) {
            if (Double.isNaN(precision(depth)) || Double.isNaN(recall(depth))) {
                continue;
            }
            builder.append("depth: ").append(depth).append(" ");
            builder.append("f-measure: ").append(Util.toFixed(fMeasure(depth), 2));
            builder.append(" precision: ").append(Util.toFixed(precision(depth), 2));
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

    private int get(int depth, Map<Integer, Object> map) {
        if (map.containsKey(depth)) {
            if (debug) {
                return ((List<String>)map.get(depth)).size();
            } else {
                return (Integer) map.get(depth);
            }
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

    private void addAll(Map<Integer, Object> from, Map<Integer, Object> to) {
        from.forEach((depth, list) -> {
            if (debug) {
                ((List<String>)list).forEach(description -> {
                    add(depth, to, description);
                });
            } else {
                for (Map.Entry<Integer, Object> entry : from.entrySet()) {
                    Integer entryDepth = entry.getKey();
                    Integer count = (Integer) entry.getValue();
                    if (to.containsKey(entryDepth)) {
                        to.put(entryDepth, ((Integer)to.get(entryDepth)) + count);
                    } else {
                        to.put(entryDepth, count);
                    }
                }

            }
        });
    }

    public int getTruePositives(int depth) {
        return get(depth, this.truePositive);
    }

    public String debugPrint() {
        assert this.debug;
        StringBuilder builder = new StringBuilder();
        HashMap<Integer, Object> falses = new HashMap<>();
        addAll(this.falseNegatives, falses);
        addAll(this.falsePositives, falses);
        ArrayList<Integer> keys = new ArrayList<>(falses.keySet());
        Collections.sort(keys);
        for (Integer key : keys) {
            builder.append("\n\n\n\n").append("Depth: ").append(key);
            List<String> list = (List<String>) falses.get(key);
            list.forEach((string) -> builder.append(string).append("\n"));
        }
        return builder.toString();
    }
}
