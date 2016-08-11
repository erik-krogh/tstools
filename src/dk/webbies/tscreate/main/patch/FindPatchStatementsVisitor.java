package dk.webbies.tscreate.main.patch;

import dk.au.cs.casa.typescript.types.SimpleType;
import dk.au.cs.casa.typescript.types.SimpleTypeKind;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.util.LookupType;
import dk.webbies.tscreate.Options;
import dk.webbies.tscreate.analysis.declarations.types.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.util.Pair;
import dk.webbies.tscreate.util.Util;

import java.util.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.EMPTY_SET;

/**
 * Created by erik1 on 14-06-2016.
 */
public class FindPatchStatementsVisitor implements DeclarationTypeVisitorWithArgument<Void, FindPatchStatementsVisitor.Argument> {
    static List<PatchStatement> generateStatements(UnnamedObjectType oldType, UnnamedObjectType newType, Options options, Type oldHandWritten, Type newHandWritten, DeclarationParser.NativeClassesMap nativeClasses) {
        FindPatchStatementsVisitor visitor = new FindPatchStatementsVisitor(nativeClasses, options, oldHandWritten, newHandWritten);
        visitor.queue.add(new QueueElement(oldType, new Argument("window", 0, fj.data.List.cons(newType, fj.data.List.nil()), newType)));

        runQueue(visitor);

        return visitor.foundStatements;
    }

    private static void runQueue(FindPatchStatementsVisitor visitor) {
        Set<Pair<DeclarationType, DeclarationType>> seen = new HashSet<>();

        while (!visitor.queue.isEmpty()) {
            QueueElement element = visitor.queue.poll();
            DeclarationType oldType = element.oldType.resolve();
            DeclarationType newType = element.arg.newType.resolve();
            if (seen.contains(new Pair<>(oldType, newType))) {
                continue;
            }
            seen.add(new Pair<>(oldType, newType));

            oldType.accept(visitor, element.arg);
        }
    }

    private final PriorityQueue<QueueElement> queue = new PriorityQueue<>();
    private final List<PatchStatement> foundStatements = new ArrayList<>();
    private final Options options;
    private final Type oldHandWrittenType;
    private final Type newHandWrittenType;
    private final DeclarationParser.NativeClassesMap nativeClasses;

    private FindPatchStatementsVisitor(DeclarationParser.NativeClassesMap nativeClasses, Options options, Type oldHandWritten, Type newHandWritten) {
        this.nativeClasses = nativeClasses;
        this.options = options;
        this.oldHandWrittenType = oldHandWritten;
        this.newHandWrittenType = newHandWritten;
    }


    @Override
    public Void visit(FunctionType oldType, Argument argument) {
        if (argument.newType instanceof FunctionType) {
            FunctionType newType = (FunctionType) argument.newType;

            int extraDepth = 1;
            if (options.evaluationPushFunctionReturnsDown) {
                extraDepth = 1000;
            }

            queue.add(new QueueElement(oldType.getReturnType(), argument.next(".[function].[return]", newType.getReturnType(), extraDepth)));

            List<FunctionType.Argument> oldArgs = oldType.getArguments();
            List<FunctionType.Argument> newArgs = newType.getArguments();
            if (oldArgs.size() != newArgs.size()) {
                foundStatements.add(new ChangedNumberOfArgumentsStatement(argument.path, oldType, newType, argument.getPrevType(1), oldArgs.size(), newArgs.size()));
            } else {
                for (int i = 0; i < Math.min(oldArgs.size(), newArgs.size()); i++) {
                    DeclarationType oldArg = oldArgs.get(i).getType();
                    DeclarationType newArg = newArgs.get(i).getType();
                    queue.add(new QueueElement(oldArg, argument.next(".[function].[arg" + i + "]", newArg, extraDepth)));
                }
            }
        } else {
            changedType(oldType, argument);
        }
        return null;
    }

