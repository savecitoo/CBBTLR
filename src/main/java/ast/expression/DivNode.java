package ast.expression;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record DivNode(AstNode left, AstNode right, int line, int column) implements AstNode {

    @Override
    public List<AstNode> children() {
        return List.of(left, right);
    }

    @Override
    public @NotNull String toString() {
        return "/";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitDiv(this);
    }

}
