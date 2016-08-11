package dk.webbies.tscreate.main.patch;

import com.google.gson.JsonObject;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.paser.AST.FunctionExpression;
import dk.webbies.tscreate.util.JSONBuilder;
import dk.webbies.tscreate.util.LookupDeclarationType;
import dk.webbies.tscreate.util.LookupType;
import dk.webbies.tscreate.util.Util;

import java.util.Collection;
import java.util.List;

/**
 * Created by erik1 on 09-06-2016.
 */
public interface PatchStatement {
    String print(PatchFileFactory.BenchmarkInformation oldInfo, PatchFileFactory.BenchmarkInformation newInfo);

    String getTypePath();

    default String describe(DeclarationType type, PatchFileFactory.BenchmarkInformation info) {
        if (type instanceof ClassInstanceType) {
            ClassType clazz = (ClassType) ((ClassInstanceType) type).getClazz().resolve();
            return describeClass(info, clazz);
        } else if (type instanceof ClassType) {
            return describeClass(info, (ClassType) type);
        } else if (type instanceof UnnamedObjectType) {
            return "object";
        } else if (type instanceof UnionDeclarationType) {
            return "some union type";
        } else if (type instanceof FunctionType) {
            return "function";
        } else if (type instanceof NamedObjectType && ((NamedObjectType) type).getName().equals("Array")) {
            return "array-type";
        } else if (type instanceof InterfaceDeclarationType) {
            return "interface";
        } else if (type instanceof PrimitiveDeclarationType) {
            return "primitive (/unknown)";
        } else if (type instanceof NamedObjectType) {
            return "named (" + ((NamedObjectType) type).getName() + ")";
        } else {
            throw new RuntimeException("Did not know: " + type.getClass().getSimpleName());
        }
    }

    default String describeClass(PatchFileFactory.BenchmarkInformation info, ClassType clazz) {
        String name = info.printer.declarationNames.get(clazz);
        if (name == null) {
            name = clazz.getName();
        }

        return "class " + name;
    }

    JsonObject toJSONObject(PatchFileFactory.BenchmarkInformation newInfo, PatchFileFactory.BenchmarkInformation oldInfo);

    static JSONBuilder.ObjectBuilder build(String typePath, PatchFileFactory.BenchmarkInformation newInfo, PatchFileFactory.BenchmarkInformation oldInfo) {
        String containerPath = withoutLastPart(typePath);

        InclosingFunctionResult newInclosingFunction = getInclodingFunction(typePath, newInfo);
        InclosingFunctionResult oldInclosingFunction = getInclodingFunction(typePath, oldInfo);

        return JSONBuilder.createObject()
                .add("isInOldDec", PatchStatement.findInHandWritten(typePath, oldInfo) != null)
                .add("isInOldDecContainer", PatchStatement.findInHandWritten(containerPath, oldInfo) != null)
                .add("isInNewDec", PatchStatement.findInHandWritten(typePath, newInfo) != null)
                .add("isInNewDecContainer", PatchStatement.findInHandWritten(containerPath, newInfo) != null)
                .add("newInclosingFunctionPath", newInclosingFunction.typePath)
                .add("newFunction", newInclosingFunction.getFunctionString())
                .add("newJSDoc", newInclosingFunction.getJSDocString())
                .add("oldInclosingFunctionPath", oldInclosingFunction.typePath)
                .add("oldFunction", oldInclosingFunction.getFunctionString())
                .add("oldJSDoc", oldInclosingFunction.getJSDocString());
    }

    static InclosingFunctionResult getInclodingFunction(String typePath, PatchFileFactory.BenchmarkInformation newInfo) {
        List<DeclarationType> list = new LookupDeclarationType(Util.removePrefix(typePath, "window.")).findList(newInfo.globalObject);

        FunctionExpression inclosingFunction = null;
        for (DeclarationType type : list) {
            if (type instanceof FunctionType) {
                Collection<FunctionExpression> astNodes = ((FunctionType) type).getAstNodes();
                if (!astNodes.isEmpty()) {
                    inclosingFunction = astNodes.iterator().next();

                    break;
                }
            } else if (type instanceof ClassType) {
                Collection<FunctionExpression> astNodes = ((ClassType) type).getConstructorType().getAstNodes();
                if (!astNodes.isEmpty()) {
                    inclosingFunction = astNodes.iterator().next();
                    break;
                }
            }
            typePath = withoutLastPart(typePath);
        }
        return new InclosingFunctionResult(inclosingFunction, typePath);
    }

    static final class InclosingFunctionResult {
        final FunctionExpression functionExpression;
        final String typePath;

        public InclosingFunctionResult(FunctionExpression functionExpression, String typePath) {
            this.functionExpression = functionExpression;
            this.typePath = typePath;
        }

        public String getFunctionString() {
            if (functionExpression != null) {
                return functionExpression.toString();
            }
            return null;
        }

        public String getJSDocString() {
            if (functionExpression != null && functionExpression.jsDoc != null) {
                return functionExpression.jsDoc.value;
            }
            return null;
        }
    }

    static String withoutLastPart(String typePath) {
        if (!typePath.contains(".")) {
            return typePath;
        } else {
            return typePath.substring(0, typePath.lastIndexOf("."));
        }
    }

    public static Type findInHandWritten(String typePath, PatchFileFactory.BenchmarkInformation information) {
        if (information.handwritten == null) {
            return null;
        }
        if (typePath.equals("window")) {
            return information.handwritten;
        }
        return information.handwritten.accept(new LookupType(Util.removePrefix(typePath, "window.")));
    }

}
