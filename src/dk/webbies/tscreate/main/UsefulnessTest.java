package dk.webbies.tscreate.main;

import dk.webbies.tscreate.BenchMark;
import dk.webbies.tscreate.analysis.declarations.DeclarationPrinter;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.ClassHierarchyExtractor;
import dk.webbies.tscreate.main.normalizeDec.NormalizeDeclaration;
import dk.webbies.tscreate.main.patch.PatchFileFactory;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.paser.JavaScriptParser;
import dk.webbies.tscreate.util.Pair;
import dk.au.cs.casa.typescript.SpecReader;
import dk.au.cs.casa.typescript.types.InterfaceType;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.util.Tuple3;
import dk.webbies.tscreate.util.Util;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.BenchMark.*;
import static dk.webbies.tscreate.Options.StaticAnalysisMethod.*;
import static dk.webbies.tscreate.declarationReader.DeclarationParser.parseNatives;
import static dk.webbies.tscreate.main.PrecisionTest.TestType.*;
import static dk.webbies.tscreate.main.patch.PatchFileFactory.getInfo;

/**
 * Created by erik1 on 25-08-2016.
 */
@SuppressWarnings("Duplicates")
public class UsefulnessTest {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException, InterruptedException {
        // Fabric, jQuery, leaflet, jasmine24 doesn't work (inconsistent class hierarchy).
        for (BenchMark benchMark : Arrays.asList(ace, angular, async201, backbone133, D3, ember27, FabricJS15, hammer, handlebars4, jasmine22, jQuery, knockout, leaflet, moment_214, PIXI_4_0, polymer11, react014, three, underscore17, vue)) {
            runForBench(benchMark, 50);
            printPrecRecall("field existence", fieldExistence);
            printPrecRecall("class method existence", classMethodExistence);
            printPrecRecall("class existence", classExistence);
            printPrecRecall("module existence", moduleExistence);
            printPrecRecall("heap prop existence", heapPropExistence);
        }

        printPrecRecall("field existence", fieldExistence);
        printPrecRecall("class method existence", classMethodExistence);
        printPrecRecall("class existence", classExistence);
        printPrecRecall("module existence", moduleExistence);
        printPrecRecall("heap prop existence", heapPropExistence);

        System.exit(0);
    }

    public static Map<BenchMark, Tuple3<Integer, Integer, Integer>> fieldExistence = new HashMap<>();
    public static Map<BenchMark, Tuple3<Integer, Integer, Integer>> classMethodExistence = new HashMap<>();
    public static Map<BenchMark, Tuple3<Integer, Integer, Integer>> classExistence = new HashMap<>();
    public static Map<BenchMark, Tuple3<Integer, Integer, Integer>> moduleExistence = new HashMap<>();
    public static Map<BenchMark, Tuple3<Integer, Integer, Integer>> heapPropExistence = new HashMap<>();

    private static void printPrecRecall(String title, Map<BenchMark, Tuple3<Integer, Integer, Integer>> map) {
        if (map.isEmpty()) {
            return;
        }

        System.out.println(title + "\tTrue positives\tFalse positives\tFalse negatives\tPrecision\tRecall");
        int truePositives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;
        for (Map.Entry<BenchMark, Tuple3<Integer, Integer, Integer>> entry : map.entrySet().stream().sorted((a, b) -> a.getKey().name.compareTo(b.getKey().name)).collect(Collectors.toList())) {
            Tuple3<Integer, Integer, Integer> tuple = entry.getValue();

            Double precision = (1.0 * tuple.a) / (tuple.a + tuple.b);
            Double recall = (1.0 * tuple.a) / (tuple.a + tuple.c);

            System.out.println(entry.getKey().name + "\t" + tuple.a + "\t" + tuple.b  + "\t" + tuple.c  + "\t" + Util.toFixed(precision, 4, ',')  + "\t" + Util.toFixed(recall, 4, ','));

            truePositives += tuple.a;
            falsePositives += tuple.b;
            falseNegatives += tuple.c;
        }

        Double precision = (1.0 * truePositives) / (truePositives + falsePositives);
        Double recall = (1.0 * truePositives) / (truePositives + falseNegatives);

        System.out.println("Total\t" + truePositives + "\t" + falsePositives + "\t" + falseNegatives + "\t" + Util.toFixed(precision, 4, ',') + "\t" + Util.toFixed(recall, 4, ','));

        System.out.println("\n");
    }



