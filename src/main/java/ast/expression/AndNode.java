package ast.expression;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This node represents the logical and operation.
 * @param left the left sub-condition of the and.
 * @param right the right sub-condition of the and.
 * @param line the line where appears the operation, it points to the start of the left operation.
 * @param column the column where appears the operation, it points to the start of the left operation.
 */
public record AndNode(@NotNull AstNode left, @NotNull AstNode right, int line, int column) implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
        return List.of(left, right);
    }

    @Override
    public @NotNull String toString() {
        return "and";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitAnd(this);
    }

}
