package dk.webbies.tscreate.analysis.declarations;

import dk.webbies.tscreate.analysis.declarations.types.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Erik Krogh Kristensen on 15-10-2015.
 */
public class DeclarationTypeUseCounter implements DeclarationTypeVisitor<Void> {
    private Map<DeclarationType, Integer> countMap = new HashMap<>();

    public Map<DeclarationType, Integer> getCountMap() {
        return countMap;
    }

    private boolean increment(DeclarationType type) {
        boolean result;
        if (this.countMap.containsKey(type)) {
            result = true;
            this.countMap.put(type, 1 + this.countMap.get(type));
        } else {
            result = false;
            this.countMap.put(type, 1);
        }
        return result;
    }

    @Override
    public Void visit(FunctionType functionType) {
        if (increment(functionType)) {
            return null;
        }
        functionType.getArguments().forEach(arg -> arg.getType().accept(this));
        functionType.getReturnType().accept(this);
        return null;
    }

    @Override
    public Void visit(PrimitiveDeclarationType primitive) {
        return null;
    }

    @Override
    public Void visit(UnnamedObjectType objectType) {
        if (increment(objectType)) {
            return null;
        }
        objectType.getDeclarations().values().forEach(type -> type.accept(this));
        return null;
    }

    @Override
    public Void visit(InterfaceType interfaceType) {
        if (increment(interfaceType)) {
            return null;
        }
        if (interfaceType.object != null) {
            interfaceType.object.accept(this);
        }
        if (interfaceType.function != null) {
            interfaceType.function.accept(this);
        }
        if (interfaceType.getDynamicAccess() != null) {
            interfaceType.getDynamicAccess().getReturnType().accept(this);
            interfaceType.getDynamicAccess().getLookupType().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(UnionDeclarationType union) {
        if (increment(union)) {
            return null;
        }
        for (DeclarationType type : union.getTypes()) {
            type.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(NamedObjectType namedObjectType) {
        return null;
    }

    @Override
    public Void visit(ClassType classType) {
        if (increment(classType)) {
            return null;
        }
        classType.getConstructorType().accept(this);
        classType.getPrototypeFields().values().forEach(type -> type.accept(this));
        classType.getStaticFields().values().forEach(type -> type.accept(this));
        if (classType.getSuperClass() != null) {
            classType.getSuperClass().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ClassInstanceType instanceType) {
        if (increment(instanceType)) {
            return null;
        }
        instanceType.getClazz().accept(this);
        return null;
    }
}
