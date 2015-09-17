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

    public DeclarationBlock buildDeclaration() {
        return resolveTypes(buildDeclaration(this.librarySnap));
    }

    private static Map<Snap.Obj, DeclarationBlock> cache = new HashMap<>();

    private DeclarationBlock buildDeclaration(Snap.Obj obj) {
        if (cache.containsKey(obj)) {
            return cache.get(obj);
        }
        ArrayList<Declaration> declarations = new ArrayList<>();
        DeclarationBlock result = new DeclarationBlock(declarations);

        cache.put(obj, result);

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
                declarations.add(new VariableDeclaration(property.name, PrimitiveDeclarationType.BOOLEAN));
            } else if (value instanceof Snap.NumberConstant) {
                declarations.add(new VariableDeclaration(property.name, PrimitiveDeclarationType.NUMBER));
            } else if (value instanceof Snap.StringConstant) {
                declarations.add(new VariableDeclaration(property.name, PrimitiveDeclarationType.STRING));
            } else if (value instanceof Snap.UndefinedConstant) {
                declarations.add(new VariableDeclaration(property.name, PrimitiveDeclarationType.UNDEFINED));
            } else {
                declarations.add(new VariableDeclaration(property.name, typeFactory.getType(value)));
            }
        }


        return result;
    }

    private DeclarationBlock resolveTypes(DeclarationBlock declarationBlock) {
        declarationBlock.accept(new ResolveDeclarationBlockVisitor());
        return declarationBlock;
    }

    private static class ResolveDeclarationBlockVisitor implements DeclarationVisitor<Void> {
        private Set<DeclarationType> seen = new HashSet<>();

        @Override
        public Void visit(DeclarationBlock block) {
            block.getDeclarations().forEach(dec -> dec.accept(this));
            return null;
        }

        @Override
        public Void visit(VariableDeclaration declaration) {
            if (declaration.getType() instanceof UnresolvedDeclarationType) {
                declaration.setType(((UnresolvedDeclarationType) declaration.getType()).getResolvedType());
            }
            declaration.getType().accept(new ResolverDeclarationTypeVisitor(seen));
            return null;
        }
    }

    private static class ResolverDeclarationTypeVisitor implements DeclarationTypeVisitor<Void> {
        private Set<DeclarationType> seen;

        public ResolverDeclarationTypeVisitor(Set<DeclarationType> seen) {
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

            objectType.getBlock().accept(new ResolveDeclarationBlockVisitor());
            return null;
        }

        @Override
        public Void visit(InterfaceType interfaceType) {
            // Already resolved.
            interfaceType.getFunction().accept(this);
            interfaceType.getObject().accept(this);
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

            if (classType.propertiesType instanceof UnresolvedDeclarationType) {
                classType.propertiesType = ((UnresolvedDeclarationType) classType.propertiesType).getResolvedType();
            }
            classType.propertiesType.accept(this);

            if (classType.superClass != null) {
                if (classType.superClass instanceof UnresolvedDeclarationType) {
                    classType.superClass = ((UnresolvedDeclarationType) classType.superClass).getResolvedType();
                }
                classType.superClass.accept(this);
            }

            return null;
        }

        @Override
        public Void visit(UnresolvedDeclarationType unresolved) {
            throw new RuntimeException();
        }
    }
}
