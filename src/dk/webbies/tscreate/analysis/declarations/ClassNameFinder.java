package dk.webbies.tscreate.analysis.declarations;

import dk.webbies.tscreate.analysis.declarations.types.*;
import fj.data.Set;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 21-12-2015.
 */
public final class ClassNameFinder implements DeclarationTypeVisitorWithArgument<Void, ClassNameFinder.Arg> {
    private final java.util.Set<DeclarationType> printsAsInterface;
    private Map<ClassType, String> classNames = new HashMap<>();

    public ClassNameFinder(Map<String, DeclarationType> declarations, Map<DeclarationType, InterfaceType> printsAsInterface) {
        this.printsAsInterface = printsAsInterface.keySet();
        for (Map.Entry<String, DeclarationType> entry : declarations.entrySet()) {
            String name = entry.getKey();
            DeclarationType type = entry.getValue();
            type.accept(this, new Arg(name, DeclarationPrinter.emptySet()));
        }
    }

    @Override
    public Void visit(FunctionType functionType, Arg argument) {
        return null;
    }

    @Override
    public Void visit(PrimitiveDeclarationType primitive, Arg argument) {
        return null;
    }

    @Override
    public Void visit(UnnamedObjectType object, Arg argument) {
        if (argument.seen.member(object)) {
            return null;
        }
        if (printsAsInterface.contains(object)) {
            return null;
        }
        for (Map.Entry<String, DeclarationType> entry : object.getDeclarations().entrySet()) {
            String name = entry.getKey();
            DeclarationType type = entry.getValue();
            type.accept(this, argument.cons(name, object));
        }

        return null;
    }

    @Override
    public Void visit(InterfaceType interfaceType, Arg argument) {
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
        if (classNames.containsKey(classType)) {
            String prevName = classNames.get(classType);
            if (arg.path.endsWith(classType.getName()) && !prevName.endsWith(classType.getName())) {
                classNames.put(classType, arg.path);
                return null;
            }
            int prevDots = StringUtils.countMatches(prevName, ".");
            int newDots = StringUtils.countMatches(arg.path, ".");
            if (newDots < prevDots) {
                classNames.put(classType, arg.path);
                return null;
            }
        } else {
            classNames.put(classType, arg.path);
        }
        return null;
    }

    @Override
    public Void visit(ClassInstanceType instanceType, Arg argument) {
        return null;
    }

    public Map<ClassType, String> getClassNames() {
        return classNames;
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
