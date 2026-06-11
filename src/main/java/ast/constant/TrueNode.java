package ast.constant;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record TrueNode(int line, int column) implements AstNode {

    @Override
    public List<AstNode> children() {
        return List.of();
    }

    public @NotNull String toString() {
        return "True";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitTrue(this);
    }

}

