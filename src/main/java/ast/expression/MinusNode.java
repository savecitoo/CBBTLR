package ast.expression;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record MinusNode(AstNode child, int line, int column) implements AstNode {

    @Override
    public List<AstNode> children() {
        return List.of(child);
    }

    public @NotNull String toString() {
        return "-";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitMinus(this);
    }

}
