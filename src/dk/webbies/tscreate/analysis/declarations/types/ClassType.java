package dk.webbies.tscreate.analysis.declarations.types;

import dk.au.cs.casa.typescript.types.GenericType;
import dk.au.cs.casa.typescript.types.Type;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.jsnap.Snap;
import dk.webbies.tscreate.jsnap.classes.LibraryClass;
import dk.webbies.tscreate.util.Util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Erik Krogh Kristensen on 17-09-2015.
 */
public class ClassType extends DeclarationType{
    private DeclarationType constructorType;
    private Map<String, DeclarationType> prototypeFields;
    private Map<String, DeclarationType> staticFields;
    private String name;
    public DeclarationType superClass;
    private LibraryClass libraryClass;
    private final ClassInstanceType instance = new ClassInstanceType(this, Collections.EMPTY_SET);

    public ClassType(String name, DeclarationType constructorType, Map<String, DeclarationType> properties, Map<String, DeclarationType> staticFields, LibraryClass libraryClass) {
        super(Collections.EMPTY_SET); // <- Got the name elsewhere.
        this.constructorType = constructorType;
        this.prototypeFields = properties;
        this.name = name;
        this.staticFields = staticFields;
        this.libraryClass = libraryClass;
    }

    public ClassInstanceType getEmptyNameInstance() {
        return this.instance;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSuperClass(DeclarationType superClass) {
        this.superClass = superClass;
    }

    // Typecasting this, because it is unresolvedType for a while.
    public FunctionType getConstructorType() {
        return (FunctionType) constructorType.resolve();
    }

    public Map<String, DeclarationType> getPrototypeFields() {
        return prototypeFields;
    }

    public void setPrototypeFields(Map<String, DeclarationType> prototypeFields) {
        this.prototypeFields = prototypeFields;
    }

    public void setStaticFields(Map<String, DeclarationType> staticFields) {
        this.staticFields = staticFields;
    }

    public void setConstructorType(DeclarationType constructorType) {
        this.constructorType = constructorType;
    }

    public Map<String, DeclarationType> getStaticFields() {
        return staticFields;
    }

    public DeclarationType getSuperClass() {
        DeclarationType resolved = DeclarationType.resolve(superClass);
        if (resolved instanceof NamedObjectType && ((NamedObjectType) resolved).getName().endsWith("Constructor")) {
            String name = ((NamedObjectType) resolved).getName();
            name = Util.removeSuffix(name, "Constructor");
            resolved = new NamedObjectType(name, false);
        }
        this.superClass = resolved;
        return resolved;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(DeclarationTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <T, A> T accept(DeclarationTypeVisitorWithArgument<T, A> visitor, A argument) {
        return visitor.visit(this, argument);
    }

    public LibraryClass getLibraryClass() {
        return libraryClass;
    }

    public static Set<String> getStaticFieldsInclSuper(DeclarationType superClass, DeclarationParser.NativeClassesMap nativeClasses) {
        Set<String> fieldsInSuper = new HashSet<>();
        while (superClass != null) {
            if (superClass instanceof ClassType) {
                fieldsInSuper.addAll(((ClassType)superClass).getStaticFields().keySet());
                superClass = ((ClassType)superClass).getSuperClass();
            } else if (superClass instanceof NamedObjectType) {
                Snap.Obj proto = nativeClasses.prototypeFromName(((NamedObjectType) superClass).getName());
                while (proto != null && proto != proto.prototype) {
                    Snap.Property constructorProp = proto.getProperty("constructor");
                    if (constructorProp != null && constructorProp.value instanceof Snap.Obj) {
                        Snap.Obj constructor = (Snap.Obj) constructorProp.value;
                        fieldsInSuper.addAll(constructor.getPropertyMap().keySet());
                    }
                    proto = proto.prototype;
                }
                break;
            } else {
                throw new RuntimeException();
            }
        }
        return fieldsInSuper;
    }


    public static Set<String> getFieldsInclSuper(DeclarationType superClass, DeclarationParser.NativeClassesMap nativeClasses) {
        Set<String> fieldsInSuper = new HashSet<>();
        while (superClass != null) {
            if (superClass instanceof ClassType) {
                fieldsInSuper.addAll(((ClassType)superClass).getPrototypeFields().keySet());
                superClass = ((ClassType)superClass).getSuperClass();
            } else if (superClass instanceof NamedObjectType) {
                Set<String> typeKeys = keysFrom(nativeClasses.typeFromName(((NamedObjectType) superClass).getName()));
                fieldsInSuper.addAll(typeKeys);

                Snap.Obj proto = nativeClasses.prototypeFromName(((NamedObjectType) superClass).getName());
                while (proto != null && proto != proto.prototype) {
                    fieldsInSuper.addAll(proto.getPropertyMap().keySet());
                    proto = proto.prototype;
                }
                break;
            } else {
                throw new RuntimeException();
            }
        }
        return fieldsInSuper;
    }

    private static Set<String> keysFrom(Type type) {
        if (type instanceof dk.au.cs.casa.typescript.types.InterfaceType) {
            dk.au.cs.casa.typescript.types.InterfaceType interfaceType = (dk.au.cs.casa.typescript.types.InterfaceType) type;
            HashSet<String> result = new HashSet<>();
            result.addAll(interfaceType.getDeclaredProperties().keySet());
            interfaceType.getBaseTypes().forEach(base -> result.addAll(keysFrom(base)));
            return result;
        } else if (type instanceof GenericType) {
            return keysFrom(((GenericType) type).toInterface());
        }
        throw new RuntimeException("Not yet! " + type.getClass().getSimpleName());
    }
}
