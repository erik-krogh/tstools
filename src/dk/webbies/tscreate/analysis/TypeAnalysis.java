package dk.webbies.tscreate.analysis;

import dk.webbies.tscreate.analysis.unionFind.nodes.FunctionNode;
import dk.webbies.tscreate.analysis.unionFind.UnionFindSolver;
import dk.webbies.tscreate.analysis.unionFind.nodes.UnionNode;
import dk.webbies.tscreate.jsnapconvert.Snap;
import dk.webbies.tscreate.jsnapconvert.classes.LibraryClass;
import dk.webbies.tscreate.paser.*;
import dk.webbies.tscreate.analysis.declarations.types.FunctionType;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class TypeAnalysis {
    private final Snap.Obj librarySnap;
    private final HashMap<Snap.Obj, LibraryClass> classes;
    private final FunctionExpression program;

    public TypeAnalysis(Snap.Obj librarySnap, HashMap<Snap.Obj, LibraryClass> classes, FunctionExpression program) {
        this.librarySnap = librarySnap;
        this.classes = classes;
        this.program = program;
    }

    public Map<Snap.Obj, FunctionType> getFunctionTypes() {
        // TODO: All the classes first.
        List<Snap.Obj> functions = getAllFunctionInstances(librarySnap, new HashSet<>());
        HashMap<Snap.Obj, FunctionType> result = new HashMap<>();
        for (Snap.Obj function : functions) {
            result.put(function, analyse(function));
        }
        return result;
    }

    private FunctionType analyse(Snap.Obj function) {
        UnionFindSolver solver = new UnionFindSolver();
        Map<ProgramPoint, UnionNode> nodes = new HashMap<>();


        FunctionNode functionNode = new FunctionNode(function.function.astNode);
        function.function.astNode.accept(new AstConstraintVisitor(function, solver, nodes, functionNode));

        Map<UnionNode, List<UnionNode>> classes = solver.getUnionClasses();

        return FunctionType.fromNode(functionNode, classes);
    }

    static class ProgramPoint {
        private Snap.Obj function;
        private Node astNode;

        public ProgramPoint(Snap.Obj function, Node astNode) {
            if (function.function == null) {
                throw new RuntimeException("This should be a function");
            }
            this.function = function;
            this.astNode = astNode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProgramPoint that = (ProgramPoint) o;
            return Objects.equals(function, that.function) &&
                    Objects.equals(astNode, that.astNode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(function, astNode);
        }
    }

    private List<Snap.Obj> getAllFunctionInstances(Snap.Obj obj, HashSet<Snap.Obj> seen) {
        if (seen.contains(obj)) {
            return Collections.EMPTY_LIST;
        }
        seen.add(obj);

        ArrayList<Snap.Obj> result = new ArrayList<>();
        if (obj.function != null && obj.function.type.equals("user")) {
            result.add(obj);
        }
        for (Snap.Property property : obj.properties) {
            if (property != null && property.value != null && property.value instanceof Snap.Obj) {
                result.addAll(getAllFunctionInstances((Snap.Obj) property.value, seen));
            }
        }
        if (obj.env != null && obj.env instanceof Snap.Obj) {
            Snap.Obj env = (Snap.Obj) obj.env;
            result.addAll(getAllFunctionInstances(env, seen));
        }

        return result;
    }

}