    public enum TestType {
        MODULES,
        FIELDS,
        CONSTRUCTORS,
        FUNCTIONS
    }

    public static void runForBench(BenchMark benchMark, int maxSamples) throws IOException, InterruptedException {
        System.out.println("Running benchmark: " + benchMark.name);
        PatchFileFactory.BenchmarkInformation two = getInfoFromHandwritten(benchMark);

        benchMark.resetOptions();
        CompareMethods.Config tsInfer = new CompareMethods.Config("TSInfer", (options) -> {
            options.staticMethod = UPPER_LOWER;
            options.useJSDoc = false;
//            options.recordCalls = false;
//            options.createInstances = false;
//            options.classOptions.useInstancesForThis = false;
        });

        tsInfer.applier.accept(benchMark.getOptions());

        PatchFileFactory.BenchmarkInformation one = getInfo(benchMark);

//        DeclarationPrinter printer = NormalizeDeclaration.generateDeclarationPrinterFromHandwritten(benchMark, globalObject, nativeClasses, typeNames, spec);

        PrecisionTest.FeatureExtractor extractor = new PrecisionTest.FeatureExtractor();
        for (String key : Util.intersection(one.globalObject.getDeclarations().keySet(), two.globalObject.getDeclarations().keySet())) {
            one.globalObject.getDeclarations().get(key).accept(extractor, new PrecisionTest.Arg(two.globalObject.getDeclarations().get(key), "window." + key, 1));
        }
        extractor.modules.put("window", new Pair<>(one.globalObject, two.globalObject));
        extractor.finish();


        testFieldExistence(extractor, benchMark);

        testHeapPropsExistence(extractor, benchMark);

        testClassMethodExistence(extractor, benchMark);

        testClassExistence(extractor, benchMark);

        testModuleExistence(extractor, benchMark);

        Map<String, Pair<DeclarationType, DeclarationType>> toCompare;
        for (PrecisionTest.TestType testType : Arrays.asList(FIELDS, MODULES, CONSTRUCTORS, FUNCTIONS)) {
            switch (testType) {
                case CONSTRUCTORS: toCompare = extractor.constructors; break;
                case FIELDS: toCompare = extractor.fields; break;
                case FUNCTIONS: toCompare = extractor.functions; break;
                case MODULES: toCompare = extractor.modules; break;
                default:
                    throw new RuntimeException("What!");
            }

            System.out.println("Comparing: " + testType.name());

            compareMethods(toCompare, one, two, maxSamples, benchMark, testType);

            System.out.println("Press enter to continue");
            scanner.nextLine();
        }
    }

    private static void testClassExistence(PrecisionTest.FeatureExtractor extractor, BenchMark benchMark) {
        System.out.println("Testing recall and precision for existence of classes");

        List<Pair<UnnamedObjectType, UnnamedObjectType>> objects = new ArrayList<>();
        extractor.modules.values().forEach(pair -> objects.add(new Pair<>((UnnamedObjectType)pair.getLeft(), (UnnamedObjectType)pair.getRight())));
        extractor.classes.forEach((path, pair) -> {
            ClassType inferred = (ClassType) pair.getLeft();
            ClassType handwritten = (ClassType) pair.getRight();
            objects.add(new Pair<>(new UnnamedObjectType(inferred.getStaticFields(), Collections.EMPTY_SET), new UnnamedObjectType(handwritten.getStaticFields(), Collections.EMPTY_SET)));
        });

        int truePositives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;

        for (Pair<UnnamedObjectType, UnnamedObjectType> pair : objects) {
            UnnamedObjectType inferred = pair.getLeft();
            UnnamedObjectType handwritten = pair.getRight();

            Set<String> inferredFields = inferred.getDeclarations().entrySet().stream().filter(entry -> entry.getValue().resolve() instanceof ClassType).map(Map.Entry::getKey).collect(Collectors.toSet());
            Set<String> handwrittenFields = handwritten.getDeclarations().entrySet().stream().filter(entry -> entry.getValue().resolve() instanceof ClassType).map(Map.Entry::getKey).collect(Collectors.toSet());
            int shared = Util.intersection(inferredFields, handwrittenFields).size();
            truePositives += shared;

            falseNegatives += (handwrittenFields.size()) - shared;
            falsePositives += (inferredFields.size()) - shared;
        }

        classExistence.put(benchMark, new Tuple3<>(truePositives, falsePositives, falseNegatives));

        System.out.println("For benchmark " + benchMark.name + " testing the existence of classes");
        System.out.println("True positives: " + truePositives);
        System.out.println("False positives: " + falsePositives);
        System.out.println("False negatives: " + falseNegatives);
//        System.out.println("Press enter to continue");
//        scanner.nextLine();
    }

