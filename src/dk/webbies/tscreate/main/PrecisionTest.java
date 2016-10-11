package dk.webbies.tscreate.main;

import dk.webbies.tscreate.BenchMark;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.util.Pair;
import dk.webbies.tscreate.util.Util;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

import static dk.webbies.tscreate.BenchMark.*;
import static dk.webbies.tscreate.Options.StaticAnalysisMethod.*;
import static dk.webbies.tscreate.main.PrecisionTest.TestType.*;
import static dk.webbies.tscreate.main.patch.PatchFileFactory.*;

/**
 * Created by erik1 on 23-08-2016.
 */
public class PrecisionTest {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException, InterruptedException {
        for (BenchMark benchMark : Arrays.asList(ace, angular, async201, backbone133, D3, /*ember27, FabricJS16, */hammer, handlebars4, jQuery, knockout, leaflet, moment_214, vue, jasmine24, PIXI_4_0, /*react014, */polymer11, /*three, */underscore17)) {
            runForBench(benchMark, 50);
        }


        System.exit(0);
    }

    public enum TestType {
        MODULES,
        FIELDS,
        CONSTRUCTORS,
        FUNCTIONS;
    }

    public static void runForBench(BenchMark benchMark, int maxSamples) throws IOException, InterruptedException {
        benchMark.resetOptions();
        CompareMethods.Config tsInfer = new CompareMethods.Config("TSInfer", (options) -> {
            options.staticMethod = UPPER_LOWER;
            options.useJSDoc = false;
//            options.recordCalls = false;
//            options.createInstances = false;
//            options.createInstances = false;
//            options.classOptions.useInstancesForThis = false;
        });
        CompareMethods.Config tsCheck = new CompareMethods.Config("TSCheck", (options) -> {
            options.staticMethod = UNIFICATION_CONTEXT_SENSITIVE;
            options.useJSDoc = false;
            options.recordCalls = false;
            options.createInstances = false;
            options.classOptions.useInstancesForThis = false;
            options.classOptions.unionThisFromConstructor = false;
            options.classOptions.useClassInstancesFromHeap = false;
        });

        tsInfer.applier.accept(benchMark.getOptions());

        BenchmarkInformation one = getInfo(benchMark);

        benchMark.resetOptions();

        tsCheck.applier.accept(benchMark.getOptions());

        BenchmarkInformation two = getInfo(benchMark);

        FeatureExtractor extractor = new FeatureExtractor();
        for (String key : Util.intersection(one.globalObject.getDeclarations().keySet(), two.globalObject.getDeclarations().keySet())) {
            one.globalObject.getDeclarations().get(key).accept(extractor, new Arg(two.globalObject.getDeclarations().get(key), "window." + key, 1));
        }
        extractor.finish();
        Map<String, Pair<DeclarationType, DeclarationType>> toCompare;
        for (TestType testType : Arrays.asList(CONSTRUCTORS, FUNCTIONS/*, MODULES, FIELDS*/)) {
            switch (testType) {
                case CONSTRUCTORS: toCompare = extractor.constructors; break;
                case FIELDS: toCompare = extractor.fields; break;
                case FUNCTIONS: toCompare = extractor.functions; break;
                case MODULES: toCompare = extractor.modules; break;
                default:
                    throw new RuntimeException("What!");
            }

            System.out.println("Comparing: " + testType.name());

            compareMethods(toCompare, tsInfer.prettyString, tsCheck.prettyString, one, two, maxSamples, benchMark, testType);

            System.out.println("Press enter to continue");
            scanner.nextLine();
        }
    }

