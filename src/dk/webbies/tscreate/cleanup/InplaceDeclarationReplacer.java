package dk.webbies.tscreate.cleanup;

import com.google.common.collect.Multimap;
import dk.webbies.tscreate.analysis.declarations.typeCombiner.TypeReducer;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.util.Tarjan;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by erik1 on 10-03-2016.
 *
 * Important note about this class, it does not have to do anything recursively, it is exposed to all the types one by one.
 */
public class InplaceDeclarationReplacer implements DeclarationTypeVisitor<Void> {
    private CollectEveryTypeVisitor collector;
    private TypeReducer reducer;
    private Map<String, DeclarationType> declarations;
    private final Function<DeclarationType, DeclarationType> cleanerFunction;
    private Multimap<DeclarationType, DeclarationType> replacements;

    public InplaceDeclarationReplacer(Multimap<DeclarationType, DeclarationType> replacements, CollectEveryTypeVisitor collector, TypeReducer reducer, Map<String, DeclarationType> declarations) {
        this(replacements, collector, reducer, declarations, Function.identity());
    }

    public InplaceDeclarationReplacer(Multimap<DeclarationType, DeclarationType> replacements, CollectEveryTypeVisitor collector, TypeReducer reducer, Map<String, DeclarationType> declarations, Function<DeclarationType, DeclarationType> cleaner) {
        this.replacements = replacements;
        this.collector = collector;
        this.reducer = reducer;
        this.declarations = declarations;
        this.cleanerFunction = cleaner;
    }

    private Map<DeclarationType, TypeNode> cache = new HashMap<>();
    private TypeNode getNode(DeclarationType type) {
        if (!cache.containsKey(type)) {
            cache.put(type, new TypeNode(type, replacements));
        }
        return cache.get(type);
    }

    public void cleanStuff() {
        List<List<TypeNode>> cycles = new Tarjan<TypeNode>().getSCComponents(collector.getEveryThing().stream().map(this::getNode).collect(Collectors.toList()));
        for (List<TypeNode> cycleNodes : cycles) {
            if (cycleNodes.size() <= 1) {
                continue;
            }
            List<DeclarationType> cycle = cycleNodes.stream().map(TypeNode::getType).collect(Collectors.toList());
            cycle.forEach(replacements::removeAll);

            DeclarationType result = new CombinationType(reducer, cycle).getCombined();
            result.accept(collector);
            cycle.forEach(type -> replacements.put(type, result));
        }

        collector.getEveryThing().forEach(dec -> dec.accept(this));
        declarations.entrySet().stream().forEach(entry -> declarations.put(entry.getKey(), this.findReplacement(entry.getValue())));
    }

    private final class TypeNode extends Tarjan.Node<TypeNode> {
        private final DeclarationType type;
        private Multimap<DeclarationType, DeclarationType> replacements;

        private TypeNode(DeclarationType type, Multimap<DeclarationType, DeclarationType> replacements) {
            this.type = type;
            this.replacements = replacements;
        }

        @Override
        public Collection<TypeNode> getEdges() {
            if (this.replacements.containsKey(this.type)) {
                return this.replacements.get(this.type).stream().map(InplaceDeclarationReplacer.this::getNode).collect(Collectors.toList());
            }
            //noinspection unchecked
            return Collections.EMPTY_LIST;
        }

        public DeclarationType getType() {
            return this.type;
        }
    }

    private DeclarationType findReplacement(DeclarationType type) {
        if (type == null) {
            return null;
        }
        type = cleanerFunction.apply(type.resolve());
        if (replacements.containsKey(type)) {
            Collection<DeclarationType> foundReplacements = replacements.get(type);
            DeclarationType next;
            if (foundReplacements.size() == 1) {
                next = foundReplacements.iterator().next();
            } else {
                next = new CombinationType(reducer, foundReplacements).getCombined();
            }
            if (next == type) {
                return next;
            }
            return findReplacement(next);
        } else {
            return type;
        }
    }

    @Override
    public Void visit(FunctionType functionType) {
        for (FunctionType.Argument argument : functionType.getArguments()) {
            argument.setType(findReplacement(argument.getType()));
        }
        functionType.setReturnType(findReplacement(functionType.getReturnType()));
        return null;
    }

    @Override
    public Void visit(PrimitiveDeclarationType primitive) {
        return null;
    }

    @Override
    public Void visit(UnnamedObjectType objectType) {
        objectType.setDeclarations(objectType.getDeclarations().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry) -> findReplacement(entry.getValue()))));
        return null;
    }

    @Override
    public Void visit(InterfaceDeclarationType interfaceType) {
        DynamicAccessType dynamicAccess = interfaceType.getDynamicAccess();
        if (dynamicAccess != null) {
            dynamicAccess.setLookupType(findReplacement(dynamicAccess.getLookupType()));
            dynamicAccess.setReturnType(findReplacement(dynamicAccess.getReturnType()));
        }
        if (interfaceType.getObject() != null) {
            interfaceType.getObject().accept(this);
        }
        if (interfaceType.getFunction() != null) {
            interfaceType.getFunction().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(UnionDeclarationType union) {
        union.setTypes(union.getTypes().stream().map(this::findReplacement).collect(Collectors.toList()));
        return null;
    }

    @Override
    public Void visit(NamedObjectType named) {
        named.setIndexType(findReplacement(named.getIndexType()));
        return null;
    }

    @Override
    public Void visit(ClassType classType) {
        classType.setPrototypeFields(classType.getPrototypeFields().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry) -> findReplacement(entry.getValue()))));
        classType.setStaticFields(classType.getStaticFields().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry) -> findReplacement(entry.getValue()))));
        classType.setConstructorType(findReplacement(classType.getConstructorType()));
        classType.setSuperClass(findReplacement(classType.getSuperClass()));
        return null;
    }

    @Override
    public Void visit(ClassInstanceType instanceType) {
        instanceType.setClazz(findReplacement(instanceType.getClazz()));
        return null;
    }
}
