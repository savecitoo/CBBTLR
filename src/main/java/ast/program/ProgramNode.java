package ast.program;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This record represents the program node, it must be the root of an AST of a program.
 * @param dimension the set of the dimensions declared.
 * @param unit a map of the unit declared, as a key the identifier, as a value the expression defining it.
 * @param functions the functions of the program node.
 * @param line the line where the program starts.
 * @param column the column where the program starts.
 */
public record ProgramNode(@NotNull Set<String> dimension,
                          @NotNull Map<@NotNull String, @NotNull AstNode> unit,
                          @NotNull Map<@NotNull String, @NotNull FunctionNode> functions,
                          int line, int column) implements AstNode {

    @Override
    public @NotNull List<AstNode> children() {
        ArrayList<AstNode> f = new ArrayList<>();
        for (String s : functions.keySet()) f.add(functions.get(s));
        return f;
    }

    @Override
    public @NotNull String toString() {
        return "Dimension: " + dimension.toString() + "\tUnit: " + unit.toString();
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitProgram(this);
    }

}
