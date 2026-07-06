package ast.visitor;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.*;
import ast.expression.*;
import ast.operation.*;
import ast.program.BodyNode;
import ast.program.FunctionNode;
import ast.program.ParameterNode;
import ast.program.ProgramNode;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ValueExpressionVisitor implements AstVisitor<Boolean> {

    private Set<String> loc_scope;


    @Override
    public @NotNull Boolean visitDefault(@NotNull AstNode node) {
        for (AstNode child : node.children())
            if (!child.accept(this))
                return false;
        return true;
    }

    @Override
    public @NotNull Boolean visitProgram(@NotNull ProgramNode node) {
        for (String name : node.functions().keySet()) {
            loc_scope = new HashSet<>();
            node.functions().get(name).accept(this);
        }
        return true;
    }

    @Override
    public @NotNull Boolean visitFunction(@NotNull FunctionNode node) {
        for (ParameterNode par : node.parameter()) {
            loc_scope.add(par.id().id());
        }
        return node.body().accept(this);
    }


    private void visitStatBody(@NotNull List<AstNode> stat) {
        for (AstNode child : stat)
            if (!child.accept(this))
                throw new IllegalArgumentException(String.format(
                        "The value type at line %d and column %d does not contains an integer or a variable " +
                                "or a function call",
                        child.line(), child.column()
                ));
    }

    @Override
    public @NotNull Boolean visitBody(@NotNull BodyNode node) {
        visitStatBody(node.decl());
        visitStatBody(node.stat());
        return true;
    }

    @Override
    public @NotNull Boolean visitIf(@NotNull IfNode node) {
        if (!node.condition().accept(this))
            throw new IllegalArgumentException(String.format(
                    "One of the expressions in the condition at line %d and column %d does not contains an integer or a variable " +
                            "or a function call",
                    node.condition().line(), node.condition().column()
            ));
        visitStatBody(node.trueBranch());
        visitStatBody(node.falseBranch());
        return true;
    }

    @Override
    public @NotNull Boolean visitWhile(@NotNull WhileNode node) {
        if (!node.condition().accept(this))
            throw new IllegalArgumentException(String.format(
                    "One of the expressions in the condition at line %d and column %d does not contains an integer or a variable " +
                            "or a function call",
                    node.condition().line(), node.condition().column()
            ));
        visitStatBody(node.instruction());
        return true;
    }

    @Override
    public @NotNull Boolean visitReturn(@NotNull ReturnNode node) {
        return node.ret_stat().accept(this);
    }

    @Override
    public @NotNull Boolean visitDeclaration(@NotNull DeclarationNode node) {
        boolean ok = node.assignment().accept(this);
        loc_scope.add(node.var().id());
        return ok;
    }

    @Override
    public @NotNull Boolean visitAssignment(@NotNull AssignmentNode node) {
        return node.expression().accept(this);
    }

    // --- Operatori aritmetici: basta che UNO dei due lati contenga
    //     un intero, un identificatore di valore o una chiamata di funzione ---

    @Override
    public @NotNull Boolean visitAdd(@NotNull AddNode node) {
        return node.left().accept(this) || node.right().accept(this);
    }

    @Override
    public @NotNull Boolean visitSub(@NotNull SubNode node) {
        return node.left().accept(this) || node.right().accept(this);
    }

    @Override
    public @NotNull Boolean visitMul(@NotNull MulNode node) {
        return node.left().accept(this) || node.right().accept(this);
    }

    @Override
    public @NotNull Boolean visitDiv(@NotNull DivNode node) {
        return node.left().accept(this) || node.right().accept(this);
    }

    @Override
    public @NotNull Boolean visitPow(@NotNull PowNode node) {
        // l'intero a destra di ^ NON rende l'espressione "di valore":
        // si considera solo il lato sinistro (es. m ^ 2 resta dimensionale,
        // x ^ 2 è di valore perché x è una variabile)
        return node.base().accept(this);
    }

// --- Confronti: i due lati sono due espressioni di valore separate,
//     quindi ENTRAMBI devono superare il controllo ---

    @Override
    public @NotNull Boolean visitGt(@NotNull GtNode node) {
        return node.left().accept(this) && node.right().accept(this);
    }

    @Override
    public @NotNull Boolean visitLt(@NotNull LtNode node) {
        return node.left().accept(this) && node.right().accept(this);
    }

    @Override
    public @NotNull Boolean visitGe(@NotNull GeNode node) {
        return node.left().accept(this) && node.right().accept(this);
    }

    @Override
    public @NotNull Boolean visitLe(@NotNull LeNode node) {
        return node.left().accept(this) && node.right().accept(this);
    }

// --- Operatori logici: combinano condizioni, entrambe devono essere valide ---

    @Override
    public @NotNull Boolean visitAnd(@NotNull AndNode node) {
        return node.left().accept(this) && node.right().accept(this);
    }

    @Override
    public @NotNull Boolean visitOr(@NotNull OrNode node) {
        return node.left().accept(this) && node.right().accept(this);
    }

    @Override
    public @NotNull Boolean visitNot(@NotNull NotNode node) {
        return node.condition().accept(this); // adatta al nome del tuo accessor
    }

// --- Foglie rimanenti ---

    @Override
    public @NotNull Boolean visitNum(@NotNull NumNode node) {
        // Num è l'unità adimensionale: da sola non rende l'espressione di valore
        return false;
    }

    @Override
    public @NotNull Boolean visitTrue(@NotNull TrueNode node) {
        return true;
    }

    @Override
    public @NotNull Boolean visitFalse(@NotNull FalseNode node) {
        return true;
    }


    @Override
    public @NotNull Boolean visitFuncCall(@NotNull FuncCallNode node) {
        for (AstNode child : node.args())
            child.accept(this);
        return true;
    }

    @Override
    public @NotNull Boolean visitIdentifier(@NotNull IdentifierNode node) {
        return loc_scope.contains(node.id());
    }

    @Override
    public @NotNull Boolean visitInt(@NotNull IntNode node) {
        return true;
    }

}