    @Override
    public Void visit(PrimitiveDeclarationType oldType, Argument argument) {
        if (!(argument.newType instanceof PrimitiveDeclarationType && ((PrimitiveDeclarationType) argument.newType).getType() == oldType.getType())) {
            changedType(oldType, argument);
        }
        return null;
    }

    public void changedType(DeclarationType oldType, Argument argument) {
        DeclarationType newType = argument.newType;
        if (newType instanceof PrimitiveDeclarationType && ((PrimitiveDeclarationType) newType).getType() == PrimitiveDeclarationType.Type.NON_VOID) {
            return;
        }

        // If any of them are any (or NON_VOID), lookup in the old handwritten declaration file. It it states something specific, skip this one.
        if ((isAny(newType) || isAny(oldType)) && oldHandWrittenType != null) {
            Type handwrittenType = oldHandWrittenType.accept(new LookupType(Util.removePrefix(argument.path, "window.")));
            if (handwrittenType instanceof SimpleType) {
                SimpleType simple = (SimpleType) handwrittenType;
                if (simple.getKind() == SimpleTypeKind.Any || simple.getKind() == SimpleTypeKind.Null || simple.getKind() == SimpleTypeKind.Void || simple.getKind() == SimpleTypeKind.Undefined) {
                    return;
                }
            }
        }

        // If both are any/void, skip it.
        if ((isAny(newType) || isVoid(newType)) && (isAny(oldType) || isVoid(oldType))) {
            return;
        }

        foundStatements.add(new ChangedTypeStatement(argument.path, newType, oldType, argument.getPrevType(1)));
    }

    @Override
    public Void visit(UnnamedObjectType oldType, Argument argument) {
        if (argument.newType instanceof UnnamedObjectType) {
            UnnamedObjectType newType = (UnnamedObjectType) argument.newType;
            Map<String, DeclarationType> oldDecs = oldType.getDeclarations();
            Map<String, DeclarationType> newDecs = newType.getDeclarations();
            List<String> allKeys = Util.concat(oldDecs.keySet(), newDecs.keySet());

            for (String key : allKeys) {
                if (!newDecs.containsKey(key)) {
                    foundStatements.add(new RemovedPropertyStatement(argument.path, key, argument.getPrevType(0)));
                } else if (!oldDecs.containsKey(key)) {
                    foundStatements.add(new AddedPropertyStatement(argument.path, key, newDecs.get(key), argument.getPrevType(0)));
                } else {
                    DeclarationType newSubType = newDecs.get(key);
                    DeclarationType oldSubType = oldDecs.get(key);
                    queue.add(new QueueElement(oldSubType, argument.next("." + key, newSubType, 1)));
                }
            }
        } else {
            changedType(oldType, argument);
        }
        return null;
    }

    @Override
    public Void visit(InterfaceDeclarationType oldType, Argument argument) {
        if (argument.newType instanceof InterfaceDeclarationType) {
            InterfaceDeclarationType newType = (InterfaceDeclarationType) argument.newType;
            if ((oldType.getDynamicAccess() == null) != (newType.getDynamicAccess() == null)) {
                changedType(oldType, argument);
                return null;
            }

            if ((oldType.getFunction() == null) != (newType.getFunction() == null)) {
                changedType(oldType, argument);
                return null;
            }

            if ((oldType.getObject() == null) != (newType.getObject() == null)) {
                changedType(oldType, argument);
                return null;
            }

            if (oldType.getDynamicAccess() != null) {
                if (oldType.getDynamicAccess().isNumberIndexer() != newType.getDynamicAccess().isNumberIndexer()) {
                    changedType(oldType, argument);
                    return null;
                }
                queue.add(new QueueElement(oldType.getDynamicAccess().getReturnType(), argument.next(".[indexer]", newType.getDynamicAccess().getReturnType(), 1)));
            }

            if (oldType.getFunction() != null) {
                queue.add(new QueueElement(oldType.getFunction(), argument.nextKeepSubType("", newType.getFunction(), 0)));
            }

            if (oldType.getObject() != null) {
                queue.add(new QueueElement(oldType.getObject(), argument.nextKeepSubType("", newType.getObject(), 0)));
            }
        } else {
            changedType(oldType, argument);
        }
        return null;
    }

