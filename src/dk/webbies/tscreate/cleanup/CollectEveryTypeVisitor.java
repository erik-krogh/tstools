package dk.webbies.tscreate.cleanup;

import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.analysis.declarations.types.ClassType;
import dk.webbies.tscreate.analysis.declarations.types.InterfaceDeclarationType;

import java.util.*;

/**
 * Created by erik1 on 10-03-2016.
 */
public class CollectEveryTypeVisitor implements DeclarationTypeVisitor<Void> {
    private Set<DeclarationType> everyThing = new HashSet<>();
    private Map<Class<? extends DeclarationType>, Set<DeclarationType>> everythingByType = new HashMap<>();
    private final Map<String, DeclarationType> declarations;

    public CollectEveryTypeVisitor(Map<String, DeclarationType> declarations, boolean dontCollectHeapTypes) {
        this.declarations = declarations;
        declarations.values().forEach(dec -> dec.accept(this));
        if (dontCollectHeapTypes) {
            HeapTypeCollector heapTypeCollector = new HeapTypeCollector();
            declarations.values().forEach(dec -> dec.accept(heapTypeCollector));
            Set<DeclarationType> heapTypes = heapTypeCollector.collected;
            heapTypes.forEach(everyThing::remove);
            heapTypes.forEach(type -> {
                everythingByType.get(type.getClass()).remove(type);
            });
        }
    }

    public Map<String, DeclarationType> getDeclarations() {
        return declarations;
    }

    private static final class HeapTypeCollector implements DeclarationTypeVisitor<Void> {
        Set<DeclarationType> collected = new HashSet<>();

        @Override
        public Void visit(FunctionType functionType) {
            collected.add(functionType);
            return null;
        }

        @Override
        public Void visit(PrimitiveDeclarationType primitive) {
            collected.add(primitive);
            return null;
        }

        @Override
        public Void visit(UnnamedObjectType objectType) {
            if (collected.contains(objectType)) {
                return null;
            }
            collected.add(objectType);
            objectType.getDeclarations().values().forEach(dec -> dec.accept(this));
            return null;
        }

        @Override
        public Void visit(InterfaceDeclarationType interfaceType) {
            if (collected.contains(interfaceType)) {
                return null;
            }
            collected.add(interfaceType);
            if (interfaceType.getObject() != null) {
                interfaceType.getObject().accept(this);
            }
            if (interfaceType.getFunction() != null) {
                interfaceType.getFunction().accept(this);
            }
            if (interfaceType.getDynamicAccess() != null) {
                interfaceType.getDynamicAccess().getLookupType().accept(this);
                interfaceType.getDynamicAccess().getReturnType().accept(this);
            }
            return null;
        }

        @Override
        public Void visit(UnionDeclarationType union) {
            // Union types that are accessible through the global object, I think I'll let this run through the redundant-thinghy.
            return null;
        }

        @Override
        public Void visit(NamedObjectType namedObjectType) {
            collected.add(namedObjectType);
            return null;
        }

        @Override
        public Void visit(ClassType classType) {
            if (collected.contains(classType)) {
                return null;
            }
            collected.add(classType);
            classType.getConstructorType().accept(this);
            classType.getStaticFields().values().forEach(dec -> dec.accept(this));
            return null;
        }

        @Override
        public Void visit(ClassInstanceType instanceType) {
            collected.add(instanceType);
            return null;
        }
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
    public Void visit(InterfaceDeclarationType interfaceType) {
        if (everyThing.contains(interfaceType)) {
            return null;
        }
        add(interfaceType);

        // This is special, because the interfaceType is one big type, and I'm not interrested in the Object part of the interface type, I'm interested in the interface.
        if (interfaceType.dynamicAccess != null) {
            interfaceType.getDynamicAccess().getReturnType().accept(this);
        }
        if (interfaceType.function != null) {
            interfaceType.getFunction().getReturnType().accept(this);
            interfaceType.getFunction().getArguments().forEach(arg -> arg.getType().accept(this));
        }
        if (interfaceType.object != null) {
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
        if (type.getIndexType() != null) {
            type.getIndexType().accept(this);
        }
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
