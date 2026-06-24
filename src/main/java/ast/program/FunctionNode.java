package ast.program;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.IdentifierNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record FunctionNode(@NotNull IdentifierNode name,
                           @NotNull List<ParameterNode> parameter,
                           @NotNull AstNode ret_expr,
                           @NotNull AstNode body,
                           int line, int column) implements AstNode {

    @Override
    public List<AstNode> children() {
        List<AstNode> par = new ArrayList<>(parameter);
        par.add(ret_expr);
        par.add(body);
        return par;
    }

    @Override
    public @NotNull String toString() {
        return "Function " + name.id();
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFunction(this);
    }

}
