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

import java.util.HashSet;
import java.util.Set;

/**
 * This class implements a visitor of the AST which checks that all type expressions are correct
 */
public class TypeDeclarationVisitor implements AstVisitor<Boolean> {

    private final @NotNull Set<@NotNull String> units = new HashSet<>();

    @Override
    public @NotNull Boolean visitDefault(@NotNull AstNode node) {
        throw new IllegalArgumentException(
                String.format(
                        "Unexpected operation at line %d and column %d, the type expressions must contains just unit identifiers, dimension or the following operators: *,/ and ^",
                        node.line(), node.column()
                )
        );
    }

    @Override
    public @NotNull Boolean visitProgram(@NotNull ProgramNode node) {
        units.addAll(node.dimension());
        units.addAll(node.unit().keySet());
        for (FunctionNode f : node.functions().values())
            f.accept(this);
        return true;
    }

    @Override
    public @NotNull Boolean visitFunction(@NotNull FunctionNode node) {
        for (AstNode child : node.parameter())
            child.accept(this);
        node.ret_type().accept(this);
        return true;
    }

    @Override
    public @NotNull Boolean visitParameter(@NotNull ParameterNode node) {
        return node.type().accept(this);
    }

    @Override
    public @NotNull Boolean visitMul(@NotNull MulNode node) {
        return node.left().accept(this) && node.right().accept(this);
    }

    @Override
    public @NotNull Boolean visitDiv(@NotNull DivNode node) {
        return node.left().accept(this) && node.right().accept(this);
    }

    @Override
    public @NotNull Boolean visitPow(@NotNull PowNode node) {
        return node.base().accept(this);
    }

    @Override
    public @NotNull Boolean visitIdentifier(@NotNull IdentifierNode node) {
        if (!units.contains(node.id()))
            throw new IllegalArgumentException(
                    String.format(
                            "At the line %d and column %d in the type expression is used an undeclared unit: %s",
                            node.line(), node.column(), node.id()
                    )
            );
        return true;
    }

    @Override
    public @NotNull Boolean visitNum(@NotNull NumNode node) { return true; }

}
