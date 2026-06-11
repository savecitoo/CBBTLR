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
        throw new UnsupportedOperationException("parent() not supported.");
    }

    // ─── Visitor / traversal ─────────────────────────────────────────────────

    /** Accept a visitor (classic Visitor pattern). */
    default <T> T accept(AstVisitor<T> visitor) {
        throw new UnsupportedOperationException("accept() not supported.");
    }

    // ─── Decorator-added capabilities ────────────────────────────────────────

    /**
     * Type: "typed"
     * Resolved static type after semantic analysis (e.g. "int", "String").
     */
    default String resolvedType() {
        throw new UnsupportedOperationException("resolvedType() not supported — node is not typed");
    }


    // ─── Debugging ───────────────────────────────────────────────────────────

    default String prettyPrint() {
        StringBuilder sb = new StringBuilder();
        prettyPrint(sb, "", true);
        return sb.toString();
    }

    private void prettyPrint(StringBuilder sb, String prefix, boolean isLast) {

        sb.append(prefix)
                .append(isLast ? "└── " : "├── ")
                .append(this)
                .append('\n');

        List<AstNode> kids = children();

        for (int i = 0; i < kids.size(); i++) {
            boolean last = i == kids.size() - 1;

            kids.get(i).prettyPrint(
                    sb,
                    prefix + (isLast ? "    " : "│   "),
                    last
            );
        }
    }
}