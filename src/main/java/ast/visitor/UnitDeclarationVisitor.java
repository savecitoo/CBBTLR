package ast.visitor;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.IdentifierNode;
import ast.constant.IntNode;
import ast.constant.NumNode;
import ast.expression.DivNode;
import ast.expression.MulNode;
import ast.expression.PowNode;
import ast.program.ProgramNode;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Checks the unit declarations of a program:
 * each unit expression may contain only unit identifiers, Num, integer
 * literals and the operators *, /, ^; every identifier must refer to a base
 * unit (declared by a dimension) or to another declared unit, in any order of
 * declaration; unit definitions must not contain cycles, direct or indirect.
 */
public class UnitDeclarationVisitor implements AstVisitor<Boolean> {

    private final @NotNull Set<String> dimensions = new HashSet<>();

    private final @NotNull Map<String, AstNode> units = new HashMap<>();

    private final @NotNull Set<String> done = new HashSet<>();

    private final @NotNull Set<String> in_progress = new HashSet<>();

    @Override
    public @NotNull Boolean visitDefault(@NotNull AstNode node) {
        throw new IllegalArgumentException(String.format(
                "At line %d and column %d: this is not allowed in a unit expression. " +
                        "Unit expressions may only contain unit identifiers, Num, integer literals " +
                        "and the operators *, / and ^.",
                node.line(), node.column()));
    }

    @Override
    public @NotNull Boolean visitProgram(@NotNull ProgramNode node) {
        dimensions.addAll(node.dimension());
        units.putAll(node.unit());
        for (String name : units.keySet())
            checkUnit(name);
        return true;
    }

    /**
     * Verifies the definition of the given unit, recursively verifying the
     * units it refers to. A unit found again while it is still being verified
     * (i.e. still in {@code in_progress}) closes a cycle.
     */
    private void checkUnit(@NotNull String name) {
        if (done.contains(name))
            return;
        if (!in_progress.add(name))
            throw new IllegalArgumentException(String.format(
                    "The definition of unit '%s' is cyclic.",
                    name));
        units.get(name).accept(this);
        in_progress.remove(name);
        done.add(name);
    }

    @Override
    public @NotNull Boolean visitIdentifier(@NotNull IdentifierNode node) {
        String id = node.id();
        if (dimensions.contains(id))
            return true;                       // base unit: no further deps
        if (!units.containsKey(id))
            throw new IllegalArgumentException(String.format(
                    "At line %d and column %d: the unit expression uses the undeclared unit '%s'.",
                    node.line(), node.column(), id));
        checkUnit(id);                         // derived unit: verify it (cycle detection)
        return true;
    }

    @Override
    public @NotNull Boolean visitMul(@NotNull MulNode node) {
        node.left().accept(this);
        node.right().accept(this);
        return true;
    }

    @Override
    public @NotNull Boolean visitDiv(@NotNull DivNode node) {
        node.left().accept(this);
        node.right().accept(this);
        return true;
    }

    @Override
    public @NotNull Boolean visitPow(@NotNull PowNode node) {
        return node.base().accept(this);       // the exponent is an IntNode by construction
    }

    /** Integer literals are allowed in unit expressions (class U). */
    @Override
    public @NotNull Boolean visitInt(@NotNull IntNode node) {
        return true;
    }

    @Override
    public @NotNull Boolean visitNum(@NotNull NumNode node) {
        return true;
    }
}