package ast.expression;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record AndNode(AstNode left, AstNode right, int line, int column) implements AstNode {

    @Override
    public List<AstNode> children() {
        return List.of(left, right);
    }

    @Override
    public @NotNull String toString() {
        return "and";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitAnd(this);
    }

}
