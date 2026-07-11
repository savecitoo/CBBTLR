package ast;

import ast.constant.*;
import ast.operation.*;
import ast.program.*;
import ast.expression.*;
import org.jetbrains.annotations.NotNull;

/**
 * Visitor interface implementing the Visitor design pattern for traversing
 * and processing an Abstract Syntax Tree (AST).
 * <p>
 * Each {@code visitX} method corresponds to a specific concrete node type
 * in the AST. All methods provide a default implementation that delegates
 * to {@link #visitDefault(AstNode)}, allowing implementing classes to
 * override only the methods relevant to their specific traversal logic.
 *
 * @param <T> the type of the result produced by visiting a node
 */
public interface AstVisitor<T> {

    /**
     * Default fallback visit method, invoked by all other {@code visitX}
     * methods unless explicitly overridden.
     * <p>
     * Implementing classes must provide a concrete implementation of this
     * method, which is typically used to handle unsupported node types,
     * throw an exception, or provide generic behavior shared across nodes.
     *
     * @param node the AST node being visited
     * @return the result of visiting the node
     */
    @NotNull T visitDefault(@NotNull AstNode node);

    /**
     * Visits a program node, representing the root of the AST.
     *
     * @param node the program node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitProgram(@NotNull ProgramNode node)       { return visitDefault(node); }

    /**
     * Visits a defining function node.
     *
     * @param node the function node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitFunction(@NotNull FunctionNode node)     { return visitDefault(node); }

    /**
     * Visits a body node.
     *
     * @param node the body node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitBody(@NotNull BodyNode node)             { return visitDefault(node); }

    /**
     * Visits a parameter node, representing a formal parameter of a function.
     *
     * @param node the parameter node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitParameter(@NotNull ParameterNode node)   { return visitDefault(node); }

    /**
     * Visits a declaration node.
     *
     * @param node the declaration node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitDeclaration(@NotNull DeclarationNode node) { return visitDefault(node); }

    /**
     * Visits an if node.
     *
     * @param node the if node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitIf(@NotNull IfNode node)                 { return visitDefault(node); }

    /**
     * Visits a while node.
     *
     * @param node the while node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitWhile(@NotNull WhileNode node)           { return visitDefault(node); }

    /**
     * Visits a return node.
     *
     * @param node the return node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitReturn(@NotNull ReturnNode node)         { return visitDefault(node); }

    /**
     * Visits an expression node.
     *
     * @param node the expression node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitAssignment(@NotNull AssignmentNode node) { return visitDefault(node); }

    /**
     * Visits a function call node.
     *
     * @param node the function call node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitFuncCall(@NotNull FuncCallNode node)     { return visitDefault(node); }

    /**
     * Visits an addition node.
     *
     * @param node the addition node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitAdd(@NotNull AddNode node)               { return visitDefault(node); }

    /**
     * Visits a subtraction node.
     *
     * @param node the subtraction node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitSub(@NotNull SubNode node)               { return visitDefault(node); }

    /**
     * Visits a multiplication node.
     *
     * @param node the multiplication node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitMul(@NotNull MulNode node)               { return visitDefault(node); }

    /**
     * Visits a division node.
     *
     * @param node the division node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitDiv(@NotNull DivNode node)               { return visitDefault(node); }

    /**
     * Visits a power node.
     *
     * @param node the power node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitPow(@NotNull PowNode node)               { return visitDefault(node); }

    /**
     * Visits a logical AND node.
     *
     * @param node the AND node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitAnd(@NotNull AndNode node)               { return visitDefault(node); }

    /**
     * Visits a logical OR node.
     *
     * @param node the OR node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitOr(@NotNull OrNode node)                 { return visitDefault(node); }

    /**
     * Visits a logical NOT node.
     *
     * @param node the NOT node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitNot(@NotNull NotNode node)               { return visitDefault(node); }

    /**
     * Visits a greater-than node.
     *
     * @param node the greater-than node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitGt(@NotNull GtNode node)                 { return visitDefault(node); }

    /**
     * Visits a less-than node.
     *
     * @param node the less-than node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitLt(@NotNull LtNode node)                 { return visitDefault(node); }

    /**
     * Visits a greater-or-equal node.
     *
     * @param node the greater-or-equal node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitGe(@NotNull GeNode node)                 { return visitDefault(node); }

    /**
     * Visits a less-or-equal node.
     *
     * @param node the less-or-equal node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitLe(@NotNull LeNode node)                 { return visitDefault(node); }

    /**
     * Visits an identifier node.
     *
     * @param node the identifier node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitIdentifier(@NotNull IdentifierNode node) { return visitDefault(node); }

    /**
     * Visits an integer constant node.
     *
     * @param node the integer constant node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitInt(@NotNull IntNode node)               { return visitDefault(node); }

    /**
     * Visits the type Num node.
     *
     * @param node the numeric constant node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitNum(@NotNull NumNode node)               { return visitDefault(node); }

    /**
     * Visits a boolean true constant node.
     *
     * @param node the true constant node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitTrue(@NotNull TrueNode node)             { return visitDefault(node); }

    /**
     * Visits a boolean false constant node..
     *
     * @param node the false constant node to visit
     * @return the result of visiting the node
     */
    default @NotNull T visitFalse(@NotNull FalseNode node)           { return visitDefault(node); }
}