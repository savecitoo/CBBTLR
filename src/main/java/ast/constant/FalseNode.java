package ast.constant;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This record represents the logic constant False in the AST.
 * @param line the line where appear the false identifier.
 * @param column the column where appear the false identifier.
 */
public record FalseNode(int line, int column)
        implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
        return List.of();
    }

    @Override
    public @NotNull String toString() {
        return "False";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFalse(this);
    }

}
