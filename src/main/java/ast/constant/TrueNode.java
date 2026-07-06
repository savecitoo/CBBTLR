package ast.constant;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This record represents the logic constant True in the AST.
 * @param line the line where appear the true identifier.
 * @param column the column where appear the true identifier.
 */
public record TrueNode(int line, int column) implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
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

