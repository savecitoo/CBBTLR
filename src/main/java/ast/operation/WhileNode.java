package ast.operation;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This record represents the While statement in the AST.
 * @param condition the condition of the while.
 * @param instruction the instructions of the while, it can be empty.
 * @param line the line where the while keyword appears.
 * @param column the column where the while keyword appears.
 */
public record WhileNode(@NotNull AstNode condition, @NotNull List<@NotNull AstNode> instruction,
                        int line, int column) implements AstNode {

    public WhileNode {
        instruction = List.copyOf(instruction);
    }

    @Override
    public @NotNull List<AstNode> children() {
        List<AstNode> children = new ArrayList<>();
        children.add(condition);
        children.addAll(instruction);
        return children;
    }

    public @NotNull String toString() {
        return "While";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitWhile(this);
    }

}