    private static void compareMethods(Map<String, Pair<DeclarationType, DeclarationType>> types, String firstMethod, String secondMethod, BenchmarkInformation one, BenchmarkInformation two, int maxSamples, BenchMark benchMark, TestType testType) {
        ArrayList<String> paths = new ArrayList<>(types.keySet());
        Collections.shuffle(paths);

        if (paths.size() > maxSamples) {
            ArrayList<String> newPaths = new ArrayList<>();
            for (int i = 0; i < maxSamples; i++) {
                newPaths.add(paths.get(i));
            }
            paths = newPaths;
        }

        Random rng = new SecureRandom();

        int firstWon = 0;
        int secondWon = 0;
        int same = 0;
        int unable = 0;

        for (int i = 0; i < paths.size(); i++) {
            String path = paths.get(i);

            DeclarationType oneType = types.get(path).getLeft();
            DeclarationType twoType = types.get(path).getRight();

            String oneString = one.printer.printType(oneType, path);  // TSInfer
            String twoString = two.printer.printType(twoType, path);  // TSCheck

            boolean oneFirst = rng.nextBoolean();

            printQuestion(oneString, twoString, oneFirst, path, i, paths.size());
//            oneString = one.printer.printType(oneType, path);
//            twoString = one.printer.printType(twoType, path);

            int in = 0;
            if (oneString.trim().equals(twoString.trim())) {
                System.out.println("They were textually equal, registering them as being the same, next!");
                in = 3;
            }
            while (in != 1 && in != 2 && in != 3 && in != 4 && in != 5) {
                try {
                    in = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.err.println(e.toString());
                    in = 0;
                }
                if (in == 6) {
                    String declaration = oneFirst ? one.printedDeclaration : two.printedDeclaration;
                    System.out.println(declaration);

                    printQuestion(oneString, twoString, oneFirst, path, i, paths.size());

                    continue;
                }
                if (in == 7) {
                    String declaration = oneFirst ? two.printedDeclaration : one.printedDeclaration;
                    System.out.println(declaration);

                    printQuestion(oneString, twoString, oneFirst, path, i, paths.size());

                    continue;
                }

                if (in != 1 && in != 2 && in != 3 && in != 4 && in != 5) {
                    System.out.println("Bad dog, try again. A number between 1 and 5");
                }
            }

            if (in == 3) {
                same++;
                continue;
            }
            if (in == 4) {
                unable++;
                continue;
            }
            if (in == 5) {
                break;
            }

            boolean tscheckWon;

            if (in == 1) {
                if (oneFirst) {
                    tscheckWon = false;
                    firstWon++;
                } else {
                    tscheckWon = true;
                    secondWon++;
                }
            } else {
                if (oneFirst) {
                    tscheckWon = true;
                    secondWon++;
                } else {
                    tscheckWon = false;
                    firstWon++;
                }
            }

            if (tscheckWon) {
                System.out.println("TSCheck was best, try to observe why.");
                System.out.println("Press enter to continue");
                scanner.nextLine();
            } else {
                System.out.println("TSInfer won, press enter to continue");
                scanner.nextLine();
            }
        }

        System.out.println("For benchmark " + benchMark.name + " and testType " + testType.name());
        System.out.println(firstMethod + " was best " + firstWon + " times");
        System.out.println(secondMethod + " was best " + secondWon + " times");
        System.out.println("The same " + same + " times");
        System.out.println("Unable to tell " + unable+ " times");
    }

    private static void printQuestion(String oneString, String twoString, boolean oneFirst, String path, int i, int size) {
        System.out.println("----- Compare the below two types (" + i + "/" + size + ") -----");
        System.out.println("Path: " + path);
        System.out.println("1: ");
        System.out.println(oneFirst ? oneString : twoString);
        System.out.println("\n2: ");
        System.out.println(oneFirst ? twoString : oneString);
        System.out.println("\nWhich is best, type 3 for same, 4 for unable to tell which, 5 for exit, 6 to print declaration for the first, 7 to print declaration for the second");
    }

    public static final class FeatureExtractor implements DeclarationTypeVisitorWithArgument<Void, PrecisionTest.Arg> {
        public final Map<String, Pair<DeclarationType, DeclarationType>> modules = new HashMap<>();
        public final Map<String, Pair<DeclarationType, DeclarationType>> fields = new HashMap<>();
        public final Map<String, Pair<DeclarationType, DeclarationType>> constructors = new HashMap<>();
        public final Map<String, Pair<DeclarationType, DeclarationType>> classes = new HashMap<>();
        public final Map<String, Pair<DeclarationType, DeclarationType>> functions = new HashMap<>();



        @Override
        public Void visit(FunctionType functionType, Arg arg) {
            if (!(arg.type instanceof FunctionType)) {
                return null;
            }
            FunctionType other = (FunctionType) arg.type;
            functions.put(arg.path, new Pair<>(functionType, other));
            return null;
        }

        @Override
        public Void visit(PrimitiveDeclarationType primitive, Arg arg) {
            return null;
        }

