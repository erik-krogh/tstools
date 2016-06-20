package dk.webbies.tscreate.main.patch;

import com.google.gson.JsonObject;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.util.JSONBuilder;

/**
 * Created by erik1 on 10-06-2016.
 */
public class ChangedTypeStatement implements PatchStatement {
    private final String typePath;
    private final DeclarationType newType;
    private final DeclarationType oldType;
    private final DeclarationType containerType;

    public ChangedTypeStatement(String typePath, DeclarationType newType, DeclarationType oldType, DeclarationType containerType) {
        this.typePath = typePath;
        this.newType = newType.resolve();
        this.oldType = oldType.resolve();
        this.containerType = containerType.resolve();
    }

    @Override
    public String print(PatchFileFactory.BenchmarkInformation oldInfo, PatchFileFactory.BenchmarkInformation newInfo) {
        return "Type changed. On path: " + typePath + "\nFrom: \n " +
                oldInfo.printer.printType(oldType, typePath) +
                "\nTo: \n" +
                newInfo.printer.printType(newType, typePath) +
                "\nIn container: \n" +
                newInfo.printer.printType(containerType, typePath);
    }

    @Override
    public String getTypePath() {
        return typePath;
    }

    @Override
    public JsonObject toJSONObject(PatchFileFactory.BenchmarkInformation newInfo, PatchFileFactory.BenchmarkInformation oldInfo) {
        return JSONBuilder.createObject()
                .add("type", "changedType")
                .add("typePath", typePath)
                .add("newType", newInfo.printer.printType(newType, typePath))
                .add("oldType", oldInfo.printer.printType(oldType, typePath))
                .add("containerType", newInfo.printer.printType(containerType, null))
                .add("containerDescription", describe(containerType, newInfo))
                .build();
    }
}
