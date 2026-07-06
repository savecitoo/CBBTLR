package ast.program;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.IdentifierNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This record represents a parameter of a function in the AST.
 * @param id the id of a variable in the function.
 * @param type the expression defining the type of the parameter.
 * @param line the line where the parameter appears.
 * @param column the column where the parameter appears.
 */
public record ParameterNode(@NotNull IdentifierNode id,
                            @NotNull AstNode type,
                            int line, int column) implements AstNode {


    @Override
    public @NotNull List<AstNode> children() {
        return List.of(id, type);
    }

    @Override
    public @NotNull String toString() {
        return "Parameter";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitParameter(this);
    }

}