    @Override
    public Void visit(UnionDeclarationType oldType, Argument argument) {
        if (argument.newType instanceof UnionDeclarationType) {
            UnionDeclarationType newType = (UnionDeclarationType) argument.newType;
            // A simple heuristics, that make everything simpler. I just look 1 level down in both, and see if they are equal. If they are, i assume that things are OK.
            Set<String> oldTypeClasses = oldType.getTypes().stream().map(type -> type.getClass().getSimpleName()).collect(Collectors.toSet());
            Set<String> newTypeClasses = newType.getTypes().stream().map(type -> type.getClass().getSimpleName()).collect(Collectors.toSet());
            if (oldTypeClasses.size() != newTypeClasses.size() || Util.intersection(oldTypeClasses, newTypeClasses).size() != oldTypeClasses.size()) {
                changedType(oldType, argument);
            }
        } else {
            changedType(oldType, argument);
        }
        return null;
    }

    @Override
    public Void visit(NamedObjectType oldType, Argument argument) {
        DeclarationType newType = argument.newType;
        if (newType instanceof NamedObjectType && ((NamedObjectType) newType).getName().equals(oldType.getName())) {
            if (((NamedObjectType) newType).getName().equals("Array")) {
                DeclarationType oldIndex = oldType.getIndexType();
                if (oldIndex == null) {
                    oldIndex = PrimitiveDeclarationType.Void(Collections.EMPTY_SET);
                }
                DeclarationType newIndex = ((NamedObjectType) newType).getIndexType();
                if (newIndex == null) {
                    newIndex = PrimitiveDeclarationType.Void(Collections.EMPTY_SET);
                }
                queue.add(new QueueElement(oldIndex, argument.next(".[indexer]", newIndex, 1)));
            }
        } else {
            changedType(oldType, argument);
        }
        return null;
    }

    @Override
    public Void visit(ClassType oldType, Argument argument) {
        if (argument.newType instanceof ClassType) {
            ClassType newType = (ClassType) argument.newType;

            queue.add(new QueueElement(oldType.getConstructorType(), argument.nextKeepSubType(".[constructor]", newType.getConstructorType(), 1)));

            UnnamedObjectType oldStatic = new UnnamedObjectType(filterStaticFields(oldType.getStaticFields(), oldType.getSuperClass()), EMPTY_SET);
            UnnamedObjectType newStatic = new UnnamedObjectType(filterStaticFields(newType.getStaticFields(), newType.getSuperClass()), EMPTY_SET);

            queue.add(new QueueElement(oldStatic, argument.nextKeepSubType("", newStatic, 0)));

            UnnamedObjectType oldFields = new UnnamedObjectType(filterPrototypeFields(oldType.getPrototypeFields(), oldType.getSuperClass()), EMPTY_SET);
            UnnamedObjectType newFields = new UnnamedObjectType(filterPrototypeFields(newType.getPrototypeFields(), newType.getSuperClass()), EMPTY_SET);

            queue.add(new QueueElement(oldFields, argument.nextKeepSubType(".[constructor].[return]", newFields, 2)));

        } else {
            changedType(oldType, argument);
        }
        return null;
    }

