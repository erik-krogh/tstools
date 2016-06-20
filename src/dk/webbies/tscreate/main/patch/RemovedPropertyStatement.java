package dk.webbies.tscreate.main.patch;

import com.google.gson.JsonObject;
import dk.webbies.tscreate.analysis.declarations.types.ClassType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.util.JSONBuilder;

/**
 * Created by erik1 on 09-06-2016.
 */
public class RemovedPropertyStatement implements PatchStatement {
    private final String typePath;
    private final String propertyName;
    private final DeclarationType containerType;

    public RemovedPropertyStatement(String typePath, String propertyName, DeclarationType containerType) {
        this.typePath = typePath;
        this.propertyName = propertyName;
        this.containerType = containerType.resolve();
    }

    @Override
    public String print(PatchFileFactory.BenchmarkInformation oldInfo, PatchFileFactory.BenchmarkInformation newInfo) {
        StringBuilder builder = new StringBuilder();

        if (containerType instanceof ClassType) {
            builder.append("Static property \"").append(propertyName).append("\" removed from ").append(describe(containerType, newInfo));
        } else {
            builder.append("Property \"").append(propertyName).append("\" removed from ").append(describe(containerType, newInfo));
        }
/*        builder.append("\n");
        builder.append("new container type: \n");
        builder.append(newInfo.printer.printType(containerType, typePath + "." + propertyName));*/

        return builder.toString();
    }

    @Override
    public String getTypePath() {
        return typePath;
    }

    @Override
    public JsonObject toJSONObject(PatchFileFactory.BenchmarkInformation newInfo, PatchFileFactory.BenchmarkInformation oldInfo) {
        return JSONBuilder.createObject()
                .add("type", "removedProperty")
                .add("typePath", typePath)
                .add("key", propertyName)
                .add("isClass", containerType instanceof ClassType)
                .add("containerType", newInfo.printer.printType(containerType, null))
                .add("containerDescription", describe(containerType, newInfo))
                .build();
    }
}
