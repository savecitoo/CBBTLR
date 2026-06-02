package ast;

/**
 * Visitor over an AST.
 * Override only the node types your pass cares about;
 * all others fall through to visitDefault().
 */
public interface AstVisitor<T> {

    // ─── Fallback ─────────────────────────────────────────────────────────────

    /**
     * Called for any node whose specific visit method is not overridden.
     * Default implementation visits all children and returns null.
     */
    default T visitDefault(AstNode node) {
        T result = null;
        for (AstNode child : node.children()) {
            result = child.accept(this);
        }
        return result;
    }

    // ─── Declarations ─────────────────────────────────────────────────────────

    default T visitProgram(AstNode node)       { return visitDefault(node); }
    default T visitFunDecl(AstNode node)       { return visitDefault(node); }
    default T visitVarDecl(AstNode node)       { return visitDefault(node); }
    default T visitParam(AstNode node)         { return visitDefault(node); }
    default T visitClassDecl(AstNode node)     { return visitDefault(node); }

    // ─── Statements ───────────────────────────────────────────────────────────

    default T visitBlock(AstNode node)         { return visitDefault(node); }
    default T visitIf(AstNode node)            { return visitDefault(node); }
    default T visitWhile(AstNode node)         { return visitDefault(node); }
    default T visitFor(AstNode node)           { return visitDefault(node); }
    default T visitReturn(AstNode node)        { return visitDefault(node); }
    default T visitBreak(AstNode node)         { return visitDefault(node); }
    default T visitContinue(AstNode node)      { return visitDefault(node); }
    default T visitExprStmt(AstNode node)      { return visitDefault(node); }

    // ─── Expressions ──────────────────────────────────────────────────────────

    default T visitBinExpr(AstNode node)       { return visitDefault(node); }
    default T visitUnaryExpr(AstNode node)     { return visitDefault(node); }
    default T visitAssign(AstNode node)        { return visitDefault(node); }
    default T visitCall(AstNode node)          { return visitDefault(node); }
    default T visitFieldAccess(AstNode node)   { return visitDefault(node); }
    default T visitIndex(AstNode node)         { return visitDefault(node); }
    default T visitCast(AstNode node)          { return visitDefault(node); }
    default T visitTernary(AstNode node)       { return visitDefault(node); }

    // ─── Literals & terminals ─────────────────────────────────────────────────

    default T visitIntLiteral(AstNode node)    { return visitDefault(node); }
    default T visitFloatLiteral(AstNode node)  { return visitDefault(node); }
    default T visitBoolLiteral(AstNode node)   { return visitDefault(node); }
    default T visitStringLiteral(AstNode node) { return visitDefault(node); }
    default T visitNullLiteral(AstNode node)   { return visitDefault(node); }
    default T visitIdentifier(AstNode node)    { return visitDefault(node); }
}