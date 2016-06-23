package dk.webbies.tscreate.main.patch;

import com.google.gson.JsonObject;
import dk.webbies.tscreate.analysis.declarations.types.ClassType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.FunctionType;
import dk.webbies.tscreate.util.JSONBuilder;

/**
 * Created by erik1 on 10-06-2016.
 */
public class ChangedNumberOfArgumentsStatement implements PatchStatement {
    private final String typePath;
    private final DeclarationType oldFunction;
    private final DeclarationType newFunction;
    private final DeclarationType containerType;
    private final int oldArgCount;
    private final int newArgCount;

    public ChangedNumberOfArgumentsStatement(String typePath, DeclarationType oldFunction, DeclarationType newFunction, DeclarationType containerType, int oldArgCount, int newArgCount) {
        this.typePath = typePath;
        this.oldFunction = oldFunction.resolve();
        this.newFunction = newFunction.resolve();
        this.containerType = containerType.resolve();
        this.oldArgCount = oldArgCount;
        this.newArgCount = newArgCount;
    }

    @Override
    public String print(PatchFileFactory.BenchmarkInformation oldInfo, PatchFileFactory.BenchmarkInformation newInfo) {
        StringBuilder builder = new StringBuilder();

        if (newFunction instanceof ClassType) {
            assert oldFunction instanceof ClassType;

            builder.append("Number of arguments changed from ").append(oldArgCount).append(" to ").append(newArgCount).append(" in constructor for ").append(describe(newFunction, newInfo));

        } else {
            assert newFunction instanceof FunctionType;
            assert oldFunction instanceof FunctionType;

            builder.append("Number of arguments changed from ").append(oldArgCount).append(" to ").append(newArgCount);
        }

        builder.append("\n\n");

        builder.append("old type: \n").append(oldInfo.printer.printType(oldFunction, typePath)).append("\n\n");

        builder.append("new type: \n").append(newInfo.printer.printType(newFunction, typePath)).append("\n\n");

        return builder.toString();
    }

    @Override
    public String getTypePath() {
        return typePath;
    }

    @Override
    public JsonObject toJSONObject(PatchFileFactory.BenchmarkInformation newInfo, PatchFileFactory.BenchmarkInformation oldInfo) {
        return JSONBuilder.createObject()
                .add("type", "changedArgCount")
                .add("typePath", typePath)
                .add("isClass", containerType instanceof ClassType && !typePath.endsWith("[constructor].[return]"))
                .add("newType", newInfo.printer.printType(newFunction, typePath))
                .add("newTypeDescription", describe(newFunction, newInfo))
                .add("oldType", oldInfo.printer.printType(oldFunction, typePath))
                .add("oldArgCount", oldArgCount)
                .add("newArgCount", newArgCount)
                .add("containerType", newInfo.printer.printType(containerType, null))
                .add("containerDescription", describe(containerType, newInfo))
                .build();
    }

}
