package ast.expression;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This record represents the logical not connector.
 * @param condition the condition to be negated.
 * @param line the line where the not operation starts.
 * @param column the column where the not operation starts.
 */
public record NotNode(@NotNull AstNode condition, int line, int column) implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
        return List.of(condition);
    }

    public @NotNull String toString() {
        return "NOT";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitNot(this);
    }

}
