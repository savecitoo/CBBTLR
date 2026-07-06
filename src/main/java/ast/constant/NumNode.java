package ast.constant;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A record representing the type Num node.
 * @param line the line where appear the Num identifier.
 * @param column the column where appear the Num identifier.
 */
public record NumNode(int line, int column) implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
        return List.of();
    }

    public @NotNull String toString() {
        return "Num";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitNum(this);
    }

}
