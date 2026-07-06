package ast.program;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.IdentifierNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This record represents a function in the AST.
 * @param name the identifier of the function.
 * @param parameter the list of parameters in the function, it can be empty.
 * @param ret_type the expression defining the return type of the function
 * @param body the body of the function.
 * @param line the line where the function is defined.
 * @param column the column where the function is defined.
 */
public record FunctionNode(@NotNull IdentifierNode name,
                           @NotNull List<@NotNull ParameterNode> parameter,
                           @NotNull AstNode ret_type,
                           @NotNull AstNode body,
                           int line, int column) implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
        List<AstNode> par = new ArrayList<>(parameter);
        par.add(ret_type);
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
