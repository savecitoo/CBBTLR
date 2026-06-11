package ast.operation;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ReturnNode(AstNode ret_stat, int line, int column) implements AstNode {

    @Override
    public List<AstNode> children() {
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

