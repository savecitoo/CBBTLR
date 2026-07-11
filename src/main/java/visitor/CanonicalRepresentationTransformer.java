package visitor;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.IdentifierNode;
import ast.constant.NumNode;
import ast.expression.DivNode;
import ast.expression.MulNode;
import ast.expression.PowNode;
import org.jetbrains.annotations.NotNull;
import utils.Fraction;

import java.util.Map;

/**
 * This class transforms the input parameters into their canonical representation.
 */
public class CanonicalRepresentationTransformer implements AstVisitor<Fraction> {

    private final @NotNull Map<@NotNull String, @NotNull Fraction> unit_frac;

    private final Fraction f;

    private final @NotNull AstNode type;

    public CanonicalRepresentationTransformer(@NotNull Map<@NotNull String, @NotNull Fraction> unit_frac,
                                              @NotNull Fraction f, @NotNull AstNode type) {
        this.unit_frac = Map.copyOf(unit_frac);
        this.f = f;
        this.type = type;
    }

    public @NotNull Fraction getCanonical() {
        return f.mul(type.accept(this));
    }

    @Override
    public @NotNull Fraction visitMul(@NotNull MulNode node) {
        return node.left().accept(this).mul(node.right().accept(this));
    }

    @Override
    public @NotNull Fraction visitDiv(@NotNull DivNode node) {
        return node.left().accept(this).div(node.right().accept(this));
    }

    @Override
    public @NotNull Fraction visitPow(@NotNull PowNode node) {
        return node.base().accept(this).pow(Math.toIntExact(node.exp().num()));
    }

    @Override
    public @NotNull Fraction visitIdentifier(@NotNull IdentifierNode node) {
        if (!unit_frac.containsKey(node.id()))
            throw new IllegalArgumentException(
                    String.format("Unknown unit identifier %s at line %d and column %d", node.id(), node.line(), node.column())
            );
        return unit_frac.get(node.id());
    }

    @Override
    public @NotNull Fraction visitNum(@NotNull NumNode node) {
        return Fraction.ONE;
    }

    @Override
    public @NotNull Fraction visitDefault(@NotNull AstNode node) {
        throw new  IllegalStateException("Unexpected node found, in the trasformation the only operation accepted are: *, /, ^.");
    }
}
