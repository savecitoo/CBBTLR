package ast.constant;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This class represents an identifier node.<p>
 * It may represent an identifier for a funcion name, a variable name, a dimension name or a unit name.
 * @param id the identifier represented by {@code this} node.
 * @param line the line where appear the identifier.
 * @param column the column where appear the identifier.
 */
public record IdentifierNode(@NotNull String id, int line, int column) implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
        return List.of();
    }

    @Override
    public @NotNull String toString() {
        return id;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIdentifier(this);
    }

}

