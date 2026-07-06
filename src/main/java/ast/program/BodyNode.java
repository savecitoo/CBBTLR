package ast.program;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This record represents the body of a functions.<p>
 * The intepretation order of the declarations and the statements will be the same as they are passed.
 * @param decl the declarations of the variables, it can be empty.
 * @param stat the statements of the functions, it can be empty.
 * @param line the line where the body of the function begins.
 * @param column the column where the body of the function begins.
 */
public record BodyNode(@NotNull List<@NotNull AstNode> decl, @NotNull List<@NotNull AstNode> stat,
                       int line, int column) implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
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
