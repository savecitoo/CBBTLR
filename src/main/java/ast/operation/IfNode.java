package ast.operation;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This record represents an If node.
 * @param condition the condition of the if.
 * @param trueBranch the list of the instruction executed if the condition is true, it can be empty.
 * @param falseBranch the list of the instruction executed if the condition is false, it can be empty.
 * @param line the line where the if starts.
 * @param column the column where the if starts.
 */
public record IfNode(@NotNull AstNode condition, @NotNull List<@NotNull AstNode> trueBranch,
                     @NotNull List<@NotNull AstNode> falseBranch, int line, int column) implements AstNode {

    public IfNode {
        trueBranch = List.copyOf(trueBranch);
        falseBranch = List.copyOf(falseBranch);
    }

    @Override
    public @NotNull List<AstNode> children() {
        List<AstNode> children = new ArrayList<>();
        children.add(condition);
        children.addAll(trueBranch);
        children.addAll(falseBranch);
        return children;
    }

    @Override
    public @NotNull String toString() {
        return "If";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIf(this);
    }

}
