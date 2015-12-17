package dk.webbies.tscreate.evaluation;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 14-12-2015.
 */
// FIXME: The last depth is always 100% precision and recall.
public class Evaluation {
    int maxDepth = -1;
    Map<Integer, Integer> falseNegatives = new HashMap<>();
    Map<Integer, Integer> falsePositives = new HashMap<>();
    Map<Integer, Integer> truePositive = new HashMap<>();

    private void add(int depth, Map<Integer, Integer> map) {
        add(depth, map, 1);
    }

    private void add(int depth, Map<Integer, Integer> map, int count) {
        maxDepth = Math.max(depth, maxDepth);
        if (map.containsKey(depth)) {
            map.put(depth, count + map.get(depth));
        } else {
            map.put(depth, count);
        }
    }

    public void addFalseNegative(int depth) {
        add(depth, falseNegatives);
    }

    public void addFalsePositive(int depth) {
        add(depth, falsePositives);
    }

    public void addTruePositive(int depth) {
        add(depth, truePositive);
    }

    public String print() {
        StringBuilder builder = new StringBuilder();
        for (int depth = 0; depth <= maxDepth; depth++) {
            if (Double.isNaN(precision(depth)) || Double.isNaN(recall(depth))) {
                continue;
            }
            builder.append("depth: ").append(depth).append(" ");
            builder.append("precision: ").append(toFixed(precision(depth), 2));
            builder.append(" recall: ").append(toFixed(recall(depth), 2));
            builder.append(" thereIs(").append(thereIs(depth)).append(")");
            builder.append(" iFound(").append(IFound(depth)).append(")");
            builder.append("\n");
        }

        return builder.toString();
    }

    private int IFound(int depth) {
        return get(depth, this.truePositive) + get(depth, this.falsePositives);
    }

    private int thereIs(int depth) {
        return get(depth, this.truePositive) + get(depth, this.falseNegatives);
    }

    private int get(int depth, Map<Integer, Integer> map) {
        if (map.containsKey(depth)) {
            return map.get(depth);
        } else {
            return 0;
        }
    }

    double precision(int depth) {
        double fn = get(depth, this.falseNegatives);
        double fp = get(depth, this.falsePositives);
        double tp = get(depth, this.truePositive);
        return tp / (tp + fp);
    }

    double recall(int depth) {
        double fn = get(depth, this.falseNegatives);
        double fp = get(depth, this.falsePositives);
        double tp = get(depth, this.truePositive);
        return tp / (tp + fn);
    }

    private String toFixed(double number, int decimals) {
        if (Double.isInfinite(number)) {
            return number > 0 ? "Infinite" : "-Infinite";
        } else if (Double.isNaN(number)) {
            return "NaN";
        }
        BigDecimal numberBigDecimal = new BigDecimal(number);
        numberBigDecimal = numberBigDecimal.setScale(decimals, BigDecimal.ROUND_HALF_UP);
        return numberBigDecimal.toString();
    }

    public void add(Evaluation evaluation) {
        addAll(evaluation.falseNegatives, this.falseNegatives);
        addAll(evaluation.falsePositives, this.falsePositives);
        addAll(evaluation.truePositive, this.truePositive);
    }

    private void addAll(Map<Integer, Integer> from, Map<Integer, Integer> to) {
        from.forEach((depth, count) -> {
            add(depth, to, count);
        });
    }

    public int getTruePositives(int depth) {
        return get(depth, this.truePositive);
    }
}
