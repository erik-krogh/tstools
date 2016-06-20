package dk.webbies.tscreate.main.patch;

import com.google.gson.JsonObject;
import dk.au.cs.casa.typescript.types.InterfaceType;
import dk.webbies.tscreate.analysis.declarations.types.*;

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


}