package dk.webbies.tscreate.cleanup;

import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.analysis.declarations.types.ClassType;
import dk.webbies.tscreate.analysis.declarations.types.InterfaceType;

import java.util.*;

/**
 * Created by erik1 on 10-03-2016.
 */
public class CollectEveryTypeVisitor implements DeclarationTypeVisitor<Void> {
    private Set<DeclarationType> everyThing = new HashSet<>();
    private Map<Class<? extends DeclarationType>, Set<DeclarationType>> everythingByType = new HashMap<>();

    public CollectEveryTypeVisitor(Collection<DeclarationType> values) {
        values.forEach(dec -> dec.accept(this));
    }

    private void add(DeclarationType type) {
        everyThing.add(type);
        if (!everythingByType.containsKey(type.getClass())) {
            everythingByType.put(type.getClass(), new HashSet<>());
        }
        everythingByType.get(type.getClass()).add(type);
    }

    public Set<DeclarationType> getEveryThing() {
        return everyThing;
    }

    public Map<Class<? extends DeclarationType>, Set<DeclarationType>> getEverythingByType() {
        return everythingByType;
    }

    @Override
    public Void visit(FunctionType functionType) {
        if (everyThing.contains(functionType)) {
            return null;
        }
        add(functionType);
        functionType.getArguments().forEach(arg -> arg.getType().accept(this));
        functionType.getReturnType().accept(this);
        return null;
    }

    @Override
    public Void visit(PrimitiveDeclarationType primitive) {
        if (everyThing.contains(primitive)) {
            return null;
        }
        add(primitive);
        return null;
    }

    @Override
    public Void visit(UnnamedObjectType objectType) {
        if (everyThing.contains(objectType)) {
            return null;
        }
        add(objectType);
        objectType.getDeclarations().values().forEach(dec -> dec.accept(this));
        return null;
    }

    @Override
    public Void visit(InterfaceType interfaceType) {
        if (everyThing.contains(interfaceType)) {
            return null;
        }
        add(interfaceType);

        // This is special, because the interfaceType is one big type, and I'm not interrested in the Object part of the interface type, I'm interested in the interface.
        // That is why there is nothing for dynamic access types.
        if (interfaceType.getFunction() != null) {
            interfaceType.getFunction().getReturnType().accept(this);
            interfaceType.getFunction().getArguments().forEach(arg -> arg.getType().accept(this));
        }
        if (interfaceType.getObject() != null) {
            interfaceType.getObject().getDeclarations().values().forEach(dec -> dec.accept(this));
        }
        return null;
    }

    @Override
    public Void visit(UnionDeclarationType type) {
        if (everyThing.contains(type)) {
            return null;
        }
        add(type);
        type.getTypes().forEach(dec -> dec.accept(this));
        return null;
    }

    @Override
    public Void visit(NamedObjectType type) {
        if (everyThing.contains(type)) {
            return null;
        }
        add(type);
        return null;
    }

    @Override
    public Void visit(ClassType type) {
        if (everyThing.contains(type)) {
            return null;
        }
        add(type);
        if (type.getSuperClass() != null) {
            type.getSuperClass().accept(this);
        }
        type.getConstructorType().accept(this);
        type.getPrototypeFields().values().forEach(dec -> dec.accept(this));
        type.getStaticFields().values().forEach(dec -> dec.accept(this));
        return null;
    }

    @Override
    public Void visit(ClassInstanceType type) {
        if (everyThing.contains(type)) {
            return null;
        }
        add(type);
        type.getClazz().accept(this);
        return null;
    }
}
