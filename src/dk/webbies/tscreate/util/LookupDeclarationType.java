package dk.webbies.tscreate.util;

import dk.webbies.tscreate.analysis.declarations.types.*;
import fj.data.Array;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by erik1 on 20-06-2016.
 */
public class LookupDeclarationType implements DeclarationTypeVisitor<fj.data.List<DeclarationType>> {
    private String path;

    private static final boolean returnAnyOnNotFound = true; 
    
    public LookupDeclarationType(String path) {
        this.path = path;
    }

    public DeclarationType find(DeclarationType type) {
        fj.data.List<DeclarationType> list = findRecursive(type);
        if (list.isEmpty()) {
            throw new RuntimeException();
        }
        return list.head();
    }

    private fj.data.List<DeclarationType> findRecursive(DeclarationType type) {
        if (path.isEmpty()) {
            return nil().cons(type);
        }
        fj.data.List<DeclarationType> list = type.accept(this);
        if (list == null) {
            list = nil().cons((DeclarationType)null);
        }
        return list.cons(type);
    }

    public List<DeclarationType> findList(DeclarationType type) {
        fj.data.List<DeclarationType> functionalResult = findRecursive(type).reverse();
        ArrayList<DeclarationType> result = new ArrayList<>();
        while (!functionalResult.isEmpty()) {
            result.add(functionalResult.head());
            functionalResult = functionalResult.tail();
        }
        return result;
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
    
    private fj.data.List<DeclarationType> noneFound() {
        if (returnAnyOnNotFound) {
            return nil().cons(PrimitiveDeclarationType.Any(Collections.EMPTY_SET));
        } else {
            throw new RuntimeException();
        }
    }

    private fj.data.List<DeclarationType> nil() {
        return fj.data.List.nil();
    }

    @Override
    public fj.data.List<DeclarationType> visit(FunctionType functionType) {
        if (!this.path.startsWith("[function]")) {
            return noneFound();
        }
        if (this.path.equals("[function]")) {
            return nil().cons(functionType);
        }
        String path = Util.removePrefix(this.path, "[function].");
        if (path.startsWith("[arg")) {
            int argIndex = Integer.parseInt(Util.removePrefix(path, "[arg").substring(0, Util.removePrefix(path, "[arg").indexOf("]")));
            if (functionType.getArguments().size() <= argIndex) {
                return noneFound();
            }
            FunctionType.Argument subType = functionType.getArguments().get(argIndex);
            String subPath = rest(rest());
            return new LookupDeclarationType(subPath).findRecursive(subType.getType());
        } else if (path.startsWith("[return]")){
            return new LookupDeclarationType(rest(rest())).findRecursive(functionType.getReturnType());
        }
        return noneFound();
    }

    @Override
    public fj.data.List<DeclarationType> visit(PrimitiveDeclarationType primitive) {
        return noneFound();
    }

    @Override
    public fj.data.List<DeclarationType> visit(UnnamedObjectType objectType) {
        DeclarationType subType = objectType.getDeclarations().get(firstPart());
        return recurse(subType);
    }

    private fj.data.List<DeclarationType> recurse(DeclarationType subType) {
        if (subType == null) {
            return noneFound();
        }
        return new LookupDeclarationType(rest()).findRecursive(subType);
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
    public fj.data.List<DeclarationType> visit(InterfaceDeclarationType interfaceType) {
        if (path.startsWith("[function]")) {
            if (interfaceType.getFunction() == null) {
                return noneFound();
            }
            return findRecursive(interfaceType.getFunction());
        } else if (path.startsWith("[indexer]")) {
            if (interfaceType.getDynamicAccess() == null) {
                return noneFound();
            }
            return recurse(interfaceType.getDynamicAccess().getReturnType());
        } else {
            if (interfaceType.getObject() == null) {
                return noneFound();
            }
            return findRecursive(interfaceType.getObject());
        }
    }

    @Override
    public fj.data.List<DeclarationType> visit(UnionDeclarationType union) {
        List<fj.data.List<DeclarationType>> results = union.getTypes().stream().map(this::findRecursive).filter(Objects::nonNull).collect(Collectors.toList());
        if (results.isEmpty()) {
            return noneFound();
        }
        if (results.size() != 1) {
            return null;
        }
        return results.iterator().next();
    }

    @Override
    public fj.data.List<DeclarationType> visit(NamedObjectType namedObjectType) {
        return null;
    }

    @Override
    public fj.data.List<DeclarationType> visit(ClassType classType) {
        if (path.equals("[constructor].[return]")) {
            return nil().cons(classType.getEmptyNameInstance());
        }
        if (path.startsWith("[constructor].[return].")) {
            return new LookupDeclarationType(Util.removePrefix(path, "[constructor].[return].")).findRecursive(classType.getEmptyNameInstance());
        }
        if (path.equals("[constructor]")) {
            return nil().cons(classType);
        }
        if (path.startsWith("[constructor].[arg")) {
            String arg = firstPart(rest());
            int argIndex = Integer.parseInt(Util.removeSuffix(Util.removePrefix(arg, "[arg"), "]"));
            if (classType.getConstructorType().getArguments().size() <= argIndex) {
                return noneFound();
            }
            FunctionType.Argument subType = classType.getConstructorType().getArguments().get(argIndex);
            String subPath = rest(rest());
            return new LookupDeclarationType(subPath).findRecursive(subType.getType());
        }
        if (!classType.getStaticFields().containsKey(firstPart())) {
            return null;
        }
        return recurse(classType.getStaticFields().get(firstPart()));
    }

    @Override
    public fj.data.List<DeclarationType> visit(ClassInstanceType instanceType) {
        DeclarationType subType = instanceType.getClazz().getPrototypeFields().get(firstPart());
        return recurse(subType);
    }
}
