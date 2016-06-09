package dk.webbies.tscreate.main;

import dk.au.cs.casa.typescript.types.*;
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

import static dk.webbies.tscreate.declarationReader.DeclarationParser.getTypeSpecification;
import static dk.webbies.tscreate.evaluation.DebugEvaluation.EvaluationType.TRUE_POSITIVE;

/**
 * Created by erik1 on 26-05-2016.
 */
public class CompareVersions {

    public static final Comparator<DebugEvaluation.EvaluationStatement> sortStmts = (a, b) -> {
        if (Double.compare(a.depth, b.depth) == 0) {
            return a.typePath.compareTo(b.typePath);
        }
        return Double.compare(a.depth, b.depth);
    };

    static List<DebugEvaluation.EvaluationStatement> compareWithNew(BenchMark oldVersion, BenchMark newScript, boolean filterFromOld) throws IOException {
        if (!oldVersion.getOptions().debugPrint) {
            throw new RuntimeException("Cannot do this without debugprint");
        }
        if (oldVersion.getOptions().evaluationMethod != Options.EvaluationMethod.EVERYTHING) {
            throw new RuntimeException("Not setup for this");
        }

        String oldBenchResultingDecPath = Main.getResultingDeclarationPath(oldVersion);
        if (!new File(oldBenchResultingDecPath).exists()) {
            Main.runAnalysis(oldVersion);
        }

        String orgDeclarationPath = newScript.declarationPath;
        newScript.declarationPath = oldBenchResultingDecPath;
        DebugEvaluation newEvaluation = (DebugEvaluation) Main.getEvaluation(newScript, Main.getResultingDeclarationPath(newScript));
        newScript.declarationPath = orgDeclarationPath;

        Set<DebugEvaluation.EvaluationStatement> allStatements = newEvaluation.getAllStatements();

        if (oldVersion.declarationPath != null && filterFromOld) {
            Type global = getTypeSpecification(oldVersion.languageLevel.environment, oldVersion.dependencyDeclarations(), oldVersion.declarationPath).getGlobal();
            allStatements = allStatements.stream().filter(stmt -> {
                if (stmt.typePath.equals("window")) {
                    return true;
                }
                assert stmt.typePath.startsWith("window.");
                String path = stmt.typePath.substring("window.".length(), stmt.typePath.length());
                if (!path.contains(".")) {
                    return true;
                }
                String[] split = path.split("\\.");
                path = path.substring(0, path.length() - split[split.length - 1].length() - 1);

                return global.accept(new LookupTypeVisitor(path));
            }).collect(Collectors.toSet());
        }

        /*applyCleanupRuleOnSame(allStatements,
                (stmt) ->
                        stmt.description.equals("was a function, that was right"),
                (stmt, next) ->
                        next.typePath.equals(stmt.typePath));*/

        return allStatements.stream().sorted(sortStmts).filter(stmt -> stmt.type != TRUE_POSITIVE).collect(Collectors.toList());
    }

    public static List<DebugEvaluation.EvaluationStatement> compareHandWritten(BenchMark oldBench, BenchMark newBench) throws IOException {
        return ((DebugEvaluation)Main.getEvaluation(oldBench, newBench.declarationPath)).getAllStatements().stream().filter(stmt -> stmt.type != TRUE_POSITIVE).collect(Collectors.toList());
    }

