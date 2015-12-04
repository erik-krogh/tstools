package dk.webbies.tscreate.evaluation;

import dk.au.cs.casa.typescript.SpecReader;
import dk.au.cs.casa.typescript.types.*;
import dk.webbies.tscreate.declarationReader.DeclarationParser;
import dk.webbies.tscreate.util.Pair;
import dk.webbies.tscreate.util.Util;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 05-10-2015.
 */
public class DeclarationEvaluator {
    private SpecReader realDeclaration;
    private SpecReader myDeclaration;
    private SpecReader emptyDeclaration;

    public DeclarationEvaluator(String resultFilePath, String comparisonFilePath, DeclarationParser.Environment env) {
        String relativePath = "";
        try {
            Util.runAll(() -> {
                realDeclaration = DeclarationParser.getTypeSpecification(env, relativePath + comparisonFilePath);
            }, () -> {
                myDeclaration = DeclarationParser.getTypeSpecification(env, relativePath + resultFilePath);
            }, () -> {
                emptyDeclaration = DeclarationParser.getTypeSpecification(env);
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }


    public Evaluation createEvaluation() {
        Evaluation result = new Evaluation();
        realDeclaration.getGlobal().accept(new EvaluationVisitor(result), myDeclaration.getGlobal());

        Evaluation baselineEvaluation = new Evaluation();
        realDeclaration.getGlobal().accept(new EvaluationVisitor(baselineEvaluation), emptyDeclaration.getGlobal());

        result.matchedProperties -= baselineEvaluation.matchedProperties;

        return result;
    }

    public static class Evaluation {
        public int matchedProperties = 0;
        public int missedProperties = 0;
        private int simpleTypeMismatches = 0;
        private int signatureMismatches = 0;
        private int parameterCountMismatches = 0;
        private int missingSignatures = 0;
        public final Map<Pair<Class<? extends Type>, Class<? extends Type>>, Integer> typeMisMatch = new HashMap<>();

        private void addTypeMismatch(Type shouldBe, Type was) {
            Pair<Class<? extends Type>, Class<? extends Type>> pair = new Pair<>(shouldBe.getClass(), was.getClass());
            if (typeMisMatch.containsKey(pair)) {
                typeMisMatch.put(pair, typeMisMatch.get(pair) + 1);
            } else {
                typeMisMatch.put(pair, 1);
            }

        }

        public void addSimpleTypeMismatch(SimpleType t, SimpleType type) {
            this.simpleTypeMismatches++;
        }

        public void addSignatureMismatch(List<Signature> realDeclaredCallSignatures, List<Signature> resultDeclaredCallSignatures) {
            this.signatureMismatches++;
        }

        public void addParameterCountMismatch() {
            this.parameterCountMismatches++;
        }

        public void addMissingSignature() {
            this.missingSignatures++;
        }

        @Override
        public String toString() {
            return "Evaluation{" +
                    "matchedProperties=" + matchedProperties +
                    ", missedProperties=" + missedProperties +
                    ", simpleTypeMismatches=" + simpleTypeMismatches +
                    ", signatureMismatches=" + signatureMismatches +
                    ", parameterCountMismatches=" + parameterCountMismatches +
                    ", missingSignatures=" + missingSignatures +
                    ", typeMisMatch=" + typeMisMatch +
                    '}';
        }
    }

    private static class EvaluationVisitor implements TypeVisitorWithArgument<Void,Type> {
        private Set<Type> seen = new HashSet<>();
        private Evaluation evaluation;

        public EvaluationVisitor(Evaluation evaluation) {
            this.evaluation = evaluation;
        }

        @Override
        public Void visit(AnonymousType t, Type type) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);

            throw new UnsupportedOperationException();
        }

        @Override
        public Void visit(ClassType t, Type type) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);

            throw new UnsupportedOperationException();
        }

