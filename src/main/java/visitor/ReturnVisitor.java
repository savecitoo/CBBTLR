package visitor;

import ast.AstNode;
import ast.AstVisitor;
import ast.operation.AssignmentNode;
import ast.operation.IfNode;
import ast.operation.ReturnNode;
import ast.program.BodyNode;
import ast.program.FunctionNode;
import ast.program.ProgramNode;
import ast.operation.WhileNode;
import org.jetbrains.annotations.NotNull;

/**
 * This visitor checks in the AST if the Define Return condition is verified.
 */
public class ReturnVisitor implements AstVisitor<Boolean> {

    @Override
    public @NotNull Boolean visitDefault(@NotNull AstNode node) {
        for (AstNode child : node.children()) {
            if (child.accept(this))
                return true;
        }
        return false;
    }

    @Override
    public @NotNull Boolean visitProgram(@NotNull ProgramNode node) {
        for (String name : node.functions().keySet()) {
            AstNode child = node.functions().get(name);
            if (!child.accept(this))
                throw new IllegalArgumentException(
                        String.format(
                                "The function %s at line %d and column %d has does not respect the define return clause.",
                                name, child.line(), child.column()
                        )
                );
        }
        return true;
    }

    @Override
    public @NotNull Boolean visitFunction(@NotNull FunctionNode node) {
        return node.body().accept(this);
    }

    @Override
    public @NotNull Boolean visitBody(@NotNull BodyNode node) {
        for (AstNode child : node.stat()) {
            if (child.accept(this))
                return true;
        }
        return false;
    }

    @Override
    // While C { S } -> False
    public @NotNull Boolean visitWhile(@NotNull WhileNode node) {
        return false;
    }

    @Override
    public @NotNull Boolean visitIf(@NotNull IfNode node) {

        // IF C { S } (no Else) -> False
        // here we are avoiding programs with else defined but its statement are void.
        if (node.falseBranch().isEmpty())
            return false;
        boolean ok = false;

        // IF C { S_1 } ELSE { S_2} -> S_1 && S_2
        for (AstNode child : node.trueBranch()) {
            if (child.accept(this)) {
                ok = true;
                break;
            }
        }
        if (!ok) {
            return false;
        }
        for (AstNode child : node.falseBranch()) {
            if (child.accept(this)) {
                return true;
            }
        }
        return false;
    }

    @Override
    // x = E -> False
    public @NotNull Boolean visitAssignment(@NotNull AssignmentNode node) {
        return false;
    }

    @Override
    // Return -> True
    public @NotNull Boolean visitReturn(@NotNull ReturnNode node) {
        return true;
    }

}