    public static Set<DebugEvaluation.EvaluationStatement> compareTheTwo(BenchMark benchMark, BenchMark newScript) throws IOException {
        Collection<DebugEvaluation.EvaluationStatement> comparedWithNew = compareWithNew(benchMark, newScript, true);
        Collection<DebugEvaluation.EvaluationStatement> comparedThroughOld = compareWithNewThroughOldDec(benchMark, newScript);

        Set<String> pathsInCompareToOld = comparedThroughOld.stream().map(stmt -> stmt.toString()).distinct().collect(Collectors.toSet());
        Set<String> pathsInCompareWithNew = comparedWithNew.stream().map(stmt -> stmt.toString()).distinct().collect(Collectors.toSet());

        Set<DebugEvaluation.EvaluationStatement> uniqueInNew = comparedWithNew.stream().filter(stmt -> !pathsInCompareToOld.contains(stmt.toString())).sorted(sortStmts).collect(Collectors.toSet());
        Set<DebugEvaluation.EvaluationStatement> uniqueInCompareThroughOld = comparedThroughOld.stream().filter(stmt -> !pathsInCompareWithNew.contains(stmt.toString())).sorted(sortStmts).collect(Collectors.toSet());

        applyCleanupRuleOnOther(uniqueInNew, uniqueInCompareThroughOld,
                (stmt) ->
                        stmt.toString().contains("excess property"),
                (stmt, next) ->
                        next.typePath.startsWith(stmt.typePath) && next.typePath.length() != stmt.typePath.length());

        applyCleanupRuleOnOther(uniqueInCompareThroughOld, uniqueInNew,
                (stmt) ->
                        stmt.toString().contains("excess property"),
                (stmt, next) ->
                        next.typePath.startsWith(stmt.typePath) && next.typePath.length() != stmt.typePath.length());

        pathsInCompareToOld.clear();
        pathsInCompareWithNew.clear();

        comparedWithNew.stream().map(stmt -> stmt.typePath).forEach(pathsInCompareWithNew::add);
        comparedThroughOld.stream().map(stmt -> stmt.typePath).forEach(pathsInCompareToOld::add);

        uniqueInNew = uniqueInNew.stream().filter(stmt -> !pathsInCompareToOld.contains(stmt.typePath)).collect(Collectors.toSet());
        uniqueInCompareThroughOld = uniqueInCompareThroughOld.stream().filter(stmt -> !pathsInCompareWithNew.contains(stmt.typePath)).collect(Collectors.toSet());

        printEvaluations(uniqueInNew, "uniqueToCompareWithNew.txt");
        printEvaluations(uniqueInCompareThroughOld, "uniqueToCompareThroughOld.txt");

//        compareWithNew(benchMark, newScript);
//        compareWithNewThroughOldDec(benchMark, newScript);



        return null;

    }

