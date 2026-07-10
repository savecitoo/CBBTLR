package ast.visitor;

import ast.AstNode;
import ast.AstVisitor;
import ast.expression.*;
import ast.operation.*;
import org.jetbrains.annotations.NotNull;
import utils.Fraction;

public class ConditionEvaluator implements AstVisitor<Boolean> {

    private final @NotNull ValueExpressionEvaluator exprEvaluator;

    public ConditionEvaluator(@NotNull ValueExpressionEvaluator exprEvaluator) {
        this.exprEvaluator = exprEvaluator;
    }

    @Override
    public @NotNull Boolean visitDefault(AstNode node) {
        throw new IllegalStateException("Nodo non gestito da ConditionEvaluator: " + node.getClass());
    }

    @Override
    public @NotNull Boolean visitAnd(AndNode node) {
        return node.left().accept(this) && node.right().accept(this); // && è già short-circuit
    }

    @Override
    public @NotNull Boolean visitOr(OrNode node) {
        return node.left().accept(this) || node.right().accept(this);
    }

    @Override
    public @NotNull Boolean visitNot(NotNode node) {
        return !node.condition().accept(this);
    }

    @Override
    public @NotNull Boolean visitGt(GtNode node) {
        return compare(node.left(), node.right()) > 0;
    }

    @Override
    public @NotNull Boolean visitLt(LtNode node) {
        return compare(node.left(), node.right()) < 0;
    }

    @Override
    public @NotNull Boolean visitGe(GeNode node) {
        return compare(node.left(), node.right()) >= 0;
    }

    @Override
    public @NotNull Boolean visitLe(LeNode node) {
        return compare(node.left(), node.right()) <= 0;
    }

    private int compare(@NotNull AstNode left, @NotNull AstNode right) {
        Fraction l = left.accept(exprEvaluator);
        Fraction r = right.accept(exprEvaluator);
        return l.sub(r).signum();
    }

    @Override
    public @NotNull Boolean visitTrue(ast.constant.TrueNode node) {
        return true;
    }

    @Override
    public @NotNull Boolean visitFalse(ast.constant.FalseNode node) {
        return false;
    }
}