package ast.expression;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This node represents the logical or operation.
 * @param left the left sub-condition of the or.
 * @param right the right sub-condition of the or.
 * @param line the line where appears the operation, it points to the start of the left operation.
 * @param column the column where appears the operation, it points to the start of the left operation.
 */
public record OrNode(@NotNull AstNode left, @NotNull AstNode right, int line, int column) implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
        return List.of(left, right);
    }

    public @NotNull String toString() {
        return "OR";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitOr(this);
    }

}

