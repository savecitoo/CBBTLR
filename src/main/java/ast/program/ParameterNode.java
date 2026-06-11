package ast.program;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ParameterNode(@NotNull AstNode symbol, @NotNull AstNode expression, int line, int col) implements AstNode {

    @Override
    public int column() {
        return 0;
    }

    @Override
    public List<AstNode> children() {
        return List.of(symbol,expression);
    }

    @Override
    public @NotNull String toString() {
        return "Parameter";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitParameter(this);
    }

}
