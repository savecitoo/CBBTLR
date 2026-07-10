package ast.visitor;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.IdentifierNode;
import ast.constant.IntNode;
import ast.program.ProgramNode;
import ast.expression.*; // AddNode, SubNode, MulNode, DivNode, PowNode, IdentifierNode, IntNode
import org.jetbrains.annotations.NotNull;
import utils.Fraction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UnitFractionVisitor implements AstVisitor<Fraction> {

    private final @NotNull Map<@NotNull String, @NotNull Fraction> unit_fract = new HashMap<>();

    private Set<String> dimension;

    private Map<String, AstNode> unit;

    public @NotNull Map<@NotNull String, @NotNull Fraction> getUnitFract() { return Map.copyOf(unit_fract); }

    @Override
    public @NotNull Fraction visitDefault(@NotNull AstNode node) {
        return Fraction.ZERO;
    }

    @Override
    public @NotNull Fraction visitProgram(@NotNull ProgramNode node) {
        dimension = node.dimension();
        unit = node.unit();
        for (String name : dimension) unit_fract.put(name, Fraction.ONE);
        for (String name : unit.keySet()) {
            // ogni unità la calcoliamo (e mettiamo in cache) al più una volta,
            // anche se referenziata più volte da altre unità
            visitUnit(name);
        }
        return Fraction.ONE;
    }

    // helper: calcola (con memoizzazione) N(u) per l'unità di nome `name`
    private @NotNull Fraction visitUnit(String name) {
        Fraction cached = unit_fract.get(name);
        if (cached != null) return cached;
        Fraction value = unit.get(name).accept(this);
        unit_fract.put(name, value);
        return value;
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
        int exponent = Math.toIntExact(node.exp().num());
        return node.base().accept(this).pow(exponent);
    }

    @Override
    public @NotNull Fraction visitIdentifier(@NotNull IdentifierNode node) {
        String name = node.id();
        if (dimension.contains(name)) {
            // unità di base: N(u) = 1
            return Fraction.ONE;
        }
        // altrimenti è un'altra unità derivata, già definita in `unit`
        return visitUnit(name);
    }

    @Override
    public @NotNull Fraction visitInt(@NotNull IntNode node) {
        return Fraction.fromNumDen(node.num(), 1);
    }
}