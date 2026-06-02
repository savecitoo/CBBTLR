package ast;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record UnarExprNode(@NotNull String op, AstNode child, int line, int col) implements AstNode {
    @Override
    public String type() {
        return "";
    }

    @Override
    public int column() {
        return col;
    }

    @Override
    public List<AstNode> children() {
        return List.of(child);
    }

    @Override
    public @NotNull String toString() {
        return op;
    }
}
