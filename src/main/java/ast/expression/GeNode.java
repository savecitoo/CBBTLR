package ast.expression;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This node represents the arithmetic greater-equal operation.
 * @param left the left sub-type of the operation.
 * @param right the right sub-type of the operation.
 * @param line the line where appears the operation, it points to the start of the left operation.
 * @param column the column where appears the operation, it points to the start of the left operation.
 */
public record GeNode(@NotNull AstNode left, @NotNull AstNode right, int line, int column) implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
        return List.of(left, right);
    }

    @Override
    public @NotNull String toString() {
        return ">=";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitGe(this);
    }

}