    private static void testModuleExistence(PrecisionTest.FeatureExtractor extractor, BenchMark benchMark) {
        System.out.println("Testing recall and precision for existence of modules");

        List<Pair<UnnamedObjectType, UnnamedObjectType>> objects = new ArrayList<>();
        extractor.modules.values().forEach(pair -> objects.add(new Pair<>((UnnamedObjectType)pair.getLeft(), (UnnamedObjectType)pair.getRight())));
        extractor.classes.forEach((path, pair) -> {
            ClassType inferred = (ClassType) pair.getLeft();
            ClassType handwritten = (ClassType) pair.getRight();
            objects.add(new Pair<>(new UnnamedObjectType(inferred.getStaticFields(), Collections.EMPTY_SET), new UnnamedObjectType(handwritten.getStaticFields(), Collections.EMPTY_SET)));
        });

        int truePositives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;

        for (Pair<UnnamedObjectType, UnnamedObjectType> pair : objects) {
            UnnamedObjectType inferred = pair.getLeft();
            UnnamedObjectType handwritten = pair.getRight();

            Set<String> inferredFields = inferred.getDeclarations().entrySet().stream().filter(entry -> entry.getValue().resolve() instanceof UnnamedObjectType).map(Map.Entry::getKey).collect(Collectors.toSet());
            Set<String> handwrittenFields = handwritten.getDeclarations().entrySet().stream().filter(entry -> entry.getValue().resolve() instanceof UnnamedObjectType).map(Map.Entry::getKey).collect(Collectors.toSet());
            int shared = Util.intersection(inferredFields, handwrittenFields).size();
            truePositives += shared;

            falseNegatives += (handwrittenFields.size()) - shared;
            falsePositives += (inferredFields.size()) - shared;
        }

        moduleExistence.put(benchMark, new Tuple3<>(truePositives, falsePositives, falseNegatives));

        System.out.println("For benchmark " + benchMark.name + " testing the existence of modules");
        System.out.println("True positives: " + truePositives);
        System.out.println("False positives: " + falsePositives);
        System.out.println("False negatives: " + falseNegatives);
//        System.out.println("Press enter to continue");
//        scanner.nextLine();
    }


