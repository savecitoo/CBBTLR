package ast.program;

import ast.AstNode;
import ast.AstVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ProgramNode(@NotNull Set<String> dimension,
                          @NotNull Map<@NotNull String, @NotNull AstNode> unit,
                          @NotNull Map<@NotNull String, @NotNull AstNode> functions,
                          int line, int column) implements AstNode {

    @Override
    public List<AstNode> children() {
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