        @Override
        public Void visit(GenericType t, Type type) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);

            // TODO: Ignoring generics for now, just making it info an interface.
            if (!(type instanceof GenericType)) {
                evaluation.addTypeMismatch(t, type);
                t.getDeclaredProperties().values().forEach(propertyType -> propertyType.accept(this, null));
                t.getDeclaredCallSignatures().forEach(signature -> compareSignatures(signature, null));
                t.getDeclaredConstructSignatures().forEach(signature -> compareSignatures(signature, null));
            } else {
                compareInterfaces(t.toInterface(), ((GenericType) type).toInterface());
            }
            return null;
        }

        @Override
        public Void visit(InterfaceType t, Type type) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);

            if (!(type instanceof InterfaceType)) {
                evaluation.addTypeMismatch(t, type);
            } else {
                compareInterfaces(t, (InterfaceType) type);
            }
            return null;
        }

        private void compareInterfaces(InterfaceType real, InterfaceType result) {
            real.getDeclaredProperties().forEach((name, type) -> {
                if (result.getDeclaredProperties().containsKey(name)) {
                    evaluation.matchedProperties++;
                    type.accept(this, result.getDeclaredProperties().get(name));
                } else {
                    evaluation.missedProperties++;
                }
            });

            for (int i = 0; i < real.getDeclaredCallSignatures().size(); i++) {
                Signature realSignature = real.getDeclaredCallSignatures().get(i);
                Signature resultSignature = result.getDeclaredCallSignatures().size() > i ? result.getDeclaredCallSignatures().get(i) : null;
                compareSignatures(realSignature, resultSignature);
            }

            for (int i = 0; i < real.getDeclaredConstructSignatures().size(); i++) {
                Signature realSignature = real.getDeclaredConstructSignatures().get(i);
                Signature resultSignature = result.getDeclaredConstructSignatures().size() > i ? result.getDeclaredConstructSignatures().get(i) : null;
                compareSignatures(realSignature, resultSignature);
            }

        }

        private void compareSignatures(Signature realSignature, Signature resultSignature) {
            if (resultSignature != null) {
                realSignature.getResolvedReturnType().accept(this, resultSignature.getResolvedReturnType());

                if (realSignature.getParameters().size() != resultSignature.getParameters().size()) {
                    evaluation.addParameterCountMismatch();
                } else {
                    Util.zip(realSignature.getParameters().stream(), resultSignature.getParameters().stream()).forEach(pair -> {
                        pair.first.getType().accept(this, pair.second.getType());
                    });
                }
            } else {
                evaluation.addMissingSignature();
            }
        }

        @Override
        public Void visit(ReferenceType t, Type type) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);

            if (!(type instanceof ReferenceType)) {
                evaluation.addTypeMismatch(t, type);
            } else {
                t.getTarget().accept(this, ((ReferenceType) type).getTarget());
                // TODO: Ignoring type-arguments for now.
            }

            return null;
        }

        @Override
        public Void visit(SimpleType t, Type type) {
            if (!(type instanceof SimpleType)) {
                evaluation.addTypeMismatch(t, type);
            } else {
                if (t.getKind() != ((SimpleType)type).getKind()) {
                    evaluation.addSimpleTypeMismatch(t, (SimpleType)type);
                }
            }
            return null;
        }

        @Override
        public Void visit(TupleType t, Type type) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);

            throw new UnsupportedOperationException();
        }

        @Override
        public Void visit(UnionType t, Type type) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);

            throw new UnsupportedOperationException();
        }

        @Override
        public Void visit(UnresolvedType t, Type type) {
            throw new RuntimeException();
        }

        @Override
        public Void visit(TypeParameterType t, Type type) {
            return null; // Completely ignored for now.
        }

        @Override
        public Void visit(SymbolType t, Type type) {
            if (seen.contains(t)) {
                return null;
            }
            seen.add(t);

            throw new UnsupportedOperationException();
        }
    }
}
