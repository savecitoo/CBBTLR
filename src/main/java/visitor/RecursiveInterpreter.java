package visitor;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.FalseNode;
import ast.constant.IdentifierNode;
import ast.constant.IntNode;
import ast.constant.TrueNode;
import ast.expression.*;
import ast.operation.*;
import ast.program.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.Fraction;

import java.util.*;

/**
 * Interprete ricorsivo per CoDiNE.
 */
public class RecursiveInterpreter implements AstVisitor<Fraction> {

    private final @NotNull Map<@NotNull String, @NotNull FunctionNode> functions = new HashMap<>();

    private final @NotNull Map<@NotNull String, @NotNull Fraction> unit_fract;

    private final @NotNull List<@NotNull Fraction> args;

    private boolean ret = false;

    /**
     * Stack dei frame di attivazione: in cima c'è l'ambiente locale
     * (variabili e parametri) della funzione attualmente in esecuzione.
     */
    private final @NotNull Deque<Map<String, Fraction>> record_stack = new ArrayDeque<>();

    // TODO: RICORDA NELLA SPECIFICA DI DIRE CHE GLI ARGS VA IN FORMA CANONICA
    public RecursiveInterpreter(@NotNull Map<@NotNull String, @NotNull Fraction> unit_fract,
                                @NotNull List<@NotNull Fraction> args) {
        this.unit_fract = unit_fract;
        this.args = args;
    }

    private @NotNull Map<String, Fraction> env() {
        Map<String, Fraction> top = record_stack.peek();
        if (top == null)
            throw new IllegalStateException("Stack dei record di attivazione vuoto.");
        return top;
    }

    @Override
    public @NotNull Fraction visitProgram(@NotNull ProgramNode node) {
        functions.putAll(node.functions());
        Map<String, Fraction> frame = new HashMap<>();
        List<ParameterNode> par = functions.get("main").parameter();
        for (int i = 0; i < par.size(); i++) {
            frame.put(par.get(i).id().id(), args.get(i));
        }
        record_stack.push(frame);
        return functions.get("main").accept(this);
    }

    @Override
    public @NotNull Fraction visitFunction(@NotNull FunctionNode node) {
        return node.body().accept(this);
    }

    @Override
    public @NotNull Fraction visitBody(@NotNull BodyNode node) {
        for (AstNode decl : node.decl())
            decl.accept(this);
        for (AstNode stat : node.stat()) {
            Fraction f = stat.accept(this);
            if (ret) {
                ret = false;
                return f;
            }
        }
        // Non-reachable from the define return clause.
        throw new IllegalStateException(String.format(
                "Not reached the return statement in function at line %d and column %d",
                node.line(), node.column()));
    }

    @Override
    public @NotNull Fraction visitDeclaration(@NotNull DeclarationNode node) {
        Fraction f = node.assignment().accept(this);
        env().put(
                node.var().id(),
                f
        );
        return f;
    }

    @Override
    public @NotNull Fraction visitAssignment(@NotNull AssignmentNode node) {
        Fraction f = node.expression().accept(this);
        env().put(node.var().id(), f);
        return f;
    }

    @Override
    public @NotNull Fraction visitReturn(@NotNull ReturnNode node) {
        Fraction f = node.ret_stat().accept(this);
        ret = true;
        record_stack.pop();
        return f;
    }

    @Override
    public @NotNull Fraction visitIf(@NotNull IfNode node) {
        Fraction f;
        if (node.condition().accept(this).equals(Fraction.ONE)) {
            for (AstNode child : node.trueBranch()) {
                f = child.accept(this);
                if (ret) {
                    return f;
                }
            }
        } else {
            for (AstNode child : node.falseBranch()) {
                f = child.accept(this);
                if (ret) {
                    return f;
                }
            }
        }
        return Fraction.ONE;
    }

    @Override
    public @NotNull Fraction visitWhile(@NotNull WhileNode node) {
        while (node.condition().accept(this).equals(Fraction.ONE)) {
            for (AstNode child : node.instruction()) {
                Fraction f = child.accept(this);
                if (ret) {
                    return f;
                }

            }
        }
        return Fraction.ONE;
    }

