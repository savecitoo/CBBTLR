package ast.visitor;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;
import utils.Fraction;

import java.util.Map;

public class UnitFractionVisitor implements AstVisitor<Fraction> {

    private final @NotNull Map<@NotNull String, @NotNull Fraction> unit_fract;

    public UnitFractionVisitor(@NotNull Map<@NotNull String, @NotNull Fraction> unitFract) {
        unit_fract = unitFract;
    }

    @Override
    public Fraction visitDefault(AstNode node) {
        return Fraction.ZERO;
    }



}