    private static void testHeapPropsExistence(PrecisionTest.FeatureExtractor extractor, BenchMark benchMark) {
        System.out.println("Testing recall and precision for existence of heap properties (classes etc). ");

        List<Pair<UnnamedObjectType, UnnamedObjectType>> objects = new ArrayList<>();
        extractor.modules.values().forEach(pair -> objects.add(new Pair<>((UnnamedObjectType)pair.getLeft(), (UnnamedObjectType)pair.getRight())));
        extractor.classes.forEach((path, pair) -> {
            ClassType inferred = (ClassType) pair.getLeft();
            ClassType handwritten = (ClassType) pair.getRight();

            if (!inferred.getStaticFields().isEmpty() || !handwritten.getStaticFields().isEmpty()) {
                objects.add(new Pair<>(new UnnamedObjectType(inferred.getStaticFields(), Collections.EMPTY_SET), new UnnamedObjectType(handwritten.getStaticFields(), Collections.EMPTY_SET)));
            }
        });

        int truePositives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;

        for (Pair<UnnamedObjectType, UnnamedObjectType> pair : objects) {
            UnnamedObjectType inferred = pair.getLeft();
            UnnamedObjectType handwritten = pair.getRight();

            Set<String> inferredFields = inferred.getDeclarations().keySet();
            Set<String> handwrittenFields = handwritten.getDeclarations().keySet();
            int shared = Util.intersection(inferredFields, handwrittenFields).size();
            truePositives += shared;

            falseNegatives += (handwrittenFields.size()) - shared;
            falsePositives += (inferredFields.size()) - shared;
        }

        heapPropExistence.put(benchMark, new Tuple3<>(truePositives, falsePositives, falseNegatives));

        System.out.println("For benchmark " + benchMark.name + " testing the existence of heap properties");
        System.out.println("True positives: " + truePositives);
        System.out.println("False positives: " + falsePositives);
        System.out.println("False negatives: " + falseNegatives);
//        System.out.println("Press enter to continue");
//        scanner.nextLine();
    }

    public static void testClassMethodExistence(PrecisionTest.FeatureExtractor extractor, BenchMark benchMark) {
        System.out.println("Testing recall and precision for existence of class methods. ");
        int truePositives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;
        Set<ClassType> seen = new HashSet<>();
        for (Pair<DeclarationType, DeclarationType> pair : extractor.classes.values()) {
            ClassType inferred = (ClassType) pair.getLeft();
            ClassType handwritten = (ClassType) pair.getRight();
            if (seen.contains(inferred) || seen.contains(handwritten)) {
                continue;
            }
            seen.add(inferred);
            seen.add(handwritten);

            int thisTruePositives = 0;
            int thisFalsePositives = 0;
            int thisFalseNegatives = 0;

            Set<String> inferredFields = inferred.getPrototypeFields().entrySet().stream().filter(entry -> (entry.getValue() instanceof FunctionType)).map(Map.Entry::getKey).collect(Collectors.toSet());
            Set<String> handwrittenFields = handwritten.getPrototypeFields().entrySet().stream().filter(entry -> (entry.getValue() instanceof FunctionType)).map(Map.Entry::getKey).collect(Collectors.toSet());

            Set<String> inferredFieldsWithSuperClasses = getFieldsWithSuper(inferred);

            Set<String> handwrittenWithSuperClasses = getFieldsWithSuper(handwritten);

            for (String field : Util.concat(inferredFields, handwrittenFields).stream().distinct().collect(Collectors.toList())) {
                if (!handwrittenWithSuperClasses.contains(field)) {
                    thisFalsePositives++;
                } else {
                    if (inferredFieldsWithSuperClasses.contains(field)) {
                        thisTruePositives++;
                    } else {
                        thisFalseNegatives++;
                    }
                }
            }

            truePositives += thisTruePositives;
            falsePositives += thisFalsePositives;
            falseNegatives += thisFalseNegatives;
        }

        classMethodExistence.put(benchMark, new Tuple3<>(truePositives, falsePositives, falseNegatives));

        System.out.println("For benchmark " + benchMark.name + " testing the existence of methods on classes");
        System.out.println("True positives: " + truePositives);
        System.out.println("False positives: " + falsePositives);
        System.out.println("False negatives: " + falseNegatives);

//        System.out.println("Press enter to continue");
//        scanner.nextLine();
    }

    public static List<String> fieldFalsePositives = new ArrayList<>();

