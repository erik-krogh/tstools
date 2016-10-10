package dk.webbies.tscreate.main.patch;

import dk.au.cs.casa.typescript.types.SimpleType;
import dk.au.cs.casa.typescript.types.SimpleTypeKind;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.BenchMark;
import dk.webbies.tscreate.analysis.declarations.DeclarationPrinter;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.evaluation.DebugEvaluation;
import dk.webbies.tscreate.evaluation.descriptions.*;
import dk.webbies.tscreate.jsnap.JSNAPUtil;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.ClassHierarchyExtractor;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.main.CompareVersions;
import dk.webbies.tscreate.main.Main;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.paser.JavaScriptParser;
import dk.webbies.tscreate.util.LookupDeclarationType;
import dk.webbies.tscreate.util.LookupType;
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
    public static PatchFile fromImplementation(BenchMark oldBench, BenchMark newBench) throws Throwable {
        final BenchmarkInformation[] oldInfo = new BenchmarkInformation[1];

        final BenchmarkInformation[] newInfo = new BenchmarkInformation[1];

        Util.runAll(() -> {
            try {
                oldInfo[0] = getInfo(oldBench);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, () -> {
            try {
                newInfo[0] = getInfo(newBench);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        List<PatchStatement> patchStatements = FindPatchStatementsVisitor.generateStatements(oldInfo[0].globalObject, newInfo[0].globalObject, newBench.getOptions(), oldInfo[0].handwritten, newInfo[0].handwritten, newInfo[0].nativeClasses);
        // For braekPoint: Break on NullPointerException.
        return new PatchFile(patchStatements, oldInfo[0], newInfo[0]);
    }

    public static PatchFile fromHandwritten(BenchMark oldBench, BenchMark newBench) throws IOException {
        List<DebugEvaluation.EvaluationStatement> evaluations = CompareVersions.compareHandWritten(oldBench, newBench);

        BenchmarkInformation oldInfo = getInfo(oldBench);

        BenchmarkInformation newInfo = getInfo(newBench);

        oldInfo.printedDeclaration = Util.readFile(oldBench.declarationPath);
        newInfo.printedDeclaration = Util.readFile(newBench.declarationPath);

        Type oldHandwritten = getTypeSpecification(oldBench.languageLevel.environment, oldBench.dependencyDeclarations(), oldBench.declarationPath).getGlobal();

        List<PatchStatement> patchStatements = evaluations.stream().map(stmt -> toPatchStatement(stmt, oldInfo, newInfo, oldHandwritten)).collect(Collectors.toList());

        return new PatchFile(patchStatements, oldInfo, newInfo);
    }

    public static final class BenchmarkInformation {
        public final BenchMark benchMark;
        public final UnnamedObjectType globalObject;
        public final DeclarationPrinter printer;
        public String printedDeclaration;
        public DeclarationParser.NativeClassesMap nativeClasses;
        public final Type handwritten;
        public Snap.Obj snapshot;

        public BenchmarkInformation(BenchMark benchMark, UnnamedObjectType globalObject, DeclarationPrinter printer, String printedDeclaration, DeclarationParser.NativeClassesMap nativeClasses, Type handwritten, Snap.Obj snapshot) {
            this.benchMark = benchMark;
            this.globalObject = globalObject;
            this.printer = printer;
            this.printedDeclaration = printedDeclaration;
            this.nativeClasses = nativeClasses;
            this.handwritten = handwritten;
            this.snapshot = snapshot;
        }
    }

    public static BenchmarkInformation getInfo(BenchMark benchMark) throws IOException {
        FunctionExpression AST = new JavaScriptParser(benchMark.languageLevel).parse(benchMark.name, Main.getScript(benchMark)).toTSCreateAST();

        Snap.Obj globalObjectJsnapObject = JSNAPUtil.getStateDump(JSNAPUtil.getJsnapRaw(benchMark.scriptPath, benchMark.getOptions(), benchMark.dependencyScripts(), benchMark.testFiles, benchMark.getOptions().asyncTest), AST);
        Snap.Obj emptySnap = JSNAPUtil.getEmptyJSnap(benchMark.getOptions(), benchMark.dependencyScripts(), AST); // Not empty, just the one without the library we are analyzing.

        HashMap<Snap.Obj, LibraryClass> libraryClasses = new ClassHierarchyExtractor(globalObjectJsnapObject, benchMark.getOptions()).extract();

        DeclarationParser.NativeClassesMap nativeClasses = parseNatives(globalObjectJsnapObject, benchMark.languageLevel.environment, benchMark.dependencyDeclarations(), libraryClasses, emptySnap);

        UnnamedObjectType globalObject = new UnnamedObjectType(Main.createDeclaration(benchMark, AST, globalObjectJsnapObject, emptySnap, libraryClasses, nativeClasses), EMPTY_SET);

        DeclarationPrinter printer = new DeclarationPrinter(globalObject.getDeclarations(), nativeClasses, benchMark.getOptions());
        String printedDeclaration = printer.print();

        Type handwritten = null;
        if (benchMark.declarationPath != null) {
            handwritten = getTypeSpecification(benchMark.languageLevel.environment, benchMark.dependencyDeclarations(), benchMark.declarationPath).getGlobal();
        }


        return new BenchmarkInformation(benchMark, globalObject, printer, printedDeclaration, nativeClasses, handwritten, globalObjectJsnapObject);
    }

    public static boolean isAny(DeclarationType type) {
        type = type.resolve();
        return type instanceof PrimitiveDeclarationType && (((PrimitiveDeclarationType) type).getType() == PrimitiveDeclarationType.Type.ANY || ((PrimitiveDeclarationType) type).getType() == PrimitiveDeclarationType.Type.NON_VOID);
    }

    public static boolean isVoid(DeclarationType type) {
        type = type.resolve();
        return type instanceof PrimitiveDeclarationType && (((PrimitiveDeclarationType) type).getType() == PrimitiveDeclarationType.Type.VOID);
    }

    private static PatchStatement toPatchStatement(DebugEvaluation.EvaluationStatement stmt, BenchmarkInformation oldInfo, BenchmarkInformation newInfo, Type oldHandwrittenType) {
        if (stmt.description.getType() == Description.DescriptionType.TRUE_POSITIVE) {
            return null;
        }
        return stmt.description.accept(new ToPatchStatementVisitor(stmt.typePath, oldInfo, newInfo, oldHandwrittenType));
    }

    private static final class ToPatchStatementVisitor implements DescriptionVisitor<PatchStatement> {
        private final String typePath;
        private final BenchmarkInformation oldInfo;
        private final BenchmarkInformation newInfo;
        private final Type oldHandWrittenType;

        public ToPatchStatementVisitor(String typePath, BenchmarkInformation oldInfo, BenchmarkInformation newInfo, Type oldHandWrittenType) {
            this.typePath = typePath;
            this.oldInfo = oldInfo;
            this.newInfo = newInfo;
            this.oldHandWrittenType = oldHandWrittenType;
        }

        private DeclarationType lookUpNew(String path) {
            return lookUp(path, newInfo.globalObject);
        }

        private DeclarationType lookUpOld(String path) {
            return lookUp(path, oldInfo.globalObject);
        }

        private DeclarationType lookUp(String path, UnnamedObjectType globalObject) {
            if (path.equals("window")) {
                return globalObject;
            }
            DeclarationType result = new LookupDeclarationType(Util.removePrefix(path, "window.")).find(globalObject);
            if (result == null) {
                result = PrimitiveDeclarationType.Any(Collections.EMPTY_SET);
            }
            return result;
        }



        @Override
        public PatchStatement visit(Description.SimpleDescription description) {
            switch (description.getSimpleType()) {
                case EXPECTED_INTERFACE:
                case SHOULD_NOT_BE_CONSTRUCTOR:
                case SHOULD_BE_CONSTRUCTOR:
                case WRONG_NATIVE_TYPE:
                case EXCESS_INDEXER:
                case MISSING_INDEXER:
                    DeclarationType newType = lookUpNew(typePath);
                    DeclarationType oldType = lookUpOld(typePath);

                    if (newType == null || oldType == null) {
                        System.err.println("error, simple, at " + typePath);
                        return null;
                    }
                    newType = newType.resolve();
                    oldType = oldType.resolve();

                    if (newType instanceof PrimitiveDeclarationType && ((PrimitiveDeclarationType) newType).getType() == PrimitiveDeclarationType.Type.NON_VOID) {
                        return null;
                    }

                    // If any of them are any (or NON_VOID), lookup in the old handwritten declaration file. It it states something specific, skip this one.
                    if (isAny(newType) || isAny(oldType)) {
                        Type handwrittenType = oldHandWrittenType.accept(new LookupType(Util.removePrefix(typePath, "window.")));
                        if (handwrittenType instanceof SimpleType) {
                            SimpleType simple = (SimpleType) handwrittenType;
                            if (simple.getKind() == SimpleTypeKind.Any || simple.getKind() == SimpleTypeKind.Null || simple.getKind() == SimpleTypeKind.Void || simple.getKind() == SimpleTypeKind.Undefined) {
                                return null;
                            }
                        }
                    }

                    return new ChangedTypeStatement(typePath, newType, oldType, lookUpNew(withoutLastPart(typePath)));
                default:
                    throw new RuntimeException(description.getSimpleType().name() + " is not implemented here yet. Path: " + typePath);
            }
        }

        @Override
        public PatchStatement visit(PropertyMissingDescription description) {
            DeclarationType oldType = lookUpOld(typePath);

            if (oldType == null) {
                System.err.println("Error, property missing,  at " + typePath);
                return null;
            }

            return new RemovedPropertyStatement(withoutLastPart(typePath), description.getPropertyName(), lookUpNew(withoutLastPart(typePath)));
        }

        @Override
        public PatchStatement visit(ExcessPropertyDescription description) {
            DeclarationType newType = lookUpNew(typePath);

            if (newType == null) {
                System.err.println("Error, excess property,  at " + typePath);
                return null;
            }

            if (isVoid(newType)) {
                return null;
            }

            return new AddedPropertyStatement(withoutLastPart(typePath), description.getPropertyName(), lookUpNew(typePath), lookUpNew(withoutLastPart(typePath)));
        }

        private String withoutLastPart(String typePath) {
            if (!typePath.contains(".")) {
                return typePath;
            } else {
                return typePath.substring(0, typePath.lastIndexOf("."));
            }
        }

        @Override
        public PatchStatement visit(RightPropertyDescription description) {
            return null;
        }

        @Override
        public PatchStatement visit(WrongNumberOfArgumentsDescription description) {
            DeclarationType newFunction = lookUpNew(typePath);
            DeclarationType oldFunction = lookUpOld(typePath);
            if (newFunction == null || oldFunction == null) {
                System.err.println("error, arg count, " + typePath);
            }
            return new ChangedNumberOfArgumentsStatement(typePath, oldFunction, newFunction, lookUpNew(withoutLastPart(typePath)), description.getOldArgCount(), description.getNewArgCount());
        }

        @Override
        public PatchStatement visit(WrongSimpleTypeDescription description) {
            if (description.getExpected().getKind() == SimpleTypeKind.Void) {
                return Description.ExcessProperty(lastPart(typePath)).accept(this);
            }
            DeclarationType newType = lookUpNew(typePath);
            DeclarationType oldType = lookUpOld(typePath);

            if (newType == null || oldType == null) {
                System.err.println("error, wrong simple, " + typePath);
            }

            if (isAny(newType) || isAny(oldType)) {
                return null;
            }
            return new ChangedTypeStatement(typePath, newType, oldType, lookUpNew(withoutLastPart(typePath)));
        }

        private String lastPart(String typePath) {
            if (typePath.indexOf('.') == -1) {
                return typePath;
            }
            return typePath.substring(typePath.lastIndexOf('.') + 1, typePath.length());
        }
    }
}
