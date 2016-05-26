package dk.webbies.tscreate.evaluation;

import dk.webbies.tscreate.Score;
import dk.webbies.tscreate.util.Util;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;
import java.util.function.Function;

/**
 * Created by Erik Krogh Kristensen on 14-12-2015.
 */
public abstract class Evaluation<T> {
    int maxDepth = -1;

    Map<Integer, T> falseNegatives = new HashMap<>();
    Map<Integer, T> falsePositives = new HashMap<>();
    Map<Integer, T> truePositive = new HashMap<>();

    public abstract void addFalseNegative(int depth, String description, String typePath);

    public abstract void addFalsePositive(int depth, String description, String typePath);

    public abstract void addTruePositive(int depth, String description, String typePath);

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

        builder.append("Score: ").append(score().fMeasure).append("\n");
        builder.append("Precision: ").append(score().precision).append("\n");
        builder.append("Recall: ").append(score().recall).append("\n");
        return builder.toString();
    }

    @Override
    public String toString() {
        return print();
    }

    abstract int get(int depth, Map<Integer, T> map);

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
        return score(false);
    }

    public Score score(boolean makeMax1) {
        double fMeasure = score(this::fMeasure, makeMax1);
        double precision = score(this::precision, makeMax1);
        double recall = score(this::recall, makeMax1);
        return new Score(fMeasure, precision, recall);
    }

    protected double score(Function<Integer, Double> function, boolean makeMax1) {
        double result = 0;
        double measure = 1;
        int startDepth = 1;
        for (int depth = startDepth; depth <= maxDepth; depth++) {
            measure = function.apply(depth) * measure;
            result += measure * Math.pow(2, -depth);
        }
        assert !Double.isNaN(result);

        if (makeMax1) {
            double makeMax1Factor = 1 / (1 - Math.pow(2, -(Math.max(maxDepth, 1))));
            result *= makeMax1Factor;
        }

        assert result <= 1;
        return result;
    }

    public abstract void add(Evaluation evaluation);

    public abstract int getTruePositives(int depth);

    abstract int IFound(int depth);

    abstract int thereIs(int depth);

    public static Evaluation create(boolean debugPrint) {
        if (debugPrint) {
            return new DebugEvaluation();
        } else {
            return new FastEvaluation();
        }
    }
}