    @Override
    public @NotNull Fraction visitAdd(@NotNull AddNode node) {
        return node.left().accept(this).add(node.right().accept(this));
    }

    @Override
    public @NotNull Fraction visitSub(@NotNull SubNode node) {
        return node.left().accept(this).sub(node.right().accept(this));
    }

    @Override
    public @NotNull Fraction visitMul(@NotNull MulNode node) {
        return node.left().accept(this).mul(node.right().accept(this));
    }

    @Override
    public @NotNull Fraction visitDiv(@NotNull DivNode node) {
        return node.left().accept(this).div(node.right().accept(this));
    }

    @Override
    public @NotNull Fraction visitPow(PowNode node) {
        return node.base().accept(this).pow(Math.toIntExact(node.exp().num()));
    }

    @Override
    public @NotNull Fraction visitInt(IntNode node) {
        return Fraction.fromNumDen(node.num(), 1);
    }

    @Override
    public @NotNull Fraction visitIdentifier(IdentifierNode node) {
        String name = node.id();
        Fraction value = env().get(name);
        if (value != null)
            return value;
        value = unit_fract.get(name);
        if (value != null)
            return value;
        throw new IllegalStateException("Identificatore non risolto a runtime: " + name);
    }

    @Override
    public @NotNull Fraction visitFuncCall(@NotNull FuncCallNode node) {
        Map<String, Fraction> scope = new HashMap<>();
        List<ParameterNode> par =  functions.get(node.func().id()).parameter();
        for (int i = 0; i < par.size(); i++) {
            scope.put(
                    par.get(i).id().id(),
                    node.args().get(i).accept(this)
            );
        }
        record_stack.push(scope);
        return functions.get(node.func().id()).accept(this);
    }

    @Override
    public @NotNull Fraction visitAnd(@NotNull AndNode node) {
        if (node.left().accept(this).equals(Fraction.ONE) &&
                node.right().accept(this).equals(Fraction.ONE)) {
            return Fraction.ONE;
        }
        return Fraction.ZERO;
    }

    @Override
    public @NotNull Fraction visitOr(@NotNull OrNode node) {
        if (node.left().accept(this).equals(Fraction.ONE) ||
                node.right().accept(this).equals(Fraction.ONE)) {
            return Fraction.ONE;
        }
        return Fraction.ZERO;
    }

    @Override
    public @NotNull Fraction visitNot(@NotNull NotNode node) {
        if (node.condition().accept(this).equals(Fraction.ONE)) {
            return Fraction.ZERO;
        }
        return Fraction.ONE;
    }

    @Override
    public @NotNull Fraction visitGe(@NotNull GeNode node) {
        if (node.left().accept(this).sub(node.right().accept(this)).signum() >= 0) {
            return Fraction.ONE;
        }
        return Fraction.ZERO;
    }

    @Override
    public @NotNull Fraction visitGt(@NotNull GtNode node) {
        if (node.left().accept(this).sub(node.right().accept(this)).signum() > 0) {
            return Fraction.ONE;
        }
        return Fraction.ZERO;
    }

    @Override
    public @NotNull Fraction visitLe(@NotNull LeNode node) {
        if (node.left().accept(this).sub(node.right().accept(this)).signum() <= 0) {
            return Fraction.ONE;
        }
        return Fraction.ZERO;
    }

    @Override
    public @NotNull Fraction visitLt(@NotNull LtNode node) {
        if (node.left().accept(this).sub(node.right().accept(this)).signum() < 0) {
            return Fraction.ONE;
        }
        return Fraction.ZERO;
    }

    @Override
    public @NotNull Fraction visitTrue(@NotNull TrueNode node) {
        return Fraction.ONE;
    }

    @Override
    public @NotNull Fraction visitFalse(@NotNull FalseNode node) {
        return Fraction.ZERO;
    }

    @Override
    public @NotNull Fraction visitDefault(@NotNull AstNode node) {
        throw new IllegalStateException("Unexpected node to visit in the interpreter.");
    }

}