    public static void printEvaluations(Collection<DebugEvaluation.EvaluationStatement> uniqueToNewList, String path) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("Statements: \n");
        uniqueToNewList.stream().sorted(sortStmts).forEach(statement -> builder.append(statement).append("\n"));

//        builder.append("\n\n\n\nUnique to old: \n");
//        List<DebugEvaluation.EvaluationStatement> uniqueToOldList = uniqueToOld.stream().sorted(sortStmts).filter(statement -> statement.type == TRUE_POSITIVE).collect(Collectors.toList());
//        uniqueToOldList.forEach(statement -> builder.append(statement).append("\n"));

        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)));
        writer.write(builder.toString());
        writer.close();
    }

    public static Collection<DebugEvaluation.EvaluationStatement> compareWithNewThroughOldDec(BenchMark benchMark, BenchMark newScript) throws IOException {
        if (!benchMark.getOptions().debugPrint) {
            throw new RuntimeException("Cannot do this without debugprint");
        }
        if (benchMark.getOptions().evaluationMethod != Options.EvaluationMethod.EVERYTHING) {
            throw new RuntimeException("Not setup for this");
        }

        DebugEvaluation oldEvaluation = (DebugEvaluation) Main.getEvaluation(benchMark, Main.getResultingDeclarationPath(benchMark));
        String orgDeclarationpath = newScript.declarationPath;
        newScript.declarationPath = benchMark.declarationPath;
        DebugEvaluation newEvaluation = (DebugEvaluation) Main.getEvaluation(newScript, Main.getResultingDeclarationPath(newScript));
        newScript.declarationPath = orgDeclarationpath;

        Set<DebugEvaluation.EvaluationStatement> uniqueToOld = oldEvaluation.getAllStatements();
        uniqueToOld.removeAll(newEvaluation.getAllStatements());

        Set<DebugEvaluation.EvaluationStatement> uniqueToNew = newEvaluation.getAllStatements();
        uniqueToNew.removeAll(oldEvaluation.getAllStatements());

//        cleanUpUniques(uniqueToOld);
//        cleanUpUniques(uniqueToNew);

        /*applyCleanupRuleOnSame(uniqueToOld,
                (stmt) ->
                        stmt.description.equals("was a function, that was right"),
                (stmt, next) ->
                        next.typePath.equals(stmt.typePath));*/

        /*applyCleanupRuleOnOther(uniqueToNew, uniqueToOld,
                (stmt) ->
                        stmt.description.startsWith("property missing:"),
                (stmt, next) ->
                        next.typePath.equals(stmt.typePath));*/

        applyCleanupRuleOnOther(uniqueToNew, uniqueToOld,
                (stmt) ->
                        stmt.toString().contains("property missing"),
                (stmt, next) ->
                        next.typePath.contains(stmt.typePath) && next.typePath.length() != stmt.typePath.length()
        );

        Set<DebugEvaluation.EvaluationStatement> result = new HashSet<>();
        result.addAll(uniqueToNew);
//        uniqueToOld.stream().filter(stmt -> stmt.type != TRUE_POSITIVE).map(stmt -> new DebugEvaluation.EvaluationStatement(stmt.description, stmt.typePath, stmt.type, stmt.depth)).forEach(result::add);

        return result;
    }

    private static List<DebugEvaluation.EvaluationStatement> filterOnPath(Collection<DebugEvaluation.EvaluationStatement> statements, String pathPrefix) {
        return statements.stream().filter(stmt -> stmt.typePath.startsWith(pathPrefix)).sorted(sortStmts).collect(Collectors.toList());
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

    public static Collection<DebugEvaluation.EvaluationStatement> compareWithNewRemoveHandwritten(BenchMark oldBench, BenchMark newBench, boolean throughOld) throws IOException {
        Collection<DebugEvaluation.EvaluationStatement> comparedGenerated;
        if (throughOld) {
            comparedGenerated = compareWithNewThroughOldDec(newBench, oldBench);
        } else {
            comparedGenerated = compareWithNew(newBench, oldBench, true);
        }
        Set<DebugEvaluation.EvaluationStatement> comparedHandwritten = new HashSet<>(compareHandWritten(newBench, oldBench));

        return comparedGenerated.stream().filter(stmt -> !comparedHandwritten.contains(stmt)).collect(Collectors.toList());
    }

    private static class LookupTypeVisitor implements TypeVisitor<Boolean> {
        private String path;

        public LookupTypeVisitor(String path) {
            this.path = path;
        }

        public String firstPart() {
            return path.split("\\.")[0];
        }

        public String rest() {
            if (!path.contains(".")) {
                return "";
            }
            return path.substring(firstPart().length() + 1, path.length());
        }

        private boolean next(Type type) {
            if (rest().isEmpty()) {
                return true;
            }
            return type.accept(new LookupTypeVisitor(rest()));
        }

        @Override
        public Boolean visit(AnonymousType t) {
            throw new RuntimeException();
        }

        @Override
        public Boolean visit(ClassType t) {
            throw new RuntimeException();
        }

        @Override
        public Boolean visit(GenericType t) {
            return t.toInterface().accept(this);
        }

        @Override
        public Boolean visit(InterfaceType t) {
            String first = firstPart();
            if (t.getBaseTypes() != null) {
                boolean foundInBase = t.getBaseTypes().stream().map(base -> base.accept(this)).reduce(Boolean.FALSE, Boolean::logicalOr);
                if (foundInBase) {
                    return true;
                }
            }
            if (first.contains("[")) {
                List<Signature> signatures;
                if (first.equals("[constructor]")) {
                    signatures = t.getDeclaredConstructSignatures();
                } else {
                    signatures = t.getDeclaredCallSignatures();
                }
                if (signatures == null) {
                    return false;
                } else {
                    LookupTypeVisitor restVisitor = new LookupTypeVisitor(rest());
                    return signatures.stream().map(restVisitor::visitSignature).reduce(Boolean.FALSE, Boolean::logicalOr);
                }
            } else {
                if (t.getDeclaredProperties() != null && t.getDeclaredProperties().get(first) != null) {
                    return next(t.getDeclaredProperties().get(first));
                }
            }
            return false;
        }

        private Boolean visitSignature(Signature sig) {
            String first = firstPart();
            if (first.startsWith("[arg")) {
                int argNumber = Integer.parseInt(first.substring("[arg".length(), first.length() - 1));
                if (sig.getParameters() == null) {
                    return false;
                }
                if (sig.getParameters().size() <= argNumber) {
                    return false;
                } else {
                    return next(sig.getParameters().get(argNumber).getType());
                }
            } else {
                if (sig.getResolvedReturnType() == null) {
                    return false;
                }
                return next(sig.getResolvedReturnType());
            }
        }


        @Override
        public Boolean visit(ReferenceType t) {
            return t.getTarget().accept(this);
        }

        @Override
        public Boolean visit(SimpleType t) {
            return false;
        }

        @Override
        public Boolean visit(TupleType t) {
            throw new RuntimeException();
        }

        @Override
        public Boolean visit(UnionType t) {
            return t.getElements().stream().map(this::next).reduce(Boolean.TRUE, Boolean::logicalOr);
        }

        @Override
        public Boolean visit(UnresolvedType t) {
            throw new RuntimeException();
        }

        @Override
        public Boolean visit(TypeParameterType t) {
            throw new RuntimeException();
        }

        @Override
        public Boolean visit(SymbolType t) {
            throw new RuntimeException();
        }
    }
}
