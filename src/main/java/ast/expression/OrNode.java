package ast.expression;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record OrNode(AstNode left, AstNode right, int line, int column) implements AstNode {

    @Override
    public List<AstNode> children() {
        return List.of(left, right);
    }

    public @NotNull String toString() {
        return "OR";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitOr(this);
    }

}

