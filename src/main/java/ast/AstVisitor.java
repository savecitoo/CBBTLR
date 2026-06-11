package ast;

import ast.constant.*;
import ast.operation.*;
import ast.program.*;
import ast.expression.*;

public interface AstVisitor<T> {

    // --- fallback ---
    T visitDefault(AstNode node);

    // === STRUTTURA PROGRAMMA ===
    default T visitProgram(ProgramNode node)       { return visitDefault(node); }
    default T visitFunction(FunctionNode node)     { return visitDefault(node); }
    default T visitBody(BodyNode node)             { return visitDefault(node); }
    default T visitParameter(ParameterNode node)   { return visitDefault(node); }
    default T visitDeclaration(DeclarationNode node) { return visitDefault(node); }

    default T visitIf(IfNode node)                 { return visitDefault(node); }
    default T visitWhile(WhileNode node)           { return visitDefault(node); }
    default T visitReturn(ReturnNode node)         { return visitDefault(node); }

    default T visitAssignment(AssignmentNode node) { return visitDefault(node); }
    default T visitFuncCall(FuncCallNode node)     { return visitDefault(node); }

    default T visitAdd(AddNode node)               { return visitDefault(node); }
    default T visitSub(SubNode node)               { return visitDefault(node); }
    default T visitMul(MulNode node)               { return visitDefault(node); }
    default T visitDiv(DivNode node)               { return visitDefault(node); }
    default T visitPow(PowNode node)               { return visitDefault(node); }
    default T visitMinus(MinusNode node)           { return visitDefault(node); }  // unario

    default T visitAnd(AndNode node)               { return visitDefault(node); }
    default T visitOr(OrNode node)                 { return visitDefault(node); }
    default T visitNot(NotNode node)               { return visitDefault(node); }

    default T visitGt(GtNode node)                 { return visitDefault(node); }
    default T visitLt(LtNode node)                 { return visitDefault(node); }
    default T visitGe(GeNode node)                 { return visitDefault(node); }
    default T visitLe(LeNode node)                 { return visitDefault(node); }

    default T visitIdentifier(IdentifierNode node) { return visitDefault(node); }
    default T visitInt(IntNode node)               { return visitDefault(node); }
    default T visitNum(NumNode node)               { return visitDefault(node); }
    default T visitTrue(TrueNode node)             { return visitDefault(node); }
    default T visitFalse(FalseNode node)           { return visitDefault(node); }
}