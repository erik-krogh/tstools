package dk.webbies.tscreate.main;

import dk.webbies.tscreate.BenchMark;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.evaluation.DebugEvaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.evaluation.DebugEvaluation.EvaluationType.FALSE_NEGATIVE;
import static dk.webbies.tscreate.evaluation.DebugEvaluation.EvaluationType.FALSE_POSITIVE;
import static dk.webbies.tscreate.evaluation.DebugEvaluation.EvaluationType.TRUE_POSITIVE;

/**
 * Created by erik1 on 26-05-2016.
 */
public class CompareWithNew {
    static void compareWithNew(BenchMark benchMark) throws IOException {
        if (!benchMark.getOptions().debugPrint) {
            throw new RuntimeException("Cannot do this without debugprint");
        }
        if (benchMark.newScript == null || benchMark.getOptions().evaluationMethod != Options.EvaluationMethod.EVERYTHING) {
            throw new RuntimeException("Not setup for this");
        }

        DebugEvaluation oldEvaluation = (DebugEvaluation) Main.getEvaluation(benchMark);
        BenchMark newScript = benchMark.newScript;
        newScript.declarationPath = benchMark.declarationPath;
        DebugEvaluation newEvaluation = (DebugEvaluation) Main.getEvaluation(newScript);

        Set<DebugEvaluation.EvaluationStatement> uniqueToOld = oldEvaluation.getAllStatements();
        uniqueToOld.removeAll(newEvaluation.getAllStatements());

        Set<DebugEvaluation.EvaluationStatement> uniqueToNew = newEvaluation.getAllStatements();
        uniqueToNew.removeAll(oldEvaluation.getAllStatements());

        cleanUpUniques(uniqueToOld);
        cleanUpUniques(uniqueToNew);

        applyCleanupRuleOnSame(uniqueToOld,
                (stmt) ->
                        stmt.description.equals("was a function, that was right"),
                (stmt, next) ->
                        next.typePath.equals(stmt.typePath));

        applyCleanupRuleOnOther(uniqueToNew, uniqueToOld,
                (stmt) ->
                        stmt.description.startsWith("property missing:"),
                (stmt, next) ->
                        next.typePath.equals(stmt.typePath));

        StringBuilder builder = new StringBuilder();

        builder.append("Unique to new: \n");
        Comparator<DebugEvaluation.EvaluationStatement> sortStmts = (a, b) -> {
            if (Double.compare(a.depth, b.depth) == 0) {
                return a.typePath.compareTo(b.typePath);
            }
            return Double.compare(a.depth, b.depth);
        };
        List<DebugEvaluation.EvaluationStatement> uniqueToNewList = uniqueToNew.stream().sorted(sortStmts).filter(statement -> statement.type == FALSE_POSITIVE || statement.type == FALSE_NEGATIVE).collect(Collectors.toList());
        uniqueToNewList.forEach(statement -> builder.append(statement).append("\n"));

        builder.append("\n\n\n\nUnique to old: \n");
        List<DebugEvaluation.EvaluationStatement> uniqueToOldList = uniqueToOld.stream().sorted(sortStmts).filter(statement -> statement.type == TRUE_POSITIVE).collect(Collectors.toList());
        uniqueToOldList.forEach(statement -> builder.append(statement).append("\n"));

        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("tmp.txt")));
        writer.write(builder.toString());
        writer.close();
    }

    private static void applyCleanupRuleOnOther(Set<DebugEvaluation.EvaluationStatement> searchFrom, Set<DebugEvaluation.EvaluationStatement> removeFrom, Predicate<DebugEvaluation.EvaluationStatement> comparisonMatch, BiFunction<DebugEvaluation.EvaluationStatement, DebugEvaluation.EvaluationStatement, Boolean> shouldRemove) {
        new HashSet<>(searchFrom).forEach(statement -> {
            if (comparisonMatch.test(statement)) {
                Iterator<DebugEvaluation.EvaluationStatement> iterator = removeFrom.iterator();
                while (iterator.hasNext()) {
                    DebugEvaluation.EvaluationStatement next = iterator.next();
                    if (statement != next && shouldRemove.apply(statement, next)) {
                        iterator.remove();
                    }
                }
            }
        });
    }

    private static void applyCleanupRuleOnSame(Set<DebugEvaluation.EvaluationStatement> set, Predicate<DebugEvaluation.EvaluationStatement> comparisonMatch, BiFunction<DebugEvaluation.EvaluationStatement, DebugEvaluation.EvaluationStatement, Boolean> shouldRemove) {
        new HashSet<>(set).forEach(statement -> {
            if (comparisonMatch.test(statement)) {
                Iterator<DebugEvaluation.EvaluationStatement> iterator = set.iterator();
                while (iterator.hasNext()) {
                    DebugEvaluation.EvaluationStatement next = iterator.next();
                    if (statement != next && shouldRemove.apply(statement, next)) {
                        iterator.remove();
                    }
                }
            }
        });
    }

    private static void cleanUpUniques(Set<DebugEvaluation.EvaluationStatement> set) {
        applyCleanupRuleOnSame(set,
                (stmt) ->
                        stmt.type == TRUE_POSITIVE,
                (stmt, next) ->
                        next.typePath.startsWith(stmt.typePath) && next.typePath.length() != stmt.typePath.length());
    }
}
