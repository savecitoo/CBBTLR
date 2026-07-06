package ast.expression;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This node represents the arithmetic subtraction operation.
 * @param left the left sub-type of the subtraction.
 * @param right the right sub-type of the subtraction.
 * @param line the line where appears the operation, it points to the start of the left operation.
 * @param column the column where appears the operation, it points to the start of the left operation.
 */
public record SubNode(@NotNull AstNode left, @NotNull AstNode right, int line, int column) implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
        return List.of(left, right);
    }

    public @NotNull String toString() {
        return "-";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitSub(this);
    }

}