    public static void testFieldExistence(PrecisionTest.FeatureExtractor extractor, BenchMark benchMark) {
        System.out.println("Testing recall and precision for existence of fields. ");
        int truePositives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;
        Set<ClassType> seen = new HashSet<>();
        for (Pair<DeclarationType, DeclarationType> pair : extractor.classes.values()) {
            ClassType inferred = (ClassType) pair.getLeft();
            ClassType handwritten = (ClassType) pair.getRight();
            if (seen.contains(inferred) || seen.contains(handwritten)) {
                continue;
            }
            seen.add(inferred);
            seen.add(handwritten);

            int thisTruePositives = 0;
            int thisFalsePositives = 0;
            int thisFalseNegatives = 0;

            Set<String> inferredFields = inferred.getPrototypeFields().entrySet().stream().filter(entry -> !(entry.getValue() instanceof FunctionType)).map(Map.Entry::getKey).collect(Collectors.toSet());
            Set<String> handwrittenFields = handwritten.getPrototypeFields().entrySet().stream().filter(entry -> !(entry.getValue() instanceof FunctionType)).map(Map.Entry::getKey).collect(Collectors.toSet());

            Set<String> inferredFieldsWithSuperClasses = getFieldsWithSuper(inferred);

            Set<String> handwrittenWithSuperClasses = getFieldsWithSuper(handwritten);

            for (String field : Util.concat(inferredFields, handwrittenFields).stream().distinct().collect(Collectors.toList())) {
                if (!handwrittenWithSuperClasses.contains(field)) {
                    fieldFalsePositives.add(field);
                    thisFalsePositives++;
                } else {
                    if (inferredFieldsWithSuperClasses.contains(field)) {
                        thisTruePositives++;
                    } else {
                        thisFalseNegatives++;
                    }
                }
            }

            truePositives += thisTruePositives;
            falsePositives += thisFalsePositives;
            falseNegatives += thisFalseNegatives;

        }
        fieldExistence.put(benchMark, new Tuple3<>(truePositives, falsePositives, falseNegatives));

        System.out.println("For benchmark " + benchMark.name + " testing the existence of fields");
        System.out.println("True positives: " + truePositives);
        System.out.println("False positives: " + falsePositives);
        System.out.println("False negatives: " + falseNegatives);

//        System.out.println("Press enter to continue");
//        scanner.nextLine();
    }

    private static Set<String> getFieldsWithSuper(ClassType clazz) {
        Set<String> keys = new HashSet<>();
        while (clazz != null) {
            keys.addAll(clazz.getPrototypeFields().keySet());
            DeclarationType superClass = clazz.getSuperClass();
            if (superClass instanceof ClassType) {
                clazz = (ClassType) superClass;
            } else {
                break;
            }
        }

        return keys;
    }

    public static PatchFileFactory.BenchmarkInformation getInfoFromHandwritten(BenchMark benchMark) throws IOException {
        SpecReader spec = DeclarationParser.getTypeSpecification(benchMark.languageLevel.environment, benchMark.dependencyDeclarations(), benchMark.declarationPath);

        Map<Type, String> typeNames = NormalizeDeclaration.getTypeNames(spec, benchMark);

        SpecReader emptySpec = DeclarationParser.getTypeSpecification(benchMark.languageLevel.environment, benchMark.dependencyDeclarations());
        Set<String> existingKeys = ((InterfaceType) emptySpec.getGlobal()).getDeclaredProperties().keySet();

        InterfaceType global = (InterfaceType)spec.getGlobal();

        global.setDeclaredProperties(global.getDeclaredProperties().entrySet().stream().filter(entry -> !existingKeys.contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        FunctionExpression AST = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, Main.getScript(benchMark)).toTSCreateAST();
        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.getOptions(), benchMark.dependencyScripts(), benchMark.testFiles, benchMark.getOptions().asyncTest), AST);
        Snap.Obj emptySnap = JSNAPUtil.getEmptyJSnap(benchMark.getOptions(), benchMark.dependencyScripts(), AST); // Not empty, just the one without the library we are analyzing.

        DeclarationParser.NativeClassesMap nativeClasses = parseNatives(globalObject, benchMark.languageLevel.environment, benchMark.dependencyDeclarations(), new ClassHierarchyExtractor(globalObject, benchMark.getOptions()).extract(), emptySnap);

        DeclarationPrinter printer = NormalizeDeclaration.generateDeclarationPrinterFromHandwritten(benchMark, globalObject, nativeClasses, typeNames, spec, global);

