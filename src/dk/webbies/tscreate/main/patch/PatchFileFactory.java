package dk.webbies.tscreate.main.patch;

import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.BenchMark;
import dk.webbies.tscreate.analysis.declarations.DeclarationPrinter;
import dk.webbies.tscreate.analysis.declarations.types.*;
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
import java.util.stream.Collectors;

import static dk.webbies.tscreate.declarationReader.DeclarationParser.getTypeSpecification;
import static dk.webbies.tscreate.declarationReader.DeclarationParser.parseNatives;
import static java.util.Collections.*;

/**
 * Created by erik1 on 09-06-2016.
 */
public class PatchFileFactory {
    public static PatchFile fromEvaluation(BenchMark oldBench, BenchMark newBench) throws IOException {
        BenchmarkInformation oldInfo = getInfo(oldBench);

        BenchmarkInformation newInfo = getInfo(newBench);

        Type handWrittenType = getTypeSpecification(oldBench.languageLevel.environment, oldBench.dependencyDeclarations(), oldBench.declarationPath).getGlobal();

        List<PatchStatement> patchStatements = FindPatchStatementsVisitor.generateStatements(oldInfo.globalObject, newInfo.globalObject, newBench.getOptions(), handWrittenType, newInfo.nativeClasses);

        return new PatchFile(patchStatements, oldInfo, newInfo);
    }

    public static final class BenchmarkInformation {
        public final BenchMark benchMark;
        public final UnnamedObjectType globalObject;
        public final DeclarationPrinter printer;
        public final String printedDeclaration;
        private DeclarationParser.NativeClassesMap nativeClasses;

        public BenchmarkInformation(BenchMark benchMark, UnnamedObjectType globalObject, DeclarationPrinter printer, String printedDeclaration, DeclarationParser.NativeClassesMap nativeClasses) {
            this.benchMark = benchMark;
            this.globalObject = globalObject;
            this.printer = printer;
            this.printedDeclaration = printedDeclaration;
            this.nativeClasses = nativeClasses;
        }
    }

    private static BenchmarkInformation getInfo(BenchMark benchMark) throws IOException {
        FunctionExpression AST = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, Main.getScript(benchMark)).toTSCreateAST();

        Snap.Obj globalObjectJsnapObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.getOptions(), benchMark.dependencyScripts(), benchMark.testFiles, benchMark.getOptions().asyncTest), AST);
        Snap.Obj emptySnap = JSNAPUtil.getEmptyJSnap(benchMark.getOptions(), benchMark.dependencyScripts(), AST); // Not empty, just the one without the library we are analyzing.

        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObjectJsnapObject, benchMark.getOptions()).extract();

        DeclarationParser.NativeClassesMap nativeClasses = parseNatives(globalObjectJsnapObject, benchMark.languageLevel.environment, benchMark.dependencyDeclarations(), libraryClasses, emptySnap);

        UnnamedObjectType globalObject = new UnnamedObjectType(Main.createDeclaration(benchMark, AST, globalObjectJsnapObject, emptySnap, libraryClasses, nativeClasses), EMPTY_SET);

        DeclarationPrinter printer = new DeclarationPrinter(globalObject.getDeclarations(), nativeClasses, benchMark.getOptions());
        String printedDeclaration = printer.print();



        return new BenchmarkInformation(benchMark, globalObject, printer, printedDeclaration, nativeClasses);
    }

    public static boolean isAny(DeclarationType type) {
        type = type.resolve();
        return type instanceof PrimitiveDeclarationType && (((PrimitiveDeclarationType) type).getType() == PrimitiveDeclarationType.Type.ANY || ((PrimitiveDeclarationType) type).getType() == PrimitiveDeclarationType.Type.NON_VOID);
    }

    public static boolean isVoid(DeclarationType type) {
        type = type.resolve();
        return type instanceof PrimitiveDeclarationType && (((PrimitiveDeclarationType) type).getType() == PrimitiveDeclarationType.Type.VOID);
    }
}
