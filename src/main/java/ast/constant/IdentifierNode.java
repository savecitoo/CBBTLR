package ast.constant;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record IdentifierNode(@NotNull String id, int line, int column) implements AstNode {

    @Override
    public List<AstNode> children() {
        return List.of();
    }

    @Override
    public @NotNull String toString() {
        return id;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIdentifier(this);
    }

}

