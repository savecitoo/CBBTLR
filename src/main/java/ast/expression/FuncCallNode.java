
package ast.expression;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.IdentifierNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record FuncCallNode(@NotNull IdentifierNode func, List<AstNode> args, int line, int column) implements AstNode {

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
