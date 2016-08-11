package dk.webbies.tscreate.main;

import com.google.javascript.jscomp.parsing.parser.Parser;
import dk.webbies.tscreate.BenchMark;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.Score;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.analysis.declarations.DeclarationBuilder;
import dk.webbies.tscreate.analysis.declarations.DeclarationPrinter;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.UnnamedObjectType;
import dk.webbies.tscreate.analysis.methods.NoTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.combined.CombinedTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.contextSensitive.combined.CombinedContextSensitiveTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.contextSensitive.mixed.MixedContextSensitiveTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.contextSensitive.pureSubsets.PureSubsetsContextSensitiveTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.mixed.MixedTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.old.analysis.OldTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.pureSubsets.PureSubsetsTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.unionEverything.UnionEverythingTypeAnalysis;
import dk.webbies.tscreate.analysis.methods.unionRecursively.UnionRecursivelyTypeAnalysis;
import dk.webbies.tscreate.cleanup.RedundantInterfaceCleaner;
import dk.webbies.tscreate.evaluation.DebugEvaluation;
import dk.webbies.tscreate.evaluation.DeclarationEvaluator;
import dk.webbies.tscreate.evaluation.Evaluation;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.ClassHierarchyExtractor;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.main.patch.*;
import dk.webbies.tscreate.paser.AST.AstNode;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.paser.JavaScriptParser;
import dk.webbies.tscreate.util.Util;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.BenchMark.*;
import static dk.webbies.tscreate.Options.StaticAnalysisMethod.*;
import static dk.webbies.tscreate.declarationReader.DeclarationParser.*;
import static dk.webbies.tscreate.util.Util.concat;
import static dk.webbies.tscreate.util.Util.toFixed;
import static java.util.Arrays.asList;

/**
 * Created by Erik Krogh Kristensen on 01-09-2015.
 */
