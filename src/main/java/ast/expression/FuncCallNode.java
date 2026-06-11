
package ast.expression;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record FuncCallNode(@NotNull AstNode func, List<AstNode> args, int line, int column) implements AstNode {

    @Override
    public List<AstNode> children() {
        List<AstNode> children = new ArrayList<>();
        children.add(func);
        children.addAll(args);
        return children;
    }

    @Override
    public @NotNull String toString() {
        return "FuncCall";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFuncCall(this);
    }

}
