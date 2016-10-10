package dk.webbies.tscreate.main.normalizeDec;

import com.google.common.collect.ArrayListMultimap;
import dk.au.cs.casa.typescript.SpecReader;
import dk.au.cs.casa.typescript.types.InterfaceType;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.BenchMark;
import dk.webbies.tscreate.analysis.declarations.DeclarationPrinter;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.cleanup.CollectEveryTypeVisitor;
import dk.webbies.tscreate.cleanup.InplaceDeclarationReplacer;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.ClassHierarchyExtractor;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.main.Main;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.paser.JavaScriptParser;
import dk.webbies.tscreate.util.Util;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dk.webbies.tscreate.declarationReader.DeclarationParser.parseNatives;

/**
 * Created by erik1 on 15-03-2016.
 */
public class NormalizeDeclaration {
    public static void main(String[] args) throws IOException {
        /*for (BenchMark benchmark : BenchMark.allBenchmarks) {
            normalize(benchmark);
        }*/

    }

    private static void normalize(BenchMark benchMark) throws IOException {
        if (benchMark.declarationPath == null) {
            return;
        }
        System.out.println("Normalizing: " + benchMark.name);

        SpecReader spec = DeclarationParser.getTypeSpecification(benchMark.languageLevel.environment, benchMark.dependencyDeclarations(), benchMark.declarationPath);

        Map<Type, String> typeNames = getTypeNames(spec, benchMark);

        SpecReader emptySpec = DeclarationParser.getTypeSpecification(benchMark.languageLevel.environment, benchMark.dependencyDeclarations());
        Set<String> existingKeys = ((InterfaceType) emptySpec.getGlobal()).getDeclaredProperties().keySet();

        InterfaceType global = (InterfaceType)spec.getGlobal();

        global.setDeclaredProperties(global.getDeclaredProperties().entrySet().stream().filter(entry -> !existingKeys.contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        FunctionExpression AST = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, Main.getScript(benchMark)).toTSCreateAST();
        Snap.Obj globalObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.getOptions(), benchMark.dependencyScripts(), benchMark.testFiles, benchMark.getOptions().asyncTest), AST);
        Snap.Obj emptySnap = JSNAPUtil.getEmptyJSnap(benchMark.getOptions(), benchMark.dependencyScripts(), AST); // Not empty, just the one without the library we are analyzing.

        DeclarationParser.NativeClassesMap nativeClasses = parseNatives(globalObject, benchMark.languageLevel.environment, benchMark.dependencyDeclarations(), new ClassHierarchyExtractor(globalObject, benchMark.getOptions()).extract(), emptySnap);

        DeclarationPrinter printer = generateDeclarationPrinterFromHandwritten(benchMark, globalObject, nativeClasses, typeNames, spec, global);
        String printedDeclaration = printer.print();
//        System.out.println(printedDeclaration);

        assert benchMark.declarationPath.endsWith(".d.ts");
        String normalizedPath = benchMark.declarationPath.substring(0, benchMark.declarationPath.length() - 5) + ".normalized.d.ts";

        Util.writeFile(normalizedPath, printedDeclaration);
    }

    public static DeclarationPrinter generateDeclarationPrinterFromHandwritten(BenchMark benchMark, Snap.Obj globalObject, DeclarationParser.NativeClassesMap nativeClasses, Map<Type, String> typeNames, SpecReader spec, InterfaceType global) throws IOException {
        TypeReducer reducer = new TypeReducer(globalObject, nativeClasses, benchMark.getOptions());
        ToDeclarationTypeVisitor converter = new ToDeclarationTypeVisitor(typeNames, DeclarationParser.getTypeNamesMap(spec), reducer);

        Map<String, DeclarationType> declaration = new HashMap<>();
        for (Map.Entry<String, Type> entry : global.getDeclaredProperties().entrySet()) {
            declaration.put(entry.getKey(), entry.getValue().accept(converter));
            converter.finish();
        }

        converter.resolveClassHierarchy();

        declaration = mapTypes(reducer, declaration, (type) -> {
            if (type instanceof InterfaceDeclarationType && converter.interfaceToClassInstanceMap.containsKey(type)) {
                return converter.interfaceToClassInstanceMap.get(type);
            } else {
                return type;
            }
        });

        declaration = mapTypes(reducer, declaration, (type) -> {
            if (converter.getInterfaceExtensions().containsKey(type)) {
                CombinationType result = new CombinationType(reducer, type);
                result.addTypes(converter.getInterfaceExtensions().get(type));
                result.addNames(type.getNames());
                return result.getCombined();
            }
            return type;
        });

        declaration = mapTypes(reducer, declaration, type -> cleanType(type, reducer));

        HashSet<String> takenClassNames = new HashSet<>();
        declaration = mapTypes(reducer, declaration, (type) -> {
            if (type instanceof ClassType) {
                ArrayList<String> names = new ArrayList<>(type.getNames());
                if (!names.isEmpty()) {
                    ((ClassType)type).setName(LibraryClass.calculateName(nativeClasses, takenClassNames, names));
                }
            }
            return type;
        });

        return new DeclarationPrinter(declaration, nativeClasses, benchMark.getOptions());
    }

    private static Map<String, DeclarationType> mapTypes(TypeReducer reducer, Map<String, DeclarationType> declaration, Function<DeclarationType, DeclarationType> mapper) {
        CollectEveryTypeVisitor collector = new CollectEveryTypeVisitor(declaration, false);
        new InplaceDeclarationReplacer(ArrayListMultimap.create(), collector, reducer, declaration, mapper).cleanStuff();
        return declaration;
    }

    private static DeclarationType cleanType(DeclarationType type, TypeReducer reducer) {
        // Two things: 1: adding names to stuff, 2: making interfaces into something prettier.
        if (type instanceof UnnamedObjectType) {
            addNames((UnnamedObjectType) type);
        }

        if (type instanceof FunctionType) {
            addNames((FunctionType) type);
        }

        if (type instanceof InterfaceDeclarationType) {
            addNames(((InterfaceDeclarationType) type).getObject());
            addNames(((InterfaceDeclarationType) type).getFunction());

            DeclarationType combined = new CombinationType(reducer, type).getCombined();
            if (!(combined instanceof InterfaceDeclarationType)) {
                combined.addNames(type.getNames());
                return combined;
            }
        }
        return type;
    }

    private static void addNames(FunctionType type) {
        if (type == null) {
            return;
        }
        for (FunctionType.Argument argument : type.getArguments()) {
            argument.getType().addName(argument.getName());
        }

    }

    private static void addNames(UnnamedObjectType type) {
        if (type == null) {
            return;
        }
        for (Map.Entry<String, DeclarationType> entry : type.getDeclarations().entrySet()) {
            entry.getValue().addName(entry.getKey());
        }
    }

    public static Map<Type, String> getTypeNames(SpecReader spec, BenchMark benchMark) {
        SpecReader withoutLib = DeclarationParser.getTypeSpecification(benchMark.languageLevel.environment, benchMark.dependencyDeclarations());

        Map<Type, String> typesNames = DeclarationParser.getTypeNamesMap(spec);

        Set<String> stringToKeep = new HashSet<>(DeclarationParser.getTypeNamesMap(withoutLib).values());

        return typesNames.entrySet().stream().filter(entry -> stringToKeep.contains(entry.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
