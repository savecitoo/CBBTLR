package ast.expression;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record PowNode(@NotNull AstNode base, @NotNull AstNode exp, int line, int column) implements AstNode {

    @Override
    public List<AstNode> children() {
        return List.of(base, exp);
    }

    public @NotNull String toString() {
        return "^";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitPow(this);
    }

}