    private Map<String, DeclarationType> filterPrototypeFields(Map<String, DeclarationType> fields, DeclarationType superClass) {
        return fields.entrySet().stream().filter(notInSuperClassTest(superClass)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, DeclarationType> filterStaticFields(Map<String, DeclarationType> fields, DeclarationType superClass) {
        return fields.entrySet().stream().filter(notStaticInSuperClassTest(superClass)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private <T> Predicate<Map.Entry<String, T>> notStaticInSuperClassTest(DeclarationType superClass) {
        Set<String> staticInSuper = ClassType.getStaticFieldsInclSuper(superClass, nativeClasses);
        return (entry) -> !staticInSuper.contains(entry.getKey());
    }

    private <T> Predicate<Map.Entry<String, T>> notInSuperClassTest(DeclarationType superClass) {
        Set<String> fieldsInSuper = ClassType.getFieldsInclSuper(superClass, nativeClasses);
        return (entry) -> !fieldsInSuper.contains(entry.getKey());
    }

    @Override
    public Void visit(ClassInstanceType oldType, Argument argument) {
        if (argument.newType instanceof ClassInstanceType) {
            ClassInstanceType newType = (ClassInstanceType) argument.newType;
            List<String> oldSeen = new ArrayList<>(oldType.getClazz().getLibraryClass().getPathsSeen());
            List<String> newSeen = new ArrayList<>(newType.getClazz().getLibraryClass().getPathsSeen());

            if (oldSeen.stream().anyMatch(path -> !path.startsWith("[ENV]")) && newSeen.stream().anyMatch(path -> !path.startsWith("[ENV]"))) {
                oldSeen = oldSeen.stream().filter(path -> !path.startsWith("[ENV]")).collect(Collectors.toList());
                newSeen = newSeen.stream().filter(path -> !path.startsWith("[ENV]")).collect(Collectors.toList());
            }

            // TODO: Try to see how many exists where the first path disagrees.

            List<String> sharedPaths = Util.intersection(oldSeen, newSeen);
            if (sharedPaths.isEmpty()) {
                changedType(oldType, argument);
                return null;
            }
            queue.add(new QueueElement(oldType.getClazz(), argument.next(".[typeof]", newType.getClazz(), 10000)));
        } else {
            changedType(oldType, argument);
        }
        return null;
    }

    public static final class Argument {
        final String path;
        final int depth;
        final fj.data.List<DeclarationType> prevTypes;
        final DeclarationType newType;

        private Argument(String path, int depth, fj.data.List<DeclarationType> prevTypes, DeclarationType newType) {
            this.path = path;
            this.depth = depth;
            this.prevTypes = prevTypes;
            this.newType = newType.resolve();
        }

        private DeclarationType getPrevType(int index) {
            if (index < prevTypes.length()) {
                return prevTypes.index(index);
            } else {
                return null;
            }
        }

        public Argument next(String extraPath, DeclarationType newSubType, int extraDepth) {
            return new Argument(path + extraPath, depth + extraDepth, prevTypes.cons(newSubType), newSubType);
        }

        public Argument nextKeepSubType(String extraPath, DeclarationType nextType, int extraDepth) {
            return new Argument(path + extraPath, depth + extraDepth, prevTypes, nextType);
        }
    }

    static final class QueueElement implements Comparable<QueueElement>{
        final DeclarationType oldType;
        final Argument arg;


        QueueElement(DeclarationType oldType, Argument arg) {
            this.oldType = oldType;
            this.arg = arg;

        }

        @Override
        public int compareTo(QueueElement other) {
            return Integer.compare(this.arg.depth, other.arg.depth);
        }
    }

    public static boolean isAny(DeclarationType type) {
        type = type.resolve();
        return type instanceof PrimitiveDeclarationType && (((PrimitiveDeclarationType) type).getType() == PrimitiveDeclarationType.Type.ANY || ((PrimitiveDeclarationType) type).getType() == PrimitiveDeclarationType.Type.NON_VOID);
    }

    public static boolean isVoid(DeclarationType type) {
        type = type.resolve();
        return type instanceof PrimitiveDeclarationType && (((PrimitiveDeclarationType) type).getType() == PrimitiveDeclarationType.Type.VOID);
    }
}
