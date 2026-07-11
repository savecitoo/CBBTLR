package visitor;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.IdentifierNode;
import ast.constant.IntNode;
import ast.expression.*;
import utils.Fraction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ValueExpressionEvaluator implements AstVisitor<Fraction> {

    /** Interfaccia di callback verso l'interprete per le chiamate di funzione. */
    public interface FunctionCallHandler {
        @NotNull Fraction call(@NotNull String name, @NotNull List<@NotNull Fraction> args);
    }

    private final @NotNull Map<@NotNull String, @NotNull Fraction> unitFract;

    private final @NotNull FunctionCallHandler callHandler;

    /** Ambiente corrente: iniettato dall'interprete prima di ogni chiamata a `accept`. */
    private Map<@NotNull String, @NotNull Fraction> env;

    public ValueExpressionEvaluator(@NotNull Map<@NotNull String, @NotNull Fraction> unitFract,
                                    @NotNull FunctionCallHandler callHandler) {
        this.unitFract = Map.copyOf(unitFract);
        this.callHandler = callHandler;
    }

    /** Da richiamare dall'interprete ogni volta che cambia il frame corrente. */
    public void setEnv(@NotNull Map<@NotNull String, @NotNull Fraction> env) {
        this.env = env;
    }

    @Override
    public @NotNull Fraction visitDefault(AstNode node) {
        throw new IllegalStateException("Unsupported node in Value Expression Evaluator: " + node.getClass());
    }

    @Override
    public @NotNull Fraction visitAdd(AddNode node) {
        Fraction f = node.left().accept(this);
        Fraction g = node.right().accept(this);
        return f.add(g);
    }

    @Override
    public @NotNull Fraction visitSub(SubNode node) {
        return node.left().accept(this).sub(node.right().accept(this));
    }

    @Override
    public @NotNull Fraction visitMul(MulNode node) {
        return node.left().accept(this).mul(node.right().accept(this));
    }

    @Override
    public @NotNull Fraction visitDiv(DivNode node) {
        return node.left().accept(this).div(node.right().accept(this));
    }

    @Override
    public @NotNull Fraction visitPow(PowNode node) {
        return node.base().accept(this).pow(Math.toIntExact(node.exp().num()));
    }

    @Override
    public @NotNull Fraction visitIdentifier(IdentifierNode node) {
        String name = node.id();
        Fraction value = env.get(name);
        if (value != null) return value;
        value = unitFract.get(name);
        if (value != null) return value;
        throw new IllegalStateException("Identificatore non risolto a runtime: " + name);
    }

    @Override
    public @NotNull Fraction visitInt(IntNode node) {
        return Fraction.fromNumDen(node.num(), 1);
    }

    @Override
    public @NotNull Fraction visitFuncCall(FuncCallNode node) {
        List<Fraction> args = new ArrayList<>();
        for (AstNode arg : node.args())
            args.add(arg.accept(this));
        return callHandler.call(node.func().id(), args);
    }
}