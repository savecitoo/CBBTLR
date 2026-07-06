package ast.operation;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.IdentifierNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This record represents a declaration node in the AST.
 * @param var the variable declared.
 * @param assignment the type that gives value the declared variable.
 * @param line the line of the declaration.
 * @param column the column where the declaration starts.
 */
public record DeclarationNode(@NotNull IdentifierNode var, @NotNull AstNode assignment, int line, int column) implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
        return List.of(var, assignment);
    }

    @Override
    public @NotNull String toString() {
        return "=";
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitDeclaration(this);
    }

}