        @Override
        public Void visit(UnnamedObjectType objectType, Arg arg) {
            if (!(arg.type instanceof UnnamedObjectType)) {
                return null;
            }
            UnnamedObjectType argType = (UnnamedObjectType) arg.type;
            modules.put(arg.path, new Pair<>(objectType, argType));

            for (String key : Util.intersection(objectType.getDeclarations().keySet(), argType.getDeclarations().keySet())) {
                DeclarationType subType1 = objectType.getDeclarations().get(key);
                DeclarationType subType2 = argType.getDeclarations().get(key);
                recurse(subType1, new Arg(subType2, arg.path + "." + key, arg.depth + 1));
            }

            return null;
        }

        @Override
        public Void visit(InterfaceDeclarationType interfaceType, Arg arg) {
            if (!(arg.type instanceof InterfaceDeclarationType)) {
                return null;
            }
            InterfaceDeclarationType other = (InterfaceDeclarationType) arg.type;

            if (interfaceType.getFunction() != null && other.getFunction() != null) {
                recurse(interfaceType.getFunction(), new Arg(other.getFunction(), arg.path, arg.depth + 1));
            }

            if (interfaceType.getObject() != null && other.getObject() != null) {
                recurse(interfaceType.getObject(), new Arg(other.getObject(), arg.path, arg.depth + 1));
            }

            return null;
        }

        @Override
        public Void visit(UnionDeclarationType union, Arg arg) {
            return null;
        }

        @Override
        public Void visit(NamedObjectType namedObjectType, Arg arg) {
            return null;
        }

        @Override
        public Void visit(ClassType classType, Arg arg) {
            if (!(arg.type instanceof ClassType)) {
                return null;
            }

            classes.put(arg.path, new Pair<>(classType, arg.type));

            ClassType otherType = (ClassType) arg.type;

            constructors.put(arg.path, new Pair<>(classType.getConstructorType(), otherType.getConstructorType()));

            for (String key : Util.intersection(classType.getPrototypeFields().keySet(), otherType.getPrototypeFields().keySet())) {
                DeclarationType one = classType.getPrototypeFields().get(key);
                DeclarationType two = otherType.getPrototypeFields().get(key);
                if (one.resolve() instanceof FunctionType && two.resolve() instanceof FunctionType) {
                    functions.put(arg.path + "()." + key, new Pair<>(one, two));
                } else {
                    fields.put(arg.path + "()." + key, new Pair<>(one, two));
                }
            }

//            recurse(new UnnamedObjectType(classType.getStaticFields(), EMPTY_SET), new Arg(new UnnamedObjectType(otherType.getStaticFields(), EMPTY_SET), arg.path, arg.depth + 1));
//            recurse(new UnnamedObjectType(classType.getStaticFields(), EMPTY_SET), new Arg(new UnnamedObjectType(otherType.getStaticFields(), EMPTY_SET), arg.path, arg.depth + 1));

            return null;
        }

        @Override
        public Void visit(ClassInstanceType instanceType, Arg arg) {
            if (arg.type instanceof ClassInstanceType) {
                recurse(instanceType.getClazz(), new Arg(((ClassInstanceType) arg.type).getClazz(), arg.path + ".[typeof]", arg.depth + 1000));
            }
            return null;
        }

        PriorityQueue<QueueElement> queue = new PriorityQueue<>();

        public void recurse(DeclarationType type, Arg arg) {
            queue.add(new QueueElement(type, arg));
        }

        private Set<DeclarationType> seen = new HashSet<>();

        public void finish() {
            while (!queue.isEmpty()) {
                QueueElement element = queue.poll();
                if (seen.contains(element.type)) {
                    continue;
                }
                seen.add(element.type);
                element.type.accept(this, element.arg);
            }
        }

        private static final class QueueElement implements Comparable<QueueElement> {
            final DeclarationType type;
            final Arg arg;

            private QueueElement(DeclarationType type, Arg arg) {
                this.type = type;
                this.arg = arg;
            }


            @Override
            public int compareTo(QueueElement other) {
                return Integer.compare(this.arg.depth, other.arg.depth);
            }
        }
    }


    public static final class Arg {
        private final DeclarationType type;
        private final String path;
        private final int depth;

        public Arg(DeclarationType type, String path, int depth) {
            this.type = type;
            this.path = path;
            this.depth = depth;
        }
    }
}
