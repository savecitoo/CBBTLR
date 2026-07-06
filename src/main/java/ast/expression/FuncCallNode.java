
package ast.expression;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.IdentifierNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This record represents a FunctionCall node.
 * @param func an identifier representing the function called.
 * @param args the list of the arguments of the function.
 * @param line the line where appears the call.
 * @param column the column where appears the call.
 */
public record FuncCallNode(@NotNull IdentifierNode func, @NotNull List< @NotNull AstNode> args, int line, int column) implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
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
