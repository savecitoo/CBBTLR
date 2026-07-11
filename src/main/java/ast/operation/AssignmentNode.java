package ast.operation;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.IdentifierNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Thie record represents an expression node in the AST.
 * @param var the variable assigned.
 * @param expression the type that give to the variable its value.
 * @param line the line of the expression.
 * @param column the column of the expression.
 */
public record AssignmentNode(@NotNull IdentifierNode var, @NotNull AstNode expression, int line, int column) implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
        return List.of(var, expression);
    }

    @Override
    public @NotNull String toString() {
        return "=";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitAssignment(this);
    }

}
