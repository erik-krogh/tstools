package dk.webbies.tscreate.main.patch;

import com.google.gson.JsonObject;
import dk.webbies.tscreate.util.JSONBuild;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by erik1 on 10-06-2016.
 */
public class PatchFile {
    private final List<PatchStatement> statements;
    private final PatchFileFactory.BenchmarkInformation oldInfo;
    private final PatchFileFactory.BenchmarkInformation newInfo;

    public PatchFile(List<PatchStatement> statements, PatchFileFactory.BenchmarkInformation oldInfo, PatchFileFactory.BenchmarkInformation newInfo) {
        this.statements = statements.stream().filter(Objects::nonNull).collect(Collectors.toList());
        this.oldInfo = oldInfo;
        this.newInfo = newInfo;
    }

    public PatchFileFactory.BenchmarkInformation getOldInfo() {
        return oldInfo;
    }

    public PatchFileFactory.BenchmarkInformation getNewInfo() {
        return newInfo;
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
        return JSONBuild.createObject()
                .add("statements",
                        JSONBuild.fromCollection(this.statements.stream().map((patchStatement) -> patchStatement.toJSONObject(newInfo, oldInfo)).collect(Collectors.toList())))
                .add("newName", newInfo.benchMark.name)
                .add("oldName", oldInfo.benchMark.name)
                .add("oldDeclaration", oldInfo.printedDeclaration)
                .add("newDeclaration", newInfo.printedDeclaration)
                .add("newDecAvailable", newInfo.benchMark.declarationPath != null)
                .build();
    }
}
