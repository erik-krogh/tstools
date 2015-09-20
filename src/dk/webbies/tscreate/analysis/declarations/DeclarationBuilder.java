package dk.webbies.tscreate.analysis.declarations;

import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.TypeAnalysis;
import dk.webbies.tscreate.analysis.TypeFactory;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class DeclarationBuilder {
    private final Snap.Obj librarySnap;
    private final TypeFactory typeFactory;

    public DeclarationBuilder(Snap.Obj librarySnap, HashMap<Snap.Obj, LibraryClass> libraryClasses, Options options, Snap.Obj globalObject) {
        this.librarySnap = librarySnap;
        TypeAnalysis typeAnalysis = new TypeAnalysis(libraryClasses, options, globalObject);
        this.typeFactory = typeAnalysis.getTypeFactory();
    }

    public Map<String, DeclarationType> buildDeclaration() {
        Map<String, DeclarationType> declarationBlock = buildDeclaration(this.librarySnap);
        resolveTypes(declarationBlock);
        return declarationBlock;
    }


    private Map<String, DeclarationType> buildDeclaration(Snap.Obj obj) {
        Map<String, DeclarationType> declarations = new HashMap<>();

        for (Snap.Property property : obj.properties) {
            if (obj.function != null) {
                if (property.name.equals("length") || property.name.equals("name") || property.name.equals("prototype")) {
                    continue;
                }
            }
            Snap.Value value = property.value;
            if (value == null) {
                continue;
            }
            if (value instanceof Snap.BooleanConstant) {
                declarations.put(property.name, PrimitiveDeclarationType.BOOLEAN);
            } else if (value instanceof Snap.NumberConstant) {
                declarations.put(property.name, PrimitiveDeclarationType.NUMBER);
            } else if (value instanceof Snap.StringConstant) {
                declarations.put(property.name, PrimitiveDeclarationType.STRING);
            } else if (value instanceof Snap.UndefinedConstant) {
                declarations.put(property.name, PrimitiveDeclarationType.UNDEFINED);
            } else {
                declarations.put(property.name, typeFactory.getType(value));
            }
        }

        return declarations;
    }

    private static void resolveTypes(Map<String, DeclarationType> types) {
        resolveTypes(types, new HashSet<>());
    }

    private static void resolveTypes(Map<String, DeclarationType> types, Set<DeclarationType> seen) {
        for (Map.Entry<String, DeclarationType> entry : new HashSet<>(types.entrySet())) {
            if (entry.getValue() instanceof UnresolvedDeclarationType) {
                types.put(entry.getKey(), ((UnresolvedDeclarationType) entry.getValue()).getResolvedType());
            }
            entry.getValue().accept(new ResolveDeclarationTypeVisitor(seen));
        }
    }

    private static class ResolveDeclarationTypeVisitor implements DeclarationTypeVisitor<Void> {
        private Set<DeclarationType> seen;

        public ResolveDeclarationTypeVisitor(Set<DeclarationType> seen) {
            this.seen = seen;
        }

        @Override
        public Void visit(FunctionType functionType) {
            if (seen.contains(functionType)) {
                return null;
            }
            seen.add(functionType);

            if (functionType.getReturnType() instanceof UnresolvedDeclarationType) {
                functionType.setReturnType(((UnresolvedDeclarationType) functionType.getReturnType()).getResolvedType());
            }

            functionType.getReturnType().accept(this);

            List<FunctionType.Argument> arguments = functionType.getArguments();
            for (int i = 0; i < arguments.size(); i++) {
                FunctionType.Argument argument = arguments.get(i);
                if (argument.getType() instanceof UnresolvedDeclarationType) {
                    argument.setType(((UnresolvedDeclarationType) argument.getType()).getResolvedType());
                }
                argument.getType().accept(this);
            }
            return null;
        }

        @Override
        public Void visit(PrimitiveDeclarationType primitive) {
            // Already done
            return null;
        }

        @Override
        public Void visit(UnnamedObjectType objectType) {
            if (seen.contains(objectType)) {
                return null;
            }
            seen.add(objectType);

            resolveTypes(objectType.getDeclarations(), seen);
            return null;
        }

        @Override
        public Void visit(InterfaceType interfaceType) {
            if (interfaceType.function != null) {
                if (interfaceType.function instanceof UnresolvedDeclarationType) {
                    interfaceType.function = ((UnresolvedDeclarationType) interfaceType.function).getResolvedType();
                }
                interfaceType.getFunction().accept(this);
            }
            if (interfaceType.object != null) {
                if (interfaceType.object instanceof UnresolvedDeclarationType) {
                    interfaceType.object = ((UnresolvedDeclarationType) interfaceType.object).getResolvedType();
                }
                interfaceType.getObject().accept(this);
            }
            return null;
        }

        @Override
        public Void visit(UnionDeclarationType union) {
            if (seen.contains(union)) {
                return null;
            }
            seen.add(union);

            ArrayList<DeclarationType> types = union.getTypes();
            for (int i = 0; i < types.size(); i++) {
                DeclarationType type = types.get(i);
                if (type instanceof UnresolvedDeclarationType) {
                    types.set(i, ((UnresolvedDeclarationType) type).getResolvedType());
                }
                types.get(i).accept(this);
            }
            return null;
        }

        @Override
        public Void visit(NamedObjectType namedObjectType) {
            // Nothing here.
            return null;
        }

        @Override
        public Void visit(ClassType classType) {
            if (seen.contains(classType)) {
                return null;
            }
            seen.add(classType);

            if (classType.constructorType instanceof UnresolvedDeclarationType) {
                classType.constructorType = ((UnresolvedDeclarationType) classType.constructorType).getResolvedType();
            }
            classType.constructorType.accept(this);

            new HashMap<>(classType.getProperties()).forEach((name, type) -> {
                if (type instanceof UnresolvedDeclarationType) {
                    classType.getProperties().put(name, ((UnresolvedDeclarationType) type).getResolvedType());
                }
                type.accept(this);
            });

            if (classType.superClass != null) {
                if (classType.superClass instanceof UnresolvedDeclarationType) {
                    classType.superClass = ((UnresolvedDeclarationType) classType.superClass).getResolvedType();
                }
                classType.superClass.accept(this);
            }

            return null;
        }

        @Override
        public Void visit(ClassInstanceType instanceType) {
            if (instanceType.clazz instanceof UnresolvedDeclarationType) {
                instanceType.clazz = ((UnresolvedDeclarationType) instanceType.clazz).getResolvedType();
            }
            instanceType.clazz.accept(this);
            return null;
        }
    }
}
