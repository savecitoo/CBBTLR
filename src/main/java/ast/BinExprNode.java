package ast;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record BinExprNode(String op, AstNode left, AstNode right, int line, int col) implements AstNode {
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
        return List.of(left,right);
    }

    @Override
    public @NotNull String toString() {
        return op;
    }
}
