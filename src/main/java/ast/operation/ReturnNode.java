package ast.operation;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This record represents the return statement in the AST.
 * @param ret_stat the return type.
 * @param line the line where the return keyword appears.
 * @param column the column where the return keyword appears.
 */
public record ReturnNode(@NotNull AstNode ret_stat, int line, int column) implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
        return List.of(ret_stat);
    }

    public @NotNull String toString() {
        return "RETURN";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitReturn(this);
    }

}

