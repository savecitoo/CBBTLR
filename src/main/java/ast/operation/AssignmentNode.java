package ast.operation;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.IdentifierNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record AssignmentNode(@NotNull IdentifierNode id, @NotNull AstNode expression, int line, int column) implements AstNode {

    @Override
    public List<AstNode> children() {
        return List.of(id, expression);
    }

    @Override
    public @NotNull String toString() {
        return "=";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitAssignment(this);
    }

}
