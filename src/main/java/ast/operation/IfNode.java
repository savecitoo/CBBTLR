package ast.operation;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record IfNode(@NotNull AstNode condition, @NotNull List<@NotNull AstNode> trueBranch,
                     @NotNull List<@NotNull AstNode> falseBranch, int line, int column) implements AstNode {

    @Override
    public List<AstNode> children() {
        List<AstNode> children = new ArrayList<>();
        children.add(condition);
        children.addAll(trueBranch);
        children.addAll(falseBranch);
        return children;
    }

    @Override
    public @NotNull String toString() {
        return "If";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIf(this);
    }

}
