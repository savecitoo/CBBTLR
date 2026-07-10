package ast.visitor;

import ast.AstNode;
import ast.AstVisitor;
import ast.operation.*;
import ast.program.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.Fraction;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Interprete ricorsivo per CoDiNE.
 * <p>
 * Esegue le istruzioni del programma (assumendo che l'AST sia già stato
 * validato dai controlli semantici) e restituisce il valore prodotto
 * dall'esecuzione di {@code main}. Non effettua alcuna stampa: è
 * responsabilità del chiamante (tipicamente una Facade o il {@code main}
 * dell'eseguibile) decidere cosa fare del risultato restituito.
 * <p>
 * Gli ambienti locali delle funzioni sono gestiti tramite uno stack di frame
 * di attivazione: ogni chiamata di funzione fa push di un nuovo frame, il
 * ritorno (che avviene sempre per {@link ReturnException}) fa pop. Il
 * valutatore di espressioni legge l'ambiente corrente direttamente dalla cima
 * dello stack, quindi è coerente per costruzione anche durante le chiamate di
 * funzione annidate nelle espressioni.
 */
public class RecursiveInterpreter implements AstVisitor<Void> {

    private final @NotNull Map<@NotNull String, @NotNull FunctionNode> functions;
    private final @NotNull ValueExpressionEvaluator exprEvaluator;
    private final @NotNull ConditionEvaluator condEvaluator;

    /**
     * Stack dei frame di attivazione: in cima c'è l'ambiente locale
     * (variabili e parametri) della funzione attualmente in esecuzione.
     */
    private final @NotNull Deque<Map<String, Fraction>> callStack = new ArrayDeque<>();

    private @Nullable Fraction returnValue;

    /** Usata internamente per propagare il valore di un'istruzione {@code return}. */
    private static class ReturnException extends RuntimeException {
        @NotNull Fraction value;
        ReturnException(@NotNull Fraction value) {
            super(null, null, false, false); // niente stack trace: è controllo di flusso, non un errore
            this.value = value;
        }
    }

    public RecursiveInterpreter(@NotNull Map<@NotNull String, @NotNull Fraction> unit_fract,
                                @NotNull Map<@NotNull String, @NotNull FunctionNode> functions) {
        this.functions = functions;
        this.exprEvaluator = new ValueExpressionEvaluator(unit_fract, this::callFunction);
        this.condEvaluator = new ConditionEvaluator(exprEvaluator);
    }

    /**
     * Esegue il programma a partire da {@code main}, con gli argomenti dati,
     * e restituisce il valore prodotto dal {@code return} che ha terminato
     * l'esecuzione.
     *
     * @param args i valori da passare ai parametri formali di {@code main}, nell'ordine
     * @return il risultato restituito da {@code main}
     */
    public @NotNull Fraction run(@NotNull List<@NotNull Fraction> args) {
        if (functions.get("main").parameter().size() != args.size())
            throw new IllegalArgumentException(
                    "The number of arguments given in input must be the same number of parameters of the main.");
        return callFunction("main", args);
    }


    private @NotNull Map<String, Fraction> env() {
        if (callStack.isEmpty())
            throw  new IllegalStateException("Empty record stack");
        return callStack.peek();
    }

    private @NotNull Fraction callFunction(@NotNull String name, @NotNull List<@NotNull Fraction> args) {
        FunctionNode function = functions.get(name);
        if (function == null)
            throw new IllegalStateException("Funzione non trovata: " + name);

        Map<String, Fraction> frame = new HashMap<>();
        List<ParameterNode> params = function.parameter();
        for (int i = 0; i < params.size(); i++)
            frame.put(params.get(i).id().id(), args.get(i));

        callStack.push(frame);
        exprEvaluator.setEnv(frame);
        function.body().accept(this);

        Fraction result = returnValue;
        returnValue = null;
        callStack.pop();
        if (!callStack.isEmpty()) exprEvaluator.setEnv(env());
        // it gives a flag, but since we have the Define Return
        // visitor returnValue cannot be null after a function call
        return result;
    }

    @Override
    public @NotNull Void visitDefault(@NotNull AstNode node) {
        throw new IllegalStateException("Nodo non gestito da RecursiveInterpreter: " + node.getClass());
    }

    @Override
    public @NotNull Void visitBody(@NotNull BodyNode node) {
        for (AstNode decl : node.decl())
            decl.accept(this);
        for (AstNode stmt : node.stat())
            stmt.accept(this);
        return null;
    }

    @Override
    public @NotNull Void visitDeclaration(@NotNull DeclarationNode node) {
        env().put(node.var().id(), node.assignment().accept(exprEvaluator));
        return null;
    }

    @Override
    public @NotNull Void visitAssignment(@NotNull AssignmentNode node) {
        env().put(node.var().id(), node.expression().accept(exprEvaluator));
        return null;
    }

    @Override
    public @NotNull Void visitIf(@NotNull IfNode node) {
        if (node.condition().accept(condEvaluator)) {
            for (AstNode child : node.trueBranch())
                child.accept(this);
        } else {
            for (AstNode child : node.falseBranch())
                child.accept(this);
        }
        return null;
    }

    @Override
    public @NotNull Void visitWhile(@NotNull WhileNode node) {
        while (node.condition().accept(condEvaluator)) {
            for (AstNode child : node.instruction())
                child.accept(this);
        }
        return null;
    }

    @Override
    public @NotNull Void visitReturn(@NotNull ReturnNode node) {
        returnValue = node.ret_stat().accept(exprEvaluator);
        return null;
    }
}