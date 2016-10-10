package dk.webbies.tscreate.main;

import dk.webbies.tscreate.BenchMark;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.main.patch.PatchFileFactory;
import dk.webbies.tscreate.util.Pair;
import dk.webbies.tscreate.util.Util;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.webbies.tscreate.BenchMark.*;

/**
 * Created by erik1 on 29-08-2016.
 */
public class TSCheckMissesCounter {
    public static void main(String[] args) throws IOException {
        // Arrays.asList(ace, angular, async201, backbone133, D3, ember27, FabricJS15, hammer, handlebars4, jQuery, knockout, leaflet, moment_214, p2, PIXI_4_0, react, sugar, three, underscore)
        System.out.println("Benchmark\tfunctions\targuments\tclasses\tfields");
        /*for (BenchMark benchMark : Arrays.asList(ace, angular, async201, backbone133, *//*D3, *//*ember27, FabricJS15, hammer, handlebars4, jQuery, knockout, leaflet, moment_214, vue, jasmine24, PIXI_4_0, react, polymer16, three, underscore)) {
//        for (BenchMark benchMark : Arrays.asList(vue, jasmine24, polymer)) {
            runForBenchmark(benchMark);
        }*/

        runForBenchmark(polymer11);

//        runForBenchmark(D3);


        System.out.println("Total\t" + totalFunctions + " (" + totalNonTrivialFunctions + ")\t" + totalArguments + "\t" + totalClasses + "\t" + totalFields);

        System.exit(0);
    }

    private static int totalFunctions = 0;
    private static int totalNonTrivialFunctions = 0;
    private static int totalArguments = 0;
    private static int totalClasses = 0;
    private static int totalFields = 0;

    private static void runForBenchmark(BenchMark benchMark) throws IOException {
        if (benchMark.declarationPath == null) {
            throw new RuntimeException("Benchmark: " + benchMark.name + " did not have a declaration file");
        }
        PatchFileFactory.BenchmarkInformation info = UsefulnessTest.getInfoFromHandwritten(benchMark);

        PrecisionTest.FeatureExtractor visitor = new PrecisionTest.FeatureExtractor();
        info.printer.declarations.values().forEach(value -> value.accept(visitor, new PrecisionTest.Arg(value, "", 0)));
        visitor.finish();

        int functions = visitor.functions.size();
        int trivialFunctions = (int) visitor.functions.values().stream()
                .map(Pair::getLeft)
                .map(func -> (FunctionType) func)
                .map(FunctionType::getReturnType)
                .map(type -> type.resolve())
                .filter(PrimitiveDeclarationType.class::isInstance)
                .map(type -> (PrimitiveDeclarationType) type)
                .map(PrimitiveDeclarationType::getType)
                .filter(type -> type == PrimitiveDeclarationType.Type.ANY || type == PrimitiveDeclarationType.Type.NON_VOID || type == PrimitiveDeclarationType.Type.VOID)
                .count();
        int nonTrivialFunctions = functions - trivialFunctions;
        int arguments = visitor.functions.values().stream()
                .map(Pair::getLeft)
                .map(func -> (FunctionType) func)
                .map(FunctionType::getArguments)
                .map(List::size)
                .reduce(0, (a, b) -> a + b);
        int classes = visitor.classes.size();
        int fields = visitor.classes.values().stream()
                .map(Pair::getLeft)
                .map(clazz -> (ClassType) clazz)
                .map(ClassType::getPrototypeFields)
                .map(Map::values)
                .map(values -> values.stream().filter(Util.not(FunctionType.class::isInstance)).collect(Collectors.toList()))
                .map(Collection::size)
                .reduce(0, (a, b) -> a + b);

        System.out.println(benchMark.name + "\t" + functions + " (" + nonTrivialFunctions + ")\t" + arguments + "\t" + classes + "\t" + fields);

        totalFunctions += functions;
        totalNonTrivialFunctions += nonTrivialFunctions;
        totalArguments += arguments;
        totalClasses += classes;
        totalFields += fields;
    }
}
