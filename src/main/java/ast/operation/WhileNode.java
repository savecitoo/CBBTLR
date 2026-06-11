package ast.operation;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record WhileNode(@NotNull AstNode condition, @NotNull List<@NotNull AstNode> instruction,
                        int line, int column) implements AstNode {

    @Override
    public List<AstNode> children() {
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
