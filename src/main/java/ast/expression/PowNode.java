package ast.expression;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.IdentifierNode;
import ast.constant.IntNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This record represents the power operation.
 * @param base the base of the power, it must be either a unit identifier or a variable.
 * @param exp the exponent, it must be an integer node.
 * @param line the line where appears the operation, it points to the start of the left operation.
 * @param column the column where appears the operation, it points to the start of the left operation.
 */
public record PowNode(@NotNull IdentifierNode base, @NotNull IntNode exp, int line, int column) implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
        return List.of(base, exp);
    }

    public @NotNull String toString() {
        return "^";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitPow(this);
    }

}
