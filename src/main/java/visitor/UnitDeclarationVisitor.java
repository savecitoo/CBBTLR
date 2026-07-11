package visitor;

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
 * This visitor checks if the unit declarations are correct.
 */
public class UnitDeclarationVisitor implements AstVisitor<Boolean> {

    /**
     * A set containing all the base dimensions.
     */
    private final @NotNull Set<@NotNull String> dimensions = new HashSet<>();

    /**
     * A map which keys are the unit ids and values are the expression characterizing it.
     */
    private final @NotNull Map<@NotNull String, @NotNull AstNode> units = new HashMap<>();

    // We use the two maps below to keep trace also if there are some cyclic unit, for example:
    /*
        unit a = b
        unit b = a
     */

    /**
     * A set of the done unit declaration.
     */
    private final @NotNull Set<String> done = new HashSet<>();

    /**
     * A set of the units that are still being visited.
     */
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
            return true;
        if (!units.containsKey(id))
            throw new IllegalArgumentException(String.format(
                    "At line %d and column %d: the unit expression uses the undeclared unit '%s'.",
                    node.line(), node.column(), id));
        checkUnit(id);
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
        return node.base().accept(this);
    }

    @Override
    public @NotNull Boolean visitInt(@NotNull IntNode node) {
        return true;
    }

    @Override
    public @NotNull Boolean visitNum(@NotNull NumNode node) {
        return true;
    }
}