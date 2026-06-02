package ast;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record IntNode(@NotNull String num, int col, int line) implements AstNode {
    @Override
    public String type() {
        return "";
    }

    @Override
    public int column() {
        return 0;
    }

    @Override
    public List<AstNode> children() {
        return List.of();
    }

    @Override
    public @NotNull String toString() {
        return num;
    }
}
