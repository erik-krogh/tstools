package dk.webbies.tscreate.main.patch;

import com.google.gson.JsonObject;
import dk.webbies.tscreate.util.JSONBuilder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by erik1 on 10-06-2016.
 */
public class PatchFile {
    private List<PatchStatement> statements;
    private final PatchFileFactory.BenchmarkInformation oldInfo;
    private final PatchFileFactory.BenchmarkInformation newInfo;

    public PatchFile(List<PatchStatement> statements, PatchFileFactory.BenchmarkInformation oldInfo, PatchFileFactory.BenchmarkInformation newInfo) {
        this.statements = statements;
        this.oldInfo = oldInfo;
        this.newInfo = newInfo;
    }

    public List<PatchStatement> getStatements() {
        return statements;
    }

    public String print() {
        StringBuilder builder = new StringBuilder();

        builder.append("Patch file between \"").append(oldInfo.benchMark.name).append("\" and \"").append(newInfo.benchMark.name).append("\"\n\n");

        statements.forEach(stmt -> {
            builder.append("----------------------------------------------------------\n\n");
            builder.append("On path: ").append(stmt.getTypePath()).append("\n");
            builder.append(stmt.print(oldInfo, newInfo));
            builder.append("\n\n");
        });

        return builder.toString();
    }

    public JsonObject toJSON() {
        return JSONBuilder.createObject()
                .add("statements",
                        JSONBuilder.fromCollection(this.statements.stream().map((patchStatement) -> patchStatement.toJSONObject(newInfo, oldInfo)).collect(Collectors.toList())))
                .add("newName", newInfo.benchMark.name)
                .add("oldName", oldInfo.benchMark.name)
                .add("oldDeclaration", oldInfo.printedDeclaration)
                .add("newDeclaration", newInfo.printedDeclaration)
                .build();
    }
}