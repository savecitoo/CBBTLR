package ast;

import java.util.List;
import java.util.Optional;

/**
 * Models an abstract syntax tree node.
 *
 * Core methods must be implemented by every node.
 * Default methods represent optional/decorator-added behaviour
 * and throw UnsupportedOperationException unless overridden.
 */
public interface AstNode {

    // ─── Core (must implement) ────────────────────────────────────────────────

    /** The syntactic category of this node (e.g. "BinExpr", "Literal", "FunDecl"). */
    String type();

    /** Source position: line where this node starts (1-based). */
    int line();

    /** Source position: column where this node starts (0-based). */
    int column();

    /** Ordered list of child nodes (leaves return an empty list). */
    List<AstNode> children();

    // ─── Structural helpers ───────────────────────────────────────────────────

    /** Returns true if this node has no children. */
    default boolean isLeaf() {
        return children().isEmpty();
    }

    /** Returns the parent node, if tracked by the implementation. */
    default Optional<AstNode> parent() {
        throw new UnsupportedOperationException("parent() not supported by " + type());
    }

    // ─── Visitor / traversal ─────────────────────────────────────────────────

    /** Accept a visitor (classic Visitor pattern). */
    default <T> T accept(AstVisitor<T> visitor) {
        throw new UnsupportedOperationException("accept() not supported by " + type());
    }

    // ─── Decorator-added capabilities ────────────────────────────────────────

    /**
     * Type: "typed"
     * Resolved static type after semantic analysis (e.g. "int", "String").
     */
    default String resolvedType() {
        throw new UnsupportedOperationException("resolvedType() not supported — node is not typed");
    }


    /**
     * Type: "scoped"
     * Symbol table / scope associated with this node.
     */
    default SymbolTable scope() {
        throw new UnsupportedOperationException("scope() not supported — node is not scoped");
    }

    // ─── Debugging ───────────────────────────────────────────────────────────

    default String prettyPrint() {
        return prettyPrint("", "");
    }

    private String prettyPrint(String prefix, String childPrefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(toString()); // usa il toString() del nodo concreto

        List<AstNode> kids = children();
        for (int i = 0; i < kids.size(); i++) {
            boolean last = i == kids.size() - 1;
            sb.append('\n');
            sb.append(kids.get(i).prettyPrint(
                    childPrefix + (last ? "└── " : "├── "),
                    childPrefix + (last ? "    " : "│   ")
            ));
        }
        return sb.toString();
    }
}