package dk.webbies.tscreate.analysis.declarations;

import dk.webbies.tscreate.analysis.declarations.types.*;
import fj.data.Set;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 21-12-2015.
 */
public final class ClassNameFinder implements DeclarationTypeVisitorWithArgument<Void, ClassNameFinder.Arg> {
    private final java.util.Set<DeclarationType> printsAsInterface;
    private Map<DeclarationType, String> declarationNames = new HashMap<>();

    public ClassNameFinder(Map<String, DeclarationType> declarations, Map<DeclarationType, InterfaceDeclarationType> printsAsInterface) {
        this.printsAsInterface = printsAsInterface.keySet();
        for (Map.Entry<String, DeclarationType> entry : declarations.entrySet()) {
            String name = entry.getKey();
            DeclarationType type = entry.getValue();
            type.accept(this, new Arg(name, DeclarationPrinter.emptySet()));
        }
    }

    @Override
    public Void visit(FunctionType functionType, Arg arg) {
        putNamedType(functionType, arg.path);
        return null;
    }

    @Override
    public Void visit(PrimitiveDeclarationType primitive, Arg argument) {
        return null;
    }

    @Override
    public Void visit(UnnamedObjectType object, Arg arg) {
        putNamedType(object, arg.path);
        if (arg.seen.member(object)) {
            return null;
        }
        if (printsAsInterface.contains(object)) {
            return null;
        }
        for (Map.Entry<String, DeclarationType> entry : object.getDeclarations().entrySet()) {
            String name = entry.getKey();
            DeclarationType type = entry.getValue();
            type.accept(this, arg.cons(name, object));
        }

        return null;
    }

    @Override
    public Void visit(InterfaceDeclarationType interfaceType, Arg argument) {
        return null;
    }

    @Override
    public Void visit(UnionDeclarationType union, Arg argument) {
        return null;
    }

    @Override
    public Void visit(NamedObjectType namedObjectType, Arg argument) {
        return null;
    }

    @Override
    public Void visit(ClassType classType, Arg arg) {
        putNamedType(classType, arg.path);
        return null;
    }

    private void putNamedType(DeclarationType type, String path) {
        if (declarationNames.containsKey(type)) {
            String prevName = declarationNames.get(type);
            if (type instanceof ClassType && path.endsWith(((ClassType)type).getName()) && !prevName.endsWith(((ClassType)type).getName())) {
                declarationNames.put(type, path);
                return;
            }
            int prevDots = StringUtils.countMatches(prevName, ".");
            int newDots = StringUtils.countMatches(path, ".");
            if (newDots < prevDots) {
                declarationNames.put(type, path);
                return;
            }
        } else {
            declarationNames.put(type, path);
        }
    }

    @Override
    public Void visit(ClassInstanceType instanceType, Arg argument) {
        return null;
    }

    Map<DeclarationType, String> getDeclarationNames() {
        // Making sure that also the super-classes have names.
        Map<DeclarationType, String> result = new HashMap<>(declarationNames);
        boolean change = true;
        while (change) {
            change = false;
            for (DeclarationType type : new ArrayList<>(result.keySet())) {
                if (!(type instanceof ClassType)) {
                    continue;
                }
                ClassType clazz = (ClassType) type;
                //noinspection SuspiciousMethodCalls
                if (clazz.getSuperClass() != null && !result.containsKey(clazz.getSuperClass())) {
                    result.remove(clazz);
                    change = true;
                }
            }
        }
        return result;
    }

    static final class Arg {
        final String path;
        final Set<DeclarationType> seen;

        Arg(String path, Set<DeclarationType> seen) {
            this.path = path;
            this.seen = seen;
        }

        Arg cons(String path, DeclarationType type) {
            return new Arg(this.path + "." + path, this.seen.insert(type));
        }
    }
}
