package ast.program;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record BodyNode(@NotNull List<@NotNull AstNode> decl, @NotNull List<@NotNull AstNode> stat,
                       int line, int column) implements AstNode {

    @Override
    public List<AstNode> children() {
        List<AstNode> children = new ArrayList<>(decl);
        children.addAll(stat);
        return children;
    }

    @Override
    public @NotNull String toString() {
        return "Body";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitBody(this);
    }

}
