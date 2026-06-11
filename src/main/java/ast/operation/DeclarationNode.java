package ast.operation;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.IdentifierNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record DeclarationNode(@NotNull IdentifierNode var, @NotNull AstNode assignment, int line, int column) implements AstNode {

    @Override
    public List<AstNode> children() {
        return List.of(var, assignment);
    }

    @Override
    public @NotNull String toString() {
        return "=";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitDeclaration(this);
    }

}
