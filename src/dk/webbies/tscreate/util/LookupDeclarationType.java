package dk.webbies.tscreate.util;

import dk.webbies.tscreate.analysis.declarations.types.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by erik1 on 20-06-2016.
 */
public class LookupDeclarationType implements DeclarationTypeVisitor<DeclarationType> {
    private String path;

    private LookupDeclarationType(String path) {
        this.path = path;
    }

    public DeclarationType find(DeclarationType type) {
        if (path.isEmpty()) {
            return type;
        }
        return type.accept(this);
    }

    String firstPart(String path) {
        if (path.indexOf('.') == -1) {
            return path;
        }
        return path.substring(0, path.indexOf('.'));
    }

    String firstPart() {
        return firstPart(this.path);
    }

    @Override
    public DeclarationType visit(FunctionType functionType) {
        if (!this.path.startsWith("[function]")) {
            throw new RuntimeException();
        }
        if (this.path.equals("[function]")) {
            return functionType;
        }
        String path = Util.removePrefix(this.path, "[function].");
        if (path.startsWith("[arg")) {
            int argIndex = Integer.parseInt(Util.removePrefix(path, "[arg").substring(0, Util.removePrefix(path, "[arg").indexOf("]")));
            if (functionType.getArguments().size() <= argIndex) {
                throw new RuntimeException();
            }
            FunctionType.Argument subType = functionType.getArguments().get(argIndex);
            String subPath = rest(rest());
            return new LookupDeclarationType(subPath).find(subType.getType());
        } else if (path.startsWith("[return]")){
            return new LookupDeclarationType(rest(rest())).find(functionType.getReturnType());
        }
        throw new RuntimeException();
    }

    @Override
    public DeclarationType visit(PrimitiveDeclarationType primitive) {
        throw new RuntimeException();
    }

    @Override
    public DeclarationType visit(UnnamedObjectType objectType) {
        DeclarationType subType = objectType.getDeclarations().get(firstPart());
        return recurse(subType);
    }

    private DeclarationType recurse(DeclarationType subType) {
        if (subType == null) {
            return null;
        }
        return new LookupDeclarationType(rest()).find(subType);
    }

    private String rest(String path) {
        if (path.indexOf('.') == -1) {
            return "";
        }
        return path.substring(path.indexOf('.') + 1, path.length());
    }

    private String rest() {
        return rest(this.path);
    }

    @Override
    public DeclarationType visit(InterfaceDeclarationType interfaceType) {
        throw new RuntimeException();
    }

    @Override
    public DeclarationType visit(UnionDeclarationType union) {
        List<DeclarationType> results = union.getTypes().stream().map(type -> type.accept(this)).filter(Objects::nonNull).collect(Collectors.toList());
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() != 1) {
            return null;
        }
        return results.iterator().next();
    }

    @Override
    public DeclarationType visit(NamedObjectType namedObjectType) {
        return null;
    }

    @Override
    public DeclarationType visit(ClassType classType) {
        if (path.equals("[constructor].[return]")) {
            return classType.getEmptyNameInstance();
        }
        if (path.startsWith("[constructor].[return].")) {
            return classType.getEmptyNameInstance().accept(new LookupDeclarationType(Util.removePrefix(path, "[constructor].[return].")));
        }
        if (path.equals("[constructor]")) {
            return classType;
        }
        if (path.startsWith("[constructor].[arg")) {
            String arg = firstPart(rest());
            int argIndex = Integer.parseInt(Util.removeSuffix(Util.removePrefix(arg, "[arg"), "]"));
            if (classType.getConstructorType().getArguments().size() <= argIndex) {
                throw new RuntimeException();
            }
            FunctionType.Argument subType = classType.getConstructorType().getArguments().get(argIndex);
            String subPath = rest(rest());
            return new LookupDeclarationType(subPath).find(subType.getType());
        }
        if (!classType.getStaticFields().containsKey(firstPart())) {
            return null;
        }
        return recurse(classType.getStaticFields().get(firstPart()));
    }

    @Override
    public DeclarationType visit(ClassInstanceType instanceType) {
        DeclarationType subType = instanceType.getClazz().getPrototypeFields().get(firstPart());
        return recurse(subType);
    }
}
