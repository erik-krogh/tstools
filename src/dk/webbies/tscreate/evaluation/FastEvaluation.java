package dk.webbies.tscreate.evaluation;

import dk.webbies.tscreate.Score;
import dk.webbies.tscreate.evaluation.descriptions.Description;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.function.Function;

/**
 * Created by Erik Krogh Kristensen on 14-12-2015.
 */
public class FastEvaluation extends Evaluation<Integer> {


    public FastEvaluation() {
        
    }

    private void add(int depth, Map<Integer, Integer> map) {
        maxDepth = Math.max(depth, maxDepth);
        if (map.containsKey(depth)) {
            map.put(depth, map.get(depth) + 1);
        } else {
            map.put(depth, 1);
        }
    }

    public void addFalseNegative(int depth, Description description, String typePath) {
        add(depth, falseNegatives);
    }

    public void addFalsePositive(int depth, Description description, String typePath) {
        add(depth, falsePositives);
    }

    public void addTruePositive(int depth, Description description, String typePath) {
        add(depth, truePositive);
    }

    @Override
    public String toString() {
        return print();
    }

    int IFound(int depth) {
        return get(depth, this.truePositive) + get(depth, this.falsePositives);
    }

    int thereIs(int depth) {
        return get(depth, this.truePositive) + get(depth, this.falseNegatives);
    }

    int get(int depth, Map<Integer, Integer> map) {
        if (map.containsKey(depth)) {
            return map.get(depth);
        } else {
            return 0;
        }
    }


    public void add(Evaluation eval) {
        if (!(eval instanceof FastEvaluation)) {
            throw new RuntimeException();
        }
        FastEvaluation evaluation = (FastEvaluation) eval;
        this.maxDepth = Math.max(this.maxDepth, evaluation.maxDepth);
        addAll(evaluation.falseNegatives, this.falseNegatives);
        addAll(evaluation.falsePositives, this.falsePositives);
        addAll(evaluation.truePositive, this.truePositive);
    }

    private void addAll(Map<Integer, Integer> from, Map<Integer, Integer> to) {
        from.forEach((depth, list) -> {
            for (Map.Entry<Integer, Integer> entry : from.entrySet()) {
                Integer entryDepth = entry.getKey();
                Integer count = entry.getValue();
                if (to.containsKey(entryDepth)) {
                    to.put(entryDepth, to.get(entryDepth) + count);
                } else {
                    to.put(entryDepth, count);
                }
            }
        });
    }

    public int getTruePositives(int depth) {
        return get(depth, this.truePositive);
    }
}