public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        // TODO: Lower fixpoint != better result
        // TODO: Synopsis.tex, look at the 4 sent by Anders.
        // TODO: What can i do that TSCheck cannot. List the problems
        // TODO: The need for updated .d.ts files is huge, libraries are not small 1 man project, but big collabarative project.
        // TODO: What changes between version, look at a diff at a couple of libraries between the current version, and a version from 1-2 years ago.
        // TODO: Collateral Evolution

        // Benchmarks, where I can run ALL the static analysis methods.
        List<BenchMark> stableBenches = asList(async142, require, knockout, backbone, hammer, moment, handlebars30, underscore, please, path, p2, mathjax, materialize, photoswipe, peer, createjs, yui);
        List<Options.StaticAnalysisMethod> allMethods = asList(NONE, ANDERSON, MIXED, COMBINED, UPPER, UPPER_LOWER);//, ANDERSON_CONTEXT_SENSITIVE, MIXED_CONTEXT_SENSITIVE, COMBINED_CONTEXT_SENSITIVE, LOWER_CONTEXT_SENSITIVE, UPPER_LOWER_CONTEXT_SENSITIVE, UNIFICATION, UNIFICATION_CONTEXT_SENSITIVE);


        long start = System.currentTimeMillis();
        try {
            /*{
                PatchFile patchFile = PatchFileFactory.fromImplementation(backbone, backbone133);
                savePatchFile(patchFile, "diffViewer/backbone-10-13.json");
            }

            {
                PatchFile patchFile = PatchFileFactory.fromImplementation(async142, async201);
                savePatchFile(patchFile, "diffViewer/async-1-2.json");
            }

            {
                PatchFile patchFile = PatchFileFactory.fromImplementation(ember20, ember27);
                savePatchFile(patchFile, "diffViewer/ember-20-27.json");
            }

            {
                PatchFile patchFile = PatchFileFactory.fromImplementation(handlebars30, handlebars4);
                savePatchFile(patchFile, "diffViewer/handlebars-3-4.json");
            }

            {
                PatchFile patchFile = PatchFileFactory.fromImplementation(PIXI, PIXI_4_0);
                savePatchFile(patchFile, "diffViewer/pixi-3-4.json");
            }

            {
                PatchFile patchFile = PatchFileFactory.fromImplementation(ember1, ember20);
                savePatchFile(patchFile, "diffViewer/ember-1-2.json");
            }*/

            {
                PatchFile patchFile = PatchFileFactory.fromImplementation(moment, moment_214);
                savePatchFile(patchFile, "diffViewer/moment-to-214.json");
            }

//            runAnalysis(ember27);

//            PatchFile patchFromImplementation = PatchFileFactory.fromImplementation(PIXI, PIXI_4_0);

            /*PatchFile patchFromHandwritten = PatchFileFactory.fromHandwritten(PIXI, PIXI_4_0);

            savePatchFile(patchFromImplementation, "diffViewer/ember/diff.json");



            int adds = 0;
            int addsInOld = 0;
            int addsInNew = 0;

            int removals = 0;
            int removalsInOld = 0;
            int removalsInNew = 0;

            for (PatchStatement stmt : patchFromImplementation.getStatements()) {
                if (stmt instanceof AddedPropertyStatement) {
                    AddedPropertyStatement addedProp = (AddedPropertyStatement) stmt;
                    boolean inOld = PatchStatement.findInHandWritten(addedProp.getTypePath() + "." + addedProp.getPropertyName(), patchFromImplementation.getOldInfo()) != null;
                    boolean inOldContainer = PatchStatement.findInHandWritten(addedProp.getTypePath(), patchFromImplementation.getOldInfo()) != null;
                    boolean inNew = PatchStatement.findInHandWritten(addedProp.getTypePath() + "." + addedProp.getPropertyName(), patchFromImplementation.getNewInfo()) != null;
                    boolean inNewContainer = PatchStatement.findInHandWritten(addedProp.getTypePath(), patchFromImplementation.getNewInfo()) != null;

                    if (!inOldContainer || !inNewContainer) {
                        continue;
                    }

                    adds++;
                    addsInOld += inOld ? 1 : 0;
                    addsInNew += inNew ? 1 : 0;
                } else if (stmt instanceof RemovedPropertyStatement) {
                    RemovedPropertyStatement removedProp = (RemovedPropertyStatement) stmt;
                    boolean inOld = PatchStatement.findInHandWritten(removedProp.getTypePath() + "." + removedProp.getPropertyName(), patchFromImplementation.getOldInfo()) != null;
                    boolean inOldContainer = PatchStatement.findInHandWritten(removedProp.getTypePath(), patchFromImplementation.getOldInfo()) != null;
                    boolean inNew = PatchStatement.findInHandWritten(removedProp.getTypePath() + "." + removedProp.getPropertyName(), patchFromImplementation.getNewInfo()) != null;
                    boolean inNewContainer = PatchStatement.findInHandWritten(removedProp.getTypePath(), patchFromImplementation.getNewInfo()) != null;

                    if (!inOldContainer || !inNewContainer) {
                        continue;
                    }

                    removals++;
                    removalsInOld += inOld ? 1 : 0;
                    removalsInNew += inNew ? 1 : 0;
                }
            }

            System.out.println();*/


        } catch (Throwable e) {
            System.err.println("Crashed: ");

            e.printStackTrace(System.err);
        } finally {
            long end = System.currentTimeMillis();
            System.out.println("Ran in " + toFixed((end - start) / 1000.0, 1) + "s");

            System.exit(0);
        }
    }

    public static void savePatchFile(PatchFile patchFile, String path) throws IOException {
        String printed = patchFile.toJSON().toString();

        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)));
        writer.write(printed);
        writer.close();
    }

    public static Score makeSuperEval(BenchMark benchMark) throws IOException {
        if (benchMark.declarationPath == null) {
            throw new RuntimeException();
        }

        Set<String> resultingDeclarationFilePaths = new HashSet<>();
        for (Options.StaticAnalysisMethod method : Options.StaticAnalysisMethod.values()) {
            benchMark.getOptions().staticMethod = method;

            String resultDeclarationFilePath = getResultingDeclarationPath(benchMark);

            if (new File(resultDeclarationFilePath).exists()) {
                resultingDeclarationFilePaths.add(resultDeclarationFilePath);
            }
        }

        System.out.println("Getting a \"best\" possible evaluation from " + resultingDeclarationFilePaths.size() + " evaluations");

        FunctionExpression AST = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, getScript(benchMark)).toTSCreateAST();

        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.getOptions(), benchMark.dependencyScripts(), benchMark.testFiles, benchMark.getOptions().asyncTest), AST);
        Snap.Obj emptySnap = JSNAPUtil.getEmptyJSnap(benchMark.getOptions(), benchMark.dependencyScripts(), AST); // Not empty, just the one without the library we are analyzing.

        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObject, benchMark.getOptions()).extract();

        Evaluation evaluation = new DeclarationEvaluator(resultingDeclarationFilePaths, benchMark, globalObject, libraryClasses, benchMark.getOptions(), emptySnap).getEvaluation();

        System.out.println(evaluation);
        System.out.println("best        : " + Util.toFixed(evaluation.score().fMeasure, 4) + " - " + Util.toFixed(evaluation.score().recall, 4) + " - " + Util.toFixed(evaluation.score().precision, 4));

        return null;

    }

    public static Evaluation runAnalysis(BenchMark benchMark) throws IOException {
        String resultDeclarationFilePath = getResultingDeclarationPath(benchMark);

        System.out.println("Analysing " + benchMark.name + " - output: " + resultDeclarationFilePath);

        FunctionExpression AST = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, getScript(benchMark)).toTSCreateAST();

        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.getOptions(), benchMark.dependencyScripts(), benchMark.testFiles, benchMark.getOptions().asyncTest), AST);
        Snap.Obj emptySnap = JSNAPUtil.getEmptyJSnap(benchMark.getOptions(), benchMark.dependencyScripts(), AST); // Not empty, just the one without the library we are analyzing.

        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObject, benchMark.getOptions()).extract();

        NativeClassesMap nativeClasses = parseNatives(globalObject, benchMark.languageLevel.environment, benchMark.dependencyDeclarations(), libraryClasses, emptySnap);

        Map<String, DeclarationType> declaration = createDeclaration(benchMark, AST, globalObject, emptySnap, libraryClasses, nativeClasses);

        String printedDeclaration = new DeclarationPrinter(declaration, nativeClasses, benchMark.getOptions()).print();
        System.out.println(printedDeclaration);

        Util.writeFile(resultDeclarationFilePath, printedDeclaration);

        Evaluation evaluation = null;
        if (benchMark.declarationPath != null) {
            evaluation = new DeclarationEvaluator(resultDeclarationFilePath, benchMark, globalObject, libraryClasses, benchMark.getOptions(), emptySnap).getEvaluation();

            System.out.println(evaluation);
        }

        if (benchMark.getOptions().tsCheck) {
            System.out.println("TSCheck: ");
            System.out.println(Util.tsCheck(benchMark.scriptPath, resultDeclarationFilePath));
            System.out.println("----------");
        }

        if (evaluation == null) {
            return null;
        } else {
            String evaluationString = "\n\n/*\n" + evaluation.toString() + "\n*/\n";
            if (benchMark.getOptions().debugPrint) {
                evaluationString = "\n\n/*\n" + ((DebugEvaluation)evaluation).debugPrint() + "\n\n*/\n" + evaluationString;
            }
            Util.writeFile(resultDeclarationFilePath, printedDeclaration + evaluationString);
            return evaluation;
        }
    }

    public static Map<String, DeclarationType> createDeclaration(BenchMark benchMark) throws IOException {
        String resultDeclarationFilePath = getResultingDeclarationPath(benchMark);

        System.out.println("Analysing " + benchMark.name + " - output: " + resultDeclarationFilePath);

        FunctionExpression AST = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, getScript(benchMark)).toTSCreateAST();

        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.getOptions(), benchMark.dependencyScripts(), benchMark.testFiles, benchMark.getOptions().asyncTest), AST);
        Snap.Obj emptySnap = JSNAPUtil.getEmptyJSnap(benchMark.getOptions(), benchMark.dependencyScripts(), AST); // Not empty, just the one without the library we are analyzing.

        Map<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObject, benchMark.getOptions()).extract();

        NativeClassesMap nativeClasses = parseNatives(globalObject, benchMark.languageLevel.environment, benchMark.dependencyDeclarations(), libraryClasses, emptySnap);

        return createDeclaration(benchMark, AST, globalObject, emptySnap, libraryClasses, nativeClasses);
    }

    public static Map<String, DeclarationType> createDeclaration(BenchMark benchMark, FunctionExpression AST, Snap.Obj globalObject, Snap.Obj emptySnap, Map<Snap.Obj, LibraryClass> libraryClasses, NativeClassesMap nativeClasses) {
        Map<AstNode, Set<Snap.Obj>> callsites = Collections.EMPTY_MAP;
        if (benchMark.getOptions().recordCalls && globalObject.getProperty("__jsnap__callsitesToClosures") != null && benchMark.getOptions().useCallsiteInformation) {
            callsites = JSNAPUtil.getCallsitesToClosures((Snap.Obj) globalObject.getProperty("__jsnap__callsitesToClosures").value, AST);
        }

        TypeAnalysis typeAnalysis = createTypeAnalysis(benchMark, globalObject, libraryClasses, nativeClasses, callsites);
        typeAnalysis.analyseFunctions();

        Map<String, DeclarationType> declaration = new DeclarationBuilder(emptySnap, globalObject, typeAnalysis.getTypeFactory()).buildDeclaration();

        if (benchMark.getOptions().combineInterfacesAfterAnalysis) {
            System.out.println("Comnbining types");
            new RedundantInterfaceCleaner(declaration, nativeClasses, typeAnalysis.getTypeFactory().typeReducer).runHeuristics();
        } else {
            System.out.println("Cleaning up types");
            new RedundantInterfaceCleaner(declaration, nativeClasses, typeAnalysis.getTypeFactory().typeReducer).cleanDeclarations();
        }

        typeAnalysis.getTypeFactory().typeReducer.originals.clear();
        typeAnalysis.getTypeFactory().typeReducer.combinationTypeCache.clear();

        return declaration;
    }

    public static Score getScore(BenchMark benchMark) throws IOException {
        String resultDeclarationFilePath = getResultingDeclarationPath(benchMark);

        Score readFromFile = readScoreFromDeclaration(resultDeclarationFilePath);
        if (readFromFile != null) {
            return readFromFile;
        }

        Evaluation evaluation = getEvaluation(benchMark, getResultingDeclarationPath(benchMark));

        if (evaluation == null) {
            throw new RuntimeException();
        } else {
            return evaluation.score();
        }
    }

    static Evaluation getEvaluation(BenchMark benchMark, String generatedDeclarationPath) throws IOException {

        System.out.println("Get evaluation of " + benchMark.name + " - from: " + generatedDeclarationPath);

        if (!new File(generatedDeclarationPath).exists()) {
            return runAnalysis(benchMark);
        }

        FunctionExpression AST = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, getScript(benchMark)).toTSCreateAST();
        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.getOptions(), benchMark.dependencyScripts(), benchMark.testFiles, benchMark.getOptions().asyncTest), AST);
        Snap.Obj emptySnap = JSNAPUtil.getEmptyJSnap(benchMark.getOptions(), benchMark.dependencyScripts(), AST); // Not empty, just the one without the library we are analyzing.

        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObject, benchMark.getOptions()).extract();

        Evaluation evaluation = null;
        if (benchMark.declarationPath != null) {
            evaluation = new DeclarationEvaluator(generatedDeclarationPath, benchMark, globalObject, libraryClasses, benchMark.getOptions(), emptySnap).getEvaluation();
        }
        return evaluation;
    }

    private static Score readScoreFromDeclaration(String filePath) throws FileNotFoundException {
        if (!new File(filePath).exists()) {
            return null;
        }
        List<String> lines = new BufferedReader(new FileReader(new File(filePath))).lines().collect(Collectors.toList());
        Collections.reverse(lines);

        Double fMeasure = null;
        Double precision = null;
        Double recall = null;

        int counter = 0;
        for (String line : lines) {
            if (counter++ > 10) {
                break;
            }
            if (line.startsWith("Score: ")) {
                fMeasure = Double.parseDouble(line.substring("Score: ".length(), line.length()));
            }
            if (line.startsWith("Precision: ")) {
                precision = Double.parseDouble(line.substring("Precision: ".length(), line.length()));
            }
            if (line.startsWith("Recall: ")) {
                recall = Double.parseDouble(line.substring("Recall: ".length(), line.length()));
            }
        }
        if (fMeasure != null && precision != null && recall != null) {
            return new Score(fMeasure, precision, recall);
        } else {
            return null;
        }
    }

    public static String getResultingDeclarationPath(BenchMark benchMark) {
        Options options = benchMark.getOptions();
        String fileSuffix = options.staticMethod.fileSuffix;
        if (options.combineInterfacesAfterAnalysis) {
            fileSuffix += "_smaller";
        }
        switch (options.evaluationMethod) {
            case ONLY_FUNCTIONS:
                fileSuffix += "_evalFunc";
                break;
            case ONLY_HEAP:
                fileSuffix += "_evalHeap";
                break;
            case EVERYTHING: break;
            default:
                throw new RuntimeException();

        }
        if (options.disableFlowFromArgsToParams) {
            fileSuffix += "_noArgPar";
        }
        if (options.disableFlowFromParamsToArgs) {
            fileSuffix += "_noParArg";
        }
        if (options.disableFlowFromCallsiteToReturn) {
            fileSuffix += "_noCalRet";
        }
        if (options.disableFlowFromReturnToCallsite) {
            fileSuffix += "_noRetCal";
        }
        if (options.useJSDoc) {
            fileSuffix += "_jsDoc";
        }
        return benchMark.scriptPath + "." + fileSuffix + ".gen.d.ts";
    }

    private static TypeAnalysis createTypeAnalysis(BenchMark benchMark, Snap.Obj globalObject, Map<Snap.Obj, LibraryClass> libraryClasses, NativeClassesMap nativeClasses, Map<AstNode, Set<Snap.Obj>> callsites) {
        switch (benchMark.getOptions().staticMethod) {
            case MIXED:
                return new MixedTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, false, callsites);
            case UPPER:
                return new MixedTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, true, callsites);
            case ANDERSON:
                return new PureSubsetsTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, callsites);
            case UNIFICATION:
                return new UnionEverythingTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, callsites);
            case UNIFICATION_CONTEXT_SENSITIVE:
                return new UnionRecursivelyTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, callsites);
            case OLD_UNIFICATION_CONTEXT_SENSITIVE:
                return new OldTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses);
            case OLD_UNIFICATION:
                return new OldTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses);
            case COMBINED:
                return new CombinedTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, false, callsites);
            case UPPER_LOWER:
                return new CombinedTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, true, callsites);
            case MIXED_CONTEXT_SENSITIVE:
                return new MixedContextSensitiveTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, false, callsites);
            case LOWER_CONTEXT_SENSITIVE:
                return new MixedContextSensitiveTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, true, callsites);
            case ANDERSON_CONTEXT_SENSITIVE:
                return new PureSubsetsContextSensitiveTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, callsites);
            case COMBINED_CONTEXT_SENSITIVE:
                return new CombinedContextSensitiveTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, false, callsites);
            case UPPER_LOWER_CONTEXT_SENSITIVE:
                return new CombinedContextSensitiveTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, true, callsites);
            case NONE:
                return new NoTypeAnalysis(libraryClasses, benchMark.getOptions(), globalObject, nativeClasses, false, callsites);
            default:
                throw new RuntimeException("I don't even know this static analysis method. ");
        }
    }

    public static String getScript(BenchMark benchMark) throws IOException {
        StringBuilder result = new StringBuilder();
        result.append(Util.readFile(benchMark.scriptPath)).append("\n");
        for (String testFile : benchMark.testFiles) {
            result.append(Util.readFile(testFile)).append("\n");
        }
        return result.toString();
    }

    public static void tsCheck() throws IOException {
        for (BenchMark benchmark : allBenchmarks) {
            benchmark.getOptions().tsCheck = true;
            runAnalysis(benchmark);
        }
    }

    private static void benchAll() throws IOException {
        double combinedScore = 0;
        for (BenchMark benchmark : allBenchmarks) {
            double score = runAnalysis(benchmark).score().fMeasure;
            if (!Double.isNaN(score)) {
                combinedScore += score;
            }
        }

        System.out.println("\nCombined score: " + combinedScore);
    }

    private static void generateDeclarations(List<BenchMark> benchMarks) throws IOException {
        if (benchMarks == null) {
            benchMarks = allBenchmarks;
        }
        for (BenchMark benchmark : benchMarks) {
            benchmark.declarationPath = null;
            runAnalysis(benchmark);
        }

        System.out.println("Generated all declarations");
    }

    private static void printTable() throws IOException {
        Map<BenchMark, Score> scores = new HashMap<>();
        for (BenchMark benchmark : allBenchmarks) {
            if (benchmark.declarationPath == null || benchmark == test) {
                continue;
            }
            Score score = runAnalysisWithTimeout(benchmark, Long.MAX_VALUE);
            if (score == null) {
                scores.put(benchmark, new Score(-1, -1, -1));
            } else {
                scores.put(benchmark, score);
            }
        }
        System.out.print("\n\n\n\n\n");
        System.out.println("\\begin{table}[]\n" +
                "\\centering\n" +
                "\\begin{tabular}{l|l|ccc}\n" +
                "& & \\multicolumn{3}{c}{\\textit{score}}  \\\\\n" +
                "\\textit{benchmark} & \\textit{lines of code} & \\multicolumn{1}{l}{\\textit{f-measure}} & \\multicolumn{1}{l}{\\textit{recall}} & \\multicolumn{1}{l}{\\textit{precision}} \\\\ \\hline");
        List<BenchMark> benches = scores.keySet().stream().sorted((o1, o2) -> o1.name.compareTo(o2.name)).collect(Collectors.toList());
        for (int i = 0; i < benches.size(); i++) {
            BenchMark benchMark = benches.get(i);
            String fMeasure = toFixed(scores.get(benchMark).fMeasure, 3);
            String precision = toFixed(scores.get(benchMark).precision, 3);
            String recall = toFixed(scores.get(benchMark).recall, 3);
            System.out.print(benchMark.name + " & " + Util.lines(benchMark.scriptPath) + " & " + fMeasure + " & " + precision + " & " + recall);
            if (i != benches.size() - 1) {
                System.out.print(" \\\\");
            }
            System.out.print("\n");
        }

        System.out.println("\\end{tabular}\n" +
                "\\caption{Evaluation of benchmarks} \\label{evaluationTable}\n" +
                "\\end{table}");

        System.out.println("\n\n");
        double sumScore = 0;
        for (Score score : scores.values()) {
            sumScore += score.fMeasure;
        }
        System.out.println("Combined score: " + sumScore);

    }

    public static Score runAnalysisWithTimeout(BenchMark benchMark, long timeout) {
        AtomicReference<Score> result = new AtomicReference<>(null);
        Thread benchThread = new Thread(() -> {
            try {
                result.set(runAnalysis(benchMark).score());
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
        benchThread.start();
        Thread killThread = new Thread(() -> {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                return;
            }
            if (result.get() != null) {
                return;
            }
            if (benchThread.isAlive()) {
                System.err.println("Stopping benchmark because of timeout.");
                //noinspection deprecation
                benchThread.stop(); // <- Deprecated, and i know it.
            }
        });
        killThread.start();
        try {
            benchThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        killThread.interrupt();
        return result.get();
    }

    public enum LanguageLevel {
        ES5(Parser.Config.Mode.ES5, Environment.ES5DOM),
        ES6(Parser.Config.Mode.ES6, Environment.ES6DOM);

        public final Parser.Config.Mode closureCompilerMode;
        public final Environment environment;

        LanguageLevel(Parser.Config.Mode closure, Environment declaration) {
            this.closureCompilerMode = closure;
            this.environment = declaration;
        }
    }
}
