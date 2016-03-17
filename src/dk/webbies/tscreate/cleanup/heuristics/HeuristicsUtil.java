package dk.webbies.tscreate.cleanup.heuristics;

import com.google.common.collect.ArrayListMultimap;
import dk.webbies.tscreate.analysis.declarations.types.DeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.InterfaceDeclarationType;
import dk.webbies.tscreate.analysis.declarations.types.UnnamedObjectType;

/**
 * Created by Erik Krogh Kristensen on 16-03-2016.
 */
public class HeuristicsUtil {
    public static int numberOfFields(DeclarationType type) {
        if (type instanceof UnnamedObjectType) {
            return ((UnnamedObjectType) type).getDeclarations().size();
        } else if (type instanceof InterfaceDeclarationType) {
            UnnamedObjectType object = ((InterfaceDeclarationType) type).getObject();
            if (object == null) {
                return 0;
            }
            return numberOfFields(object);
        }
        throw new RuntimeException("Whut?");
    }

    public static boolean hasDynAccess(DeclarationType type) {
        if (type instanceof UnnamedObjectType) {
            return false;
        } else if (type instanceof InterfaceDeclarationType) {
            return ((InterfaceDeclarationType) type).getDynamicAccess() != null;
        }
        throw new RuntimeException("Wut?!?");
    }

    public static boolean hasObject(DeclarationType type) {
        if (type instanceof UnnamedObjectType) {
            return true;
        } else if (type instanceof InterfaceDeclarationType) {
            return ((InterfaceDeclarationType) type).getObject() != null;
        }
        throw new RuntimeException("What???");
    }

    public static void combine(DeclarationType inter1, DeclarationType inter2, ArrayListMultimap<DeclarationType, DeclarationType> replacements) {
        replacements.put(inter1, inter2);
        replacements.put(inter2, inter1);
    }

    public static boolean hasFunction(DeclarationType type) {
        if (type instanceof UnnamedObjectType) {
            return false;
        } else if (type instanceof InterfaceDeclarationType) {
            return ((InterfaceDeclarationType) type).getFunction() != null;
        }
        throw new RuntimeException("Wit!?!?");
    }
}
