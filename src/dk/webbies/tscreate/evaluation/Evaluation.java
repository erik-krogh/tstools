package dk.webbies.tscreate.evaluation;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 14-12-2015.
 */
public class Evaluation {
    int maxDepth = -1;

    private void add(int depth, Map<Integer, Integer> map) {
        maxDepth = Math.max(depth, maxDepth);
        if (map.containsKey(depth)) {
            map.put(depth, 1 + map.get(depth));
        } else {
            map.put(depth, 1);
        }
    }

    Map<Integer, Integer> falseNegatives = new HashMap<>();

    public void addFalseNegative(int depth) {
        add(depth, falseNegatives);
    }

    Map<Integer, Integer> falsePositives = new HashMap<>();

    public void addFalsePositive(int depth) {
        add(depth, falsePositives);
    }

    Map<Integer, Integer> truePositive = new HashMap<>();

    public void addTruePositive(int depth) {
        add(depth, truePositive);
    }

    public String print() {
        StringBuilder builder = new StringBuilder();
        for (int depth = 0; depth <= maxDepth; depth++) {
            builder.append("depth: ").append(depth).append(" ");
            builder.append("precision: ").append(toFixed(precision(depth), 2));
            builder.append(" recall: ").append(toFixed(recall(depth), 2));
            builder.append("\n");
        }

        return builder.toString();
    }

    private int get(int depth, Map<Integer, Integer> map) {
        if (map.containsKey(depth)) {
            return map.get(depth);
        } else {
            return 0;
        }
    }

    private double precision(int depth) {
        double fn = get(depth, this.falseNegatives);
        double fp = get(depth, this.falsePositives);
        double tp = get(depth, this.truePositive);
        return tp / (tp + fp);
    }

    private double recall(int depth) {
        double fn = get(depth, this.falseNegatives);
        double fp = get(depth, this.falsePositives);
        double tp = get(depth, this.truePositive);
        return tp / (tp + fn);
    }

    private String toFixed(double number, int decimals) {
        BigDecimal numberBigDecimal = new BigDecimal(number);
        numberBigDecimal = numberBigDecimal.setScale(decimals, BigDecimal.ROUND_HALF_UP);
        return numberBigDecimal.toString();
    }
}
