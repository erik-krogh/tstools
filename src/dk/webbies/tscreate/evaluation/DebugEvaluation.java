package dk.webbies.tscreate.evaluation;

import dk.webbies.tscreate.evaluation.descriptions.Description;

import java.util.*;

import static dk.webbies.tscreate.evaluation.DebugEvaluation.EvaluationType.*;

/**
 * Created by erik1 on 26-05-2016.
 */
public class DebugEvaluation extends Evaluation<List<DebugEvaluation.EvaluationStatement>> {
    DebugEvaluation() {
        
    }

    private void add(int depth, Map<Integer, List<EvaluationStatement>> map, Description description, String typePath, EvaluationType type) {
        EvaluationStatement statement = new EvaluationStatement(description, typePath, type, depth);
        add(depth, map, statement);
    }

    private void add(int depth, Map<Integer, List<EvaluationStatement>> map, EvaluationStatement statement) {
        maxDepth = Math.max(depth, maxDepth);
        if (map.containsKey(depth)) {
            map.get(depth).add(statement);
        } else {
            map.put(depth, new ArrayList<>(Arrays.asList(statement)));
        }
    }

    public void addFalseNegative(int depth, Description description, String typePath) {
        add(depth, falseNegatives, description, typePath, FALSE_NEGATIVE);
    }

    public void addFalsePositive(int depth, Description description, String typePath) {
        add(depth, falsePositives, description, typePath, FALSE_POSITIVE);
    }

    public void addTruePositive(int depth, Description description, String typePath) {
        add(depth, truePositive, description, typePath, TRUE_POSITIVE);
    }


    int IFound(int depth) {
        return get(depth, this.truePositive) + get(depth, this.falsePositives);
    }

    int thereIs(int depth) {
        return get(depth, this.truePositive) + get(depth, this.falseNegatives);
    }

    int get(int depth, Map<Integer, List<EvaluationStatement>> map) {
        if (map.containsKey(depth)) {
            return map.get(depth).size();
        } else {
            return 0;
        }
    }


    public void add(Evaluation eval) {
        if (!(eval instanceof DebugEvaluation)) {
            throw new RuntimeException();
        }
        DebugEvaluation evaluation = (DebugEvaluation) eval;
        this.maxDepth = Math.max(this.maxDepth, evaluation.maxDepth);
        addAll(evaluation.falseNegatives, this.falseNegatives);
        addAll(evaluation.falsePositives, this.falsePositives);
        addAll(evaluation.truePositive, this.truePositive);
    }

    private void addAll(Map<Integer, List<EvaluationStatement>> from, Map<Integer, List<EvaluationStatement>> to) {
        from.forEach((depth, list) -> {
            list.forEach(statement -> {
                add(depth, to, statement);
            });
        });
    }

    public int getTruePositives(int depth) {
        return get(depth, this.truePositive);
    }

    public String debugPrint() {
        StringBuilder builder = new StringBuilder();
        HashMap<Integer, List<EvaluationStatement>> falses = new HashMap<>();
        addAll(this.falseNegatives, falses);
        addAll(this.falsePositives, falses);
        ArrayList<Integer> keys = new ArrayList<>(falses.keySet());
        Collections.sort(keys);
        for (Integer key : keys) {
            builder.append("\n\n\n").append("Depth: ").append(key).append("\n");
            List<EvaluationStatement> list = falses.get(key);
            list.forEach((string) -> builder.append(string.toString()).append("\n"));
        }
        return builder.toString();
    }

    public Set<EvaluationStatement> getAllStatements() {
        Set<EvaluationStatement> result = new HashSet<>();
        this.truePositive.values().forEach(result::addAll);
        this.falseNegatives.values().forEach(result::addAll);
        this.falsePositives.values().forEach(result::addAll);
        return result;
    }

    public static final class EvaluationStatement {
        public final Description description;
        public final String typePath;
        public final EvaluationType type;
        public final int depth;

        public EvaluationStatement(Description description, String typePath, EvaluationType type, int depth) {
            this.description = description;
            this.typePath = typePath;
            this.type = type;
            this.depth = depth;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EvaluationStatement that = (EvaluationStatement) o;

            if (depth != that.depth) return false;
            if (!description.equals(that.description)) return false;
            if (!typePath.equals(that.typePath)) return false;
            return type == that.type;

        }

        @Override
        public int hashCode() {
            int result = description.hashCode();
            result = 31 * result + typePath.hashCode();
            result = 31 * result + type.hashCode();
            result = 31 * result + depth;
            return result;
        }

        @Override
        public String toString() {
            return description + " Path: " + typePath;
        }
    }

    public enum EvaluationType {
        TRUE_POSITIVE,
        FALSE_NEGATIVE,
        FALSE_POSITIVE;
    }
}
