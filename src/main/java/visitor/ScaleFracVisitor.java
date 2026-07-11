package visitor;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.IdentifierNode;
import ast.constant.NumNode;
import ast.expression.DivNode;
import ast.expression.MulNode;
import ast.expression.PowNode;
import ast.program.FunctionNode;
import ast.program.ParameterNode;
import ast.program.ProgramNode;
import org.jetbrains.annotations.NotNull;
import utils.Fraction;

import java.util.*;

public class ScaleFracVisitor implements AstVisitor<Fraction> {

    private final @NotNull Map<@NotNull String, @NotNull List<@NotNull Fraction>> scale_par_frac = new HashMap<>();

    private final @NotNull Map<@NotNull String,@NotNull Fraction> scal_ret_frac = new HashMap<>();

    private final Map<String, Fraction> unit_frac;

    public ScaleFracVisitor(Map<String, Fraction> unit_frac) {
        this.unit_frac = Map.copyOf(unit_frac);
    }

    public @NotNull Map<@NotNull String, @NotNull List<@NotNull Fraction>> getParameterScaleFrac() {
        return Map.copyOf(scale_par_frac);
    }

    public @NotNull Map<@NotNull String,@NotNull Fraction> getReturnScaleFrac() {
        return Map.copyOf(scal_ret_frac);
    }

    @Override
    public @NotNull Fraction visitDefault(@NotNull AstNode node) {
        throw new IllegalArgumentException(String.format("Unexpected xpression in type expression at line %d and column %d", node.line(), node.column()));
    }

    public @NotNull Fraction visitProgram(@NotNull ProgramNode node) {
        for (FunctionNode f : node.functions().values()) {
            List<Fraction> parameter = new ArrayList<>();
            for (ParameterNode par : f.parameter()) {
                parameter.add(par.accept(this));
            }
            scale_par_frac.put(f.name().id(), parameter);
            scal_ret_frac.put(f.name().id(), f.ret_type().accept(this));
        }

        return Fraction.ONE;
    }

    public @NotNull Fraction visitParameter(@NotNull ParameterNode node) {
        return node.type().accept(this);
    }

    public @NotNull Fraction visitMul(@NotNull MulNode node) {
        return node.left().accept(this).mul(node.right().accept(this));
    }

    public @NotNull Fraction visitDiv(@NotNull DivNode node) {
        return node.left().accept(this).div(node.right().accept(this));
    }

    public @NotNull Fraction visitPow(@NotNull PowNode node) {
        return node.base().accept(this).pow(Math.toIntExact(node.exp().num()));
    }

    public @NotNull Fraction visitIdentifier(@NotNull IdentifierNode node) {
        if (!unit_frac.containsKey(node.id()))
            throw new IllegalArgumentException(String.format("Unknown unit identifier %s at line %d and column %d", node.id(),node.line(),node.column()));
        return unit_frac.get(node.id());
    }

    public @NotNull Fraction visitNum(@NotNull NumNode node) { return  Fraction.ONE;}

}