        return new PatchFileFactory.BenchmarkInformation(benchMark, new UnnamedObjectType(printer.declarations, Collections.EMPTY_SET), printer, printer.print(), nativeClasses, spec.getGlobal(), globalObject);
    }

    private static void compareMethods(Map<String, Pair<DeclarationType, DeclarationType>> types, PatchFileFactory.BenchmarkInformation one, PatchFileFactory.BenchmarkInformation two, int maxSamples, BenchMark benchMark, PrecisionTest.TestType testType) {
        ArrayList<String> paths = pickSamples(types, maxSamples);

        int perfect = 0;
        int close = 0;
        int any = 0;
        int wayoff = 0;
        int unknown = 0;
        int missingArgs = 0;

        for (int i = 0; i < paths.size(); i++) {
            String path = paths.get(i);
            DeclarationType oneType = types.get(path).getLeft();
            DeclarationType twoType = types.get(path).getRight();

            String oneString = one.printer.printType(oneType, path);
            String twoString = two.printer.printType(twoType, path);


            printTwoTypes(paths, i, path, oneString, twoString);
//            oneString = one.printer.printType(oneType, path);
//            twoString = one.printer.printType(twoType, path);

            int in = 0;
            if (oneString.trim().equals(twoString.trim())) {
                System.out.println("They were textually equal, registering them as being the same, next!");
                in = 1;
            }
            while (1 > in || in > 6) {
                try {
                    in = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.err.println(e.toString());
                    in = 0;
                }
                if (in == 7) {
                    System.out.println("----- Start Inferred declaration -----: ");
                    System.out.println(one.printedDeclaration);
                    System.out.println("----- End Inferred declaration -----: ");
                    printTwoTypes(paths, i, path, oneString, twoString);
                    continue;
                }
                if (in == 8) {
                    System.out.println("----- Start Handwritten declaration -----: ");
                    System.out.println(two.printedDeclaration);
                    System.out.println("----- End Handwritten declaration -----: ");
                    printTwoTypes(paths, i, path, oneString, twoString);
                    continue;
                }
                if (1 > in || in > 6) {
                    System.out.println("Bad dog, try again. A number between 1 and 6");
                }
            }

            switch (in) {
                case 1: perfect++; break;
                case 2: close++; break;
                case 3: any++; break;
                case 4: wayoff++; break;
                case 5: unknown++; break;
                case 6: missingArgs++; break;
                default:
                    throw new RuntimeException("ioungbiubg");
            }
        }

        System.out.println("For benchmark " + benchMark.name + " testtype: " + testType);
        System.out.println("Perfect: " + perfect);
        System.out.println("Close: " + close);
        System.out.println("Any: " + any);
        System.out.println("Wayoff: " + wayoff);
        System.out.println("MissingArgs: " + missingArgs);
        System.out.println("Unknown: " + unknown);
    }

    private static void printTwoTypes(ArrayList<String> paths, int i, String path, String oneString, String twoString) {
        System.out.println("----- Compare the below two types (" + i + "/" + paths.size() + ") -----");
        System.out.println("Path: " + path);

        System.out.println("Inferred type: ");

        System.out.println(oneString);

        System.out.println("\nHandwritten declaration: ");

        System.out.println(twoString);

        System.out.println("\nHow good was it, 1 for perfect, 2 for good, 3 for \"any\", 4 for way off, 5 for cannot be categorized, 6 for missing arguments. 7 For print the inferred declaration, 8 for print the handwritten declaration");
    }

    private static ArrayList<String> pickSamples(Map<String, Pair<DeclarationType, DeclarationType>> types, int maxSamples) {
        ArrayList<String> paths = new ArrayList<>(types.keySet());
        Collections.shuffle(paths);

        if (paths.size() > maxSamples) {
            ArrayList<String> newPaths = new ArrayList<>();
            for (int i = 0; i < maxSamples; i++) {
                newPaths.add(paths.get(i));
            }
            paths = newPaths;
        }
        return paths;
    }
}
