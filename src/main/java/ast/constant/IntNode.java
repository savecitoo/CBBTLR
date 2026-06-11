package ast.constant;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record IntNode(@NotNull int num, int line, int column) implements AstNode {

    @Override
    public List<AstNode> children() {
        return List.of();
    }

    @Override
    public @NotNull String toString() {
        return String.valueOf(num);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitInt(this);
    }

}
