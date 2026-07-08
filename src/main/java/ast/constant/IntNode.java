package ast.constant;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This record represents an integer node, it contains the numerical constant representing.<p>
 * @param num the integer represented by the node
 * @param line the line where appear the integer identifier.
 * @param column the column where appear the integer identifier.
 */
public record IntNode(long num, int line, int column) implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
        return List.of();
    }

    @Override
    public @NotNull String toString() {
        return String.valueOf(num);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitInt(this);
    }

}
