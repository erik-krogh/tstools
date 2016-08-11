package dk.webbies.tscreate.main.patch;

import com.google.gson.JsonObject;
import dk.webbies.tscreate.analysis.declarations.types.ClassType;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.util.JSONBuilder;

/**
 * Created by erik1 on 09-06-2016.
 */
public class AddedPropertyStatement implements PatchStatement {
    private final String typePath;
    private final String propertyName;
    private final DeclarationType propertyType;
    private final DeclarationType containerType;

    public AddedPropertyStatement(String typePath, String propertyName, DeclarationType propertyType, DeclarationType containerType) {
        this.typePath = typePath;
        this.propertyName = propertyName;
        this.propertyType = propertyType.resolve();
        this.containerType = containerType.resolve();
    }

    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public String print(PatchFileFactory.BenchmarkInformation oldInfo, PatchFileFactory.BenchmarkInformation newInfo) {
        StringBuilder builder = new StringBuilder();

        if (containerType instanceof ClassType) {
            builder.append("Static property \"").append(propertyName).append("\" added to ").append(describe(containerType, newInfo));
        } else {
            builder.append("Property \"").append(propertyName).append("\" added to ").append(describe(containerType, newInfo));
        }
        builder.append("\n");
        if (PatchFileFactory.isAny(propertyType) || PatchFileFactory.isVoid(propertyType)) {
            builder.append("a type could not be inferred");
        } else {
            builder.append("type: \n");
            builder.append(newInfo.printer.printType(propertyType, typePath + "." + propertyName));
        }

        return builder.toString();
    }

    @Override
    public String getTypePath() {
        return typePath;
    }

    @Override
    public JsonObject toJSONObject(PatchFileFactory.BenchmarkInformation newInfo, PatchFileFactory.BenchmarkInformation oldInfo) {
        String typeString = null;
        if (!PatchFileFactory.isAny(propertyType) && !PatchFileFactory.isVoid(propertyType)) {
            typeString = newInfo.printer.printType(propertyType, typePath + "." + propertyName);
        }

        return PatchStatement.build(typePath + "." + propertyName, newInfo, oldInfo)
                .add("type", "addedProperty")
                .add("typePath", typePath)
                .add("key", propertyName)
                .add("newType", typeString)
                .add("isAny", PatchFileFactory.isAny(propertyType) || PatchFileFactory.isVoid(propertyType))
                .add("isClass", containerType instanceof ClassType && !typePath.endsWith("[constructor].[return]"))
                .add("containerType", newInfo.printer.printType(containerType, null))
                .add("containerDescription", describe(containerType, newInfo))
                .build();
    }

}